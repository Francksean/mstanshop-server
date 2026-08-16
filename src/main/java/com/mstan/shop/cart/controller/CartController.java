package com.mstan.shop.cart.controller;

import com.mstan.shop.cart.dto.AddCartItemRequest;
import com.mstan.shop.cart.dto.CartResponse;
import com.mstan.shop.cart.dto.PromoCodePreviewResponse;
import com.mstan.shop.cart.dto.UpdateCartItemRequest;
import com.mstan.shop.cart.service.CartService;
import com.mstan.shop.common.security.SecurityUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@Tag(name = "Cart", description = "Gestion du panier de l'utilisateur authentifié")
@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public CartResponse getCart(@AuthenticationPrincipal SecurityUser user) {
        return cartService.getCart(user.getId());
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(
            @AuthenticationPrincipal SecurityUser user,
            @Valid @RequestBody AddCartItemRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cartService.addItem(user.getId(), request));
    }

    @PutMapping("/items/{itemId}")
    public CartResponse updateItem(
            @AuthenticationPrincipal SecurityUser user,
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateCartItemRequest request
    ) {
        return cartService.updateItemQuantity(user.getId(), itemId, request);
    }

    @DeleteMapping("/items/{itemId}")
    public CartResponse removeItem(@AuthenticationPrincipal SecurityUser user, @PathVariable UUID itemId) {
        return cartService.removeItem(user.getId(), itemId);
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(@AuthenticationPrincipal SecurityUser user) {
        cartService.clearCart(user.getId());
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Prévisualiser un code promo",
            description = "Calcule l'effet d'un code promo sur le panier actuel, sans créer de commande ni "
                    + "consommer d'utilisation du code. Répond toujours 200 : `valid=false` avec un `reason` "
                    + "explicite si le code est inexistant/inactif/expiré/montant minimum non atteint/limite "
                    + "d'utilisations atteinte."
    )
    @GetMapping("/promo-preview")
    public PromoCodePreviewResponse previewPromoCode(
            @AuthenticationPrincipal SecurityUser user,
            @RequestParam String code
    ) {
        return cartService.previewPromoCode(user.getId(), code);
    }
}
