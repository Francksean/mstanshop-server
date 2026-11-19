package com.mstan.shop.order.service;

import com.mstan.shop.cart.entity.Cart;
import com.mstan.shop.cart.service.CartService;
import com.mstan.shop.common.email.EmailService;
import com.mstan.shop.common.email.OrderEmailData;
import com.mstan.shop.common.email.OrderEmailItem;
import com.mstan.shop.common.exception.InsufficientStockException;
import com.mstan.shop.common.exception.InvalidPromoCodeException;
import com.mstan.shop.common.exception.OrderCannotBeCancelledException;
import com.mstan.shop.common.exception.ResourceNotFoundException;
import com.mstan.shop.whatsapp.service.GenukaWhatsAppService;
import com.mstan.shop.common.util.CategoryPromotionResolver;
import com.mstan.shop.common.util.CurrencyFormatter;
import com.mstan.shop.common.util.OrderNumberGenerator;
import com.mstan.shop.common.util.PagedResponse;
import com.mstan.shop.order.dto.CreateOrderRequest;
import com.mstan.shop.order.dto.GuestOrderCreateRequest;
import com.mstan.shop.order.dto.GuestOrderItemRequest;
import com.mstan.shop.order.dto.OrderResponse;
import com.mstan.shop.order.dto.OrderSummaryResponse;
import com.mstan.shop.order.dto.UpdateOrderStatusRequest;
import com.mstan.shop.order.entity.Address;
import com.mstan.shop.order.entity.Order;
import com.mstan.shop.order.entity.OrderItem;
import com.mstan.shop.order.entity.OrderStatus;
import com.mstan.shop.order.mapper.OrderMapper;
import com.mstan.shop.order.repository.AddressRepository;
import com.mstan.shop.order.repository.OrderRepository;
import com.mstan.shop.order.repository.OrderSpecifications;
import com.mstan.shop.payment.entity.Payment;
import com.mstan.shop.payment.entity.PaymentMethod;
import com.mstan.shop.payment.entity.PaymentStatus;
import com.mstan.shop.payment.repository.PaymentRepository;
import com.mstan.shop.payment.service.FapshiPaymentService;
import com.mstan.shop.product.entity.Product;
import com.mstan.shop.product.entity.ProductVariant;
import com.mstan.shop.product.mapper.ProductMapper;
import com.mstan.shop.product.repository.ProductRepository;
import com.mstan.shop.product.repository.ProductVariantRepository;
import com.mstan.shop.promo.entity.PromoCode;
import com.mstan.shop.promo.repository.PromoCodeRepository;
import com.mstan.shop.promo.service.PromoCodeEvaluation;
import com.mstan.shop.promo.service.PromoCodeService;
import com.mstan.shop.user.entity.User;
import com.mstan.shop.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final AddressRepository addressRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final PromoCodeRepository promoCodeRepository;
    private final PromoCodeService promoCodeService;
    private final PaymentRepository paymentRepository;
    private final FapshiPaymentService fapshiPaymentService;
    private final UserRepository userRepository;
    private final CartService cartService;
    private final OrderMapper orderMapper;
    private final ProductMapper productMapper;
    private final OrderNumberGenerator orderNumberGenerator;
    private final EmailService emailService;
    private final GenukaWhatsAppService whatsAppService;

    @Transactional
    public OrderResponse createOrder(UUID userId, CreateOrderRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("Utilisateur", userId));

        Cart cart = cartService.getOrCreateCart(userId);
        if (cart.getItems().isEmpty()) {
            throw new IllegalArgumentException("Le panier est vide, impossible de passer commande");
        }

        if (request.paymentMethod() == PaymentMethod.MOBILE_PAYMENT
                && (request.phoneNumber() == null || request.phoneNumber().isBlank())) {
            throw new IllegalArgumentException("Le numéro Mobile Money est obligatoire pour ce mode de paiement");
        }

        Address address = new Address();
        address.setUser(user);
        address.setFirstName(request.address().firstName());
        address.setLastName(request.address().lastName());
        address.setStreet(request.address().street());
        address.setCity(request.address().city());
        address.setPostalCode(request.address().postalCode());
        address.setCountry(request.address().country());
        address.setPhone(request.address().phone());
        address.setWhatsapp(request.address().whatsapp());
        addressRepository.save(address);

        Order order = new Order();
        order.setOrderNumber(orderNumberGenerator.generate());
        order.setUser(user);
        order.setAddress(address);
        order.setStatus(OrderStatus.EN_ATTENTE);

        List<OrderLine> lines = cart.getItems().stream()
                .map(cartItem -> new OrderLine(cartItem.getProduct(), cartItem.getVariant(), cartItem.getQuantity()))
                .toList();
        BigDecimal total = buildOrderItems(order, lines);

        applyPromoCode(order, request.promoCode(), total);

        orderRepository.save(order);
        cartService.clearCart(userId);

        Payment payment = createPaymentAndNotify(order, address, request.paymentMethod(), request.phoneNumber());

        return orderMapper.toResponse(order, payment);
    }

    @Transactional
    public OrderResponse createGuestOrder(GuestOrderCreateRequest request) {
        if (request.paymentMethod() == PaymentMethod.MOBILE_PAYMENT
                && (request.phoneNumber() == null || request.phoneNumber().isBlank())) {
            throw new IllegalArgumentException("Le numéro Mobile Money est obligatoire pour ce mode de paiement");
        }

        Address address = new Address();
        address.setFirstName(request.address().firstName());
        address.setLastName(request.address().lastName());
        address.setStreet(request.address().street());
        address.setCity(request.address().city());
        address.setPostalCode(request.address().postalCode());
        address.setCountry(request.address().country());
        address.setPhone(request.address().phone());
        address.setWhatsapp(request.address().whatsapp());
        addressRepository.save(address);

        Order order = new Order();
        order.setOrderNumber(orderNumberGenerator.generate());
        order.setGuestEmail(request.email() != null && !request.email().isBlank() ? request.email() : null);
        order.setAddress(address);
        order.setStatus(OrderStatus.EN_ATTENTE);

        List<OrderLine> lines = request.items().stream()
                .map(this::resolveGuestOrderLine)
                .toList();
        BigDecimal total = buildOrderItems(order, lines);

        applyPromoCode(order, request.promoCode(), total);

        orderRepository.save(order);

        Payment payment = createPaymentAndNotify(order, address, request.paymentMethod(), request.phoneNumber());

        return orderMapper.toResponse(order, payment);
    }

    private OrderLine resolveGuestOrderLine(GuestOrderItemRequest item) {
        Product product = productRepository.findById(item.productId())
                .orElseThrow(() -> ResourceNotFoundException.of("Produit", item.productId()));

        ProductVariant variant = null;
        if (item.variantId() != null) {
            variant = productVariantRepository.findById(item.variantId())
                    .orElseThrow(() -> ResourceNotFoundException.of("Variante", item.variantId()));
            if (!variant.getProduct().getId().equals(product.getId())) {
                throw new IllegalArgumentException("La variante ne correspond pas au produit indiqué");
            }
        }

        return new OrderLine(product, variant, item.quantity());
    }

    private record OrderLine(Product product, ProductVariant variant, int quantity) {
    }

    private BigDecimal buildOrderItems(Order order, List<OrderLine> lines) {
        BigDecimal total = BigDecimal.ZERO;
        for (OrderLine line : lines) {
            Product product = line.product();
            ProductVariant variant = line.variant();
            int quantity = line.quantity();

            BigDecimal effectivePrice = CategoryPromotionResolver.resolveEffectivePrice(product);

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setProduct(product);
            orderItem.setProductName(product.getName());
            orderItem.setProductThumbnailUrl(productMapper.resolveThumbnailUrl(product));
            orderItem.setUnitPrice(effectivePrice);
            orderItem.setQuantity(quantity);

            if (variant != null) {
                if (variant.getStock() < quantity) {
                    throw new InsufficientStockException(
                            "Stock insuffisant pour le produit \"" + product.getName() + "\" (couleur : "
                                    + (variant.getColorName() != null ? variant.getColorName()
                                            : variant.getColorHex() != null ? variant.getColorHex() : variant.getSize())
                                    + ", disponible : " + variant.getStock() + ")"
                    );
                }

                variant.setStock(variant.getStock() - quantity);
                productVariantRepository.save(variant);

                orderItem.setVariant(variant);
                orderItem.setVariantColorName(variant.getColorName());
                orderItem.setVariantColorHex(variant.getColorHex());
                orderItem.setVariantSize(variant.getSize());
            } else {
                if (product.getStock() < quantity) {
                    throw new InsufficientStockException(
                            "Stock insuffisant pour le produit \"" + product.getName() + "\" (disponible : " + product.getStock() + ")"
                    );
                }

                product.setStock(product.getStock() - quantity);
                productRepository.save(product);
            }

            order.getItems().add(orderItem);

            total = total.add(effectivePrice.multiply(BigDecimal.valueOf(quantity)));
        }

        return total;
    }

    private void applyPromoCode(Order order, String promoCode, BigDecimal total) {
        BigDecimal discountAmount = BigDecimal.ZERO;
        PromoCode appliedPromo = null;
        if (promoCode != null && !promoCode.isBlank()) {
            PromoCodeEvaluation evaluation = promoCodeService.evaluate(promoCode, total);
            if (!evaluation.valid()) {
                throw new InvalidPromoCodeException(evaluation.reason());
            }

            appliedPromo = evaluation.promoCode();
            discountAmount = evaluation.discountAmount();
            appliedPromo.setUsesCount(appliedPromo.getUsesCount() + 1);
            promoCodeRepository.save(appliedPromo);
        }

        order.setSubtotalAmount(total);
        order.setDiscountAmount(appliedPromo != null ? discountAmount : null);
        order.setPromoCode(appliedPromo != null ? appliedPromo.getCode() : null);
        order.setTotalAmount(total.subtract(discountAmount));
    }

    private Payment createPaymentAndNotify(Order order, Address address, PaymentMethod paymentMethod, String phoneNumber) {
        Payment payment;
        if (paymentMethod == PaymentMethod.CASH_ON_DELIVERY || paymentMethod == PaymentMethod.PAYMENT_ON_PICKUP) {
            payment = new Payment();
            payment.setOrder(order);
            payment.setMethod(paymentMethod);
            payment.setStatus(PaymentStatus.PENDING);
            payment.setAmount(order.getTotalAmount());
            payment.setCurrency("XAF");
            payment.setPhoneNumber(phoneNumber != null && !phoneNumber.isBlank() ? phoneNumber : address.getPhone());
            payment = paymentRepository.save(payment);

            // Paiement à la livraison ou en boutique : la commande est confirmée immédiatement,
            // seul l'encaissement reste à faire — pas d'attente asynchrone à justifier ici.
            String phone = order.getAddress().getPhone();
            String totalFormatted = CurrencyFormatter.format(order.getTotalAmount());
            sendOrderConfirmation(order, phone, totalFormatted);
        } else {
            // Le paiement mobile est asynchrone (le client confirme sur son téléphone) : pas
            // d'email/SMS/WhatsApp de confirmation ici, seulement une fois confirmPayment()
            // appelé par le webhook/le polling.
            payment = fapshiPaymentService.initiateCollection(order, phoneNumber);
        }

        return payment;
    }

    /** Guests without an email only get WhatsApp confirmation, never a null-email crash. */
    private String resolveNotificationEmail(Order order) {
        return order.getUser() != null ? order.getUser().getEmail() : order.getGuestEmail();
    }

    private void sendOrderConfirmation(Order order, String phone, String totalFormatted) {
        String email = resolveNotificationEmail(order);
        if (email != null) {
            emailService.sendOrderConfirmation(email, toEmailData(order));
        }
        whatsAppService.sendOrderConfirmation(order, phone, totalFormatted);
    }

    @Transactional(readOnly = true)
    public PagedResponse<OrderSummaryResponse> getOrderHistory(UUID userId, Pageable pageable) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("Utilisateur", userId));

        Page<OrderSummaryResponse> page = orderRepository.findByUser(user, pageable).map(orderMapper::toSummaryResponse);
        return PagedResponse.from(page);
    }

    @Transactional(readOnly = true)
    public OrderResponse getGuestOrderByNumber(String orderNumber) {
        Order order = orderRepository.findByOrderNumberAndUserIsNull(orderNumber)
                .orElseThrow(() -> ResourceNotFoundException.of("Commande", orderNumber));

        return toResponseWithPayment(order);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrder(UUID userId, UUID orderId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("Utilisateur", userId));

        Order order = orderRepository.findByIdAndUser(orderId, user)
                .orElseThrow(() -> ResourceNotFoundException.of("Commande", orderId));

        return toResponseWithPayment(order);
    }

    /** Polling endpoint: re-checks Fapshi server-to-server rather than trusting only the local row. */
    @Transactional
    public OrderResponse refreshPaymentStatus(UUID userId, UUID orderId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("Utilisateur", userId));

        Order order = orderRepository.findByIdAndUser(orderId, user)
                .orElseThrow(() -> ResourceNotFoundException.of("Commande", orderId));

        Payment payment = paymentRepository.findFirstByOrderIdOrderByCreatedAtDesc(orderId).orElse(null);
        if (payment != null && payment.getMethod() == PaymentMethod.MOBILE_PAYMENT
                && payment.getStatus() == PaymentStatus.PENDING && payment.getProviderReference() != null) {
            PaymentStatus latest = fapshiPaymentService.checkStatus(payment.getProviderReference());
            if (latest == PaymentStatus.SUCCESSFUL) {
                confirmPayment(orderId, payment.getProviderReference());
            } else if (latest == PaymentStatus.FAILED) {
                failPayment(orderId, payment.getProviderReference(), "Paiement refusé ou expiré");
            }
        }

        Order refreshed = orderRepository.findByIdAndUser(orderId, user)
                .orElseThrow(() -> ResourceNotFoundException.of("Commande", orderId));
        return toResponseWithPayment(refreshed);
    }

    @Transactional
    public void confirmPayment(UUID orderId, String providerReference) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> ResourceNotFoundException.of("Commande", orderId));
        Payment payment = paymentRepository.findByProviderReference(providerReference)
                .orElseThrow(() -> ResourceNotFoundException.of("Paiement", providerReference));

        if (payment.getStatus() == PaymentStatus.SUCCESSFUL) {
            return;
        }
        if (order.getStatus() == OrderStatus.ANNULEE) {
            log.warn("Paiement confirmé reçu pour la commande {} (référence {}) déjà annulée — réconciliation manuelle nécessaire",
                    orderId, providerReference);
            return;
        }

        payment.setStatus(PaymentStatus.SUCCESSFUL);
        paymentRepository.save(payment);

        order.setStatus(OrderStatus.PAYEE);
        orderRepository.save(order);

        String phone = order.getAddress().getPhone();
        String totalFormatted = CurrencyFormatter.format(order.getTotalAmount());
        sendOrderConfirmation(order, phone, totalFormatted);
    }

    @Transactional
    public void failPayment(UUID orderId, String providerReference, String reason) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> ResourceNotFoundException.of("Commande", orderId));
        Payment payment = paymentRepository.findByProviderReference(providerReference)
                .orElseThrow(() -> ResourceNotFoundException.of("Paiement", providerReference));

        if (payment.getStatus() == PaymentStatus.FAILED) {
            return;
        }

        payment.setStatus(PaymentStatus.FAILED);
        paymentRepository.save(payment);

        if (order.getStatus() == OrderStatus.ANNULEE || order.getStatus() == OrderStatus.ECHEC_PAIEMENT) {
            return;
        }

        restockItems(order);
        order.setStatus(OrderStatus.ECHEC_PAIEMENT);
        orderRepository.save(order);

        String phone = order.getAddress().getPhone();
        String email = resolveNotificationEmail(order);
        if (email != null) {
            emailService.sendPaymentFailed(email, order.getOrderNumber());
        }
        whatsAppService.sendPaymentFailed(order, phone);
    }

    /**
     * Admin action confirming that cash was actually collected — at delivery (COD) or at
     * store pickup — deliberately separate from marking an order "Livrée", since a delivery
     * or pickup attempt can fail to collect payment independently of the order being fulfilled.
     */
    @Transactional
    public OrderResponse confirmCashPayment(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> ResourceNotFoundException.of("Commande", orderId));
        Payment payment = paymentRepository.findFirstByOrderIdOrderByCreatedAtDesc(orderId)
                .orElseThrow(() -> ResourceNotFoundException.of("Paiement", orderId));

        if (payment.getMethod() != PaymentMethod.CASH_ON_DELIVERY && payment.getMethod() != PaymentMethod.PAYMENT_ON_PICKUP) {
            throw new IllegalArgumentException("Cette commande n'est pas payée à la livraison ou en boutique");
        }
        if (payment.getStatus() == PaymentStatus.SUCCESSFUL) {
            return toResponseWithPayment(order);
        }

        payment.setStatus(PaymentStatus.SUCCESSFUL);
        paymentRepository.save(payment);

        // Ne fait jamais "reculer" le statut de la commande : si elle est déjà expédiée/livrée,
        // seul le statut de paiement change — Order.status ne représente que la progression de
        // la livraison une fois celle-ci amorcée.
        if (order.getStatus() == OrderStatus.EN_ATTENTE) {
            order.setStatus(OrderStatus.PAYEE);
            orderRepository.save(order);
        }

        return toResponseWithPayment(order);
    }

    @Transactional
    public OrderResponse updateStatus(UUID orderId, UpdateOrderStatusRequest request) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> ResourceNotFoundException.of("Commande", orderId));

        if (request.status() == OrderStatus.ANNULEE) {
            cancelOrder(order);
            return toResponseWithPayment(order);
        }

        order.setStatus(request.status());
        orderRepository.save(order);

        return toResponseWithPayment(order);
    }

    @Transactional
    public OrderResponse cancelOrderAsClient(UUID userId, UUID orderId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> ResourceNotFoundException.of("Utilisateur", userId));

        Order order = orderRepository.findByIdAndUser(orderId, user)
                .orElseThrow(() -> ResourceNotFoundException.of("Commande", orderId));

        cancelOrder(order);
        return toResponseWithPayment(order);
    }

    @Transactional
    public OrderResponse cancelOrderAsAdmin(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> ResourceNotFoundException.of("Commande", orderId));

        cancelOrder(order);
        return toResponseWithPayment(order);
    }

    private void cancelOrder(Order order) {
        if (!isCancellable(order)) {
            throw new OrderCannotBeCancelledException(
                    "La commande ne peut plus être annulée (statut actuel : " + order.getStatus() + ")"
            );
        }

        // Invalide tout paiement encore en attente pour qu'un webhook/poll tardif ne vienne pas
        // re-confirmer une commande déjà annulée.
        paymentRepository.findFirstByOrderIdOrderByCreatedAtDesc(order.getId())
                .filter(payment -> payment.getStatus() == PaymentStatus.PENDING)
                .ifPresent(payment -> {
                    payment.setStatus(PaymentStatus.FAILED);
                    paymentRepository.save(payment);
                });

        restockItems(order);

        order.setStatus(OrderStatus.ANNULEE);
        orderRepository.save(order);

        String phone = order.getAddress().getPhone();
        String email = resolveNotificationEmail(order);
        if (email != null) {
            emailService.sendOrderCancellation(email, toEmailData(order));
        }
        whatsAppService.sendOrderCancellation(order, phone);
    }

    private void restockItems(Order order) {
        for (OrderItem item : order.getItems()) {
            ProductVariant variant = item.getVariant();
            if (variant != null) {
                variant.setStock(variant.getStock() + item.getQuantity());
                productVariantRepository.save(variant);
            } else {
                Product product = item.getProduct();
                product.setStock(product.getStock() + item.getQuantity());
                productRepository.save(product);
            }
        }
    }

    private boolean isCancellable(Order order) {
        return order.getStatus() == OrderStatus.EN_ATTENTE || order.getStatus() == OrderStatus.PAYEE;
    }

    @Transactional(readOnly = true)
    public PagedResponse<OrderSummaryResponse> getAllOrders(OrderStatus status, UUID userId, String orderNumber, Pageable pageable) {
        Specification<Order> spec = Specification.allOf(
                OrderSpecifications.hasStatus(status),
                OrderSpecifications.hasUser(userId),
                OrderSpecifications.hasOrderNumber(orderNumber)
        );

        Page<OrderSummaryResponse> page = orderRepository.findAll(spec, pageable).map(orderMapper::toSummaryResponse);
        return PagedResponse.from(page);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderByIdAdmin(UUID orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> ResourceNotFoundException.of("Commande", orderId));

        return toResponseWithPayment(order);
    }

    private OrderResponse toResponseWithPayment(Order order) {
        Payment payment = paymentRepository.findFirstByOrderIdOrderByCreatedAtDesc(order.getId()).orElse(null);
        return orderMapper.toResponse(order, payment);
    }

    private OrderEmailData toEmailData(Order order) {
        List<OrderEmailItem> items = order.getItems().stream()
                .map(item -> new OrderEmailItem(
                        item.getProductName(),
                        item.getVariantColorName() != null ? item.getVariantColorName() : item.getVariantColorHex(),
                        item.getProductThumbnailUrl(),
                        item.getQuantity(),
                        item.getUnitPrice(),
                        item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()))
                ))
                .toList();

        return new OrderEmailData(
                order.getId(),
                order.getOrderNumber(),
                items,
                order.getSubtotalAmount(),
                order.getDiscountAmount(),
                order.getPromoCode(),
                order.getTotalAmount(),
                order.getAddress().getStreet(),
                order.getAddress().getCity(),
                order.getAddress().getPostalCode(),
                order.getAddress().getCountry()
        );
    }
}
