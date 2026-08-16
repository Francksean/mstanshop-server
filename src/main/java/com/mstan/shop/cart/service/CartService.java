package com.mstan.shop.cart.service;

import com.mstan.shop.cart.dto.AddCartItemRequest;
import com.mstan.shop.cart.dto.CartResponse;
import com.mstan.shop.cart.dto.PromoCodePreviewResponse;
import com.mstan.shop.cart.dto.UpdateCartItemRequest;
import com.mstan.shop.cart.entity.Cart;
import com.mstan.shop.cart.entity.CartItem;
import com.mstan.shop.cart.mapper.CartMapper;
import com.mstan.shop.cart.repository.CartRepository;
import com.mstan.shop.common.exception.InsufficientStockException;
import com.mstan.shop.common.exception.ResourceNotFoundException;
import com.mstan.shop.common.util.CategoryPromotionResolver;
import com.mstan.shop.product.entity.Product;
import com.mstan.shop.product.entity.ProductVariant;
import com.mstan.shop.product.repository.ProductRepository;
import com.mstan.shop.product.repository.ProductVariantRepository;
import com.mstan.shop.promo.service.PromoCodeEvaluation;
import com.mstan.shop.promo.service.PromoCodeService;
import com.mstan.shop.user.entity.User;
import com.mstan.shop.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final PromoCodeService promoCodeService;
    private final UserRepository userRepository;
    private final CartMapper cartMapper;

    @Transactional
    public CartResponse getCart(UUID userId) {
        return toResponse(getOrCreateCart(userId));
    }

    @Transactional
    public CartResponse addItem(UUID userId, AddCartItemRequest request) {
        Cart cart = getOrCreateCart(userId);
        mergeSingleItem(cart, request);
        cartRepository.save(cart);
        return toResponse(cart);
    }

    /**
     * Merges a batch of items (typically a guest/local cart) into the user's server cart at
     * login/register time. Best-effort: an item whose product/variant no longer exists or whose
     * stock is insufficient is skipped and logged rather than failing the whole merge, since this
     * runs as a side effect of authentication and must not block it.
     */
    @Transactional
    public CartResponse mergeGuestItems(UUID userId, List<AddCartItemRequest> items) {
        Cart cart = getOrCreateCart(userId);

        for (AddCartItemRequest item : items) {
            try {
                mergeSingleItem(cart, item);
            } catch (ResourceNotFoundException | InsufficientStockException e) {
                log.warn("Ligne de panier invité ignorée lors de la fusion (utilisateur {}, produit {}) : {}",
                        userId, item.productId(), e.getMessage());
            }
        }

        cartRepository.save(cart);
        return toResponse(cart);
    }

    private void mergeSingleItem(Cart cart, AddCartItemRequest request) {
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> ResourceNotFoundException.of("Produit", request.productId()));
        ProductVariant variant = resolveVariant(product, request.variantId());

        CartItem existingItem = cart.getItems().stream()
                .filter(item -> item.getProduct().getId().equals(product.getId())
                        && Objects.equals(variantId(item.getVariant()), variantId(variant)))
                .findFirst()
                .orElse(null);

        int requestedQuantity = (existingItem != null ? existingItem.getQuantity() : 0) + request.quantity();
        ensureStockAvailable(product, variant, requestedQuantity);

        if (existingItem != null) {
            existingItem.setQuantity(requestedQuantity);
        } else {
            CartItem newItem = new CartItem();
            newItem.setCart(cart);
            newItem.setProduct(product);
            newItem.setVariant(variant);
            newItem.setQuantity(request.quantity());
            cart.getItems().add(newItem);
        }
    }

    @Transactional
    public CartResponse updateItemQuantity(UUID userId, UUID itemId, UpdateCartItemRequest request) {
        Cart cart = getOrCreateCart(userId);
        CartItem item = findItem(cart, itemId);

        ensureStockAvailable(item.getProduct(), item.getVariant(), request.quantity());
        item.setQuantity(request.quantity());

        cartRepository.save(cart);
        return toResponse(cart);
    }

    @Transactional
    public CartResponse removeItem(UUID userId, UUID itemId) {
        Cart cart = getOrCreateCart(userId);
        CartItem item = findItem(cart, itemId);

        cart.getItems().remove(item);
        cartRepository.save(cart);
        return toResponse(cart);
    }

    @Transactional
    public void clearCart(UUID userId) {
        Cart cart = getOrCreateCart(userId);
        cart.getItems().clear();
        cartRepository.save(cart);
    }

    public Cart getOrCreateCart(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("Utilisateur", userId));

        return cartRepository.findByUser(user).orElseGet(() -> {
            Cart cart = new Cart();
            cart.setUser(user);
            return cartRepository.save(cart);
        });
    }

    private CartItem findItem(Cart cart, UUID itemId) {
        return cart.getItems().stream()
                .filter(item -> item.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> ResourceNotFoundException.of("Article du panier", itemId));
    }

    private ProductVariant resolveVariant(Product product, UUID variantId) {
        if (variantId == null) {
            return null;
        }
        return productVariantRepository.findById(variantId)
                .filter(v -> v.getProduct().getId().equals(product.getId()))
                .orElseThrow(() -> ResourceNotFoundException.of("Variante", variantId));
    }

    private UUID variantId(ProductVariant variant) {
        return variant != null ? variant.getId() : null;
    }

    private void ensureStockAvailable(Product product, ProductVariant variant, int requestedQuantity) {
        if (variant != null) {
            if (variant.getStock() < requestedQuantity) {
                throw new InsufficientStockException(
                        "Stock insuffisant pour le produit \"" + product.getName() + "\" (couleur : "
                                + (variant.getColorName() != null ? variant.getColorName() : variant.getColorHex())
                                + ", disponible : " + variant.getStock() + ")"
                );
            }
            return;
        }

        if (product.getStock() < requestedQuantity) {
            throw new InsufficientStockException(
                    "Stock insuffisant pour le produit \"" + product.getName() + "\" (disponible : " + product.getStock() + ")"
            );
        }
    }

    @Transactional(readOnly = true)
    public PromoCodePreviewResponse previewPromoCode(UUID userId, String code) {
        Cart cart = getOrCreateCart(userId);
        BigDecimal subtotal = computeSubtotal(cart);
        PromoCodeEvaluation evaluation = promoCodeService.evaluate(code, subtotal);

        BigDecimal discountAmount = evaluation.valid() ? evaluation.discountAmount() : BigDecimal.ZERO;
        BigDecimal totalAmount = subtotal.subtract(discountAmount);

        return new PromoCodePreviewResponse(code, evaluation.valid(), evaluation.reason(), subtotal, discountAmount, totalAmount);
    }

    private BigDecimal computeSubtotal(Cart cart) {
        return cart.getItems().stream()
                .map(item -> CategoryPromotionResolver.resolveEffectivePrice(item.getProduct())
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private CartResponse toResponse(Cart cart) {
        return new CartResponse(
                cart.getId(),
                cart.getItems().stream().map(cartMapper::toItemResponse).toList(),
                computeSubtotal(cart)
        );
    }
}
