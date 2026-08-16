package com.mstan.shop.dashboard.service;

import com.mstan.shop.dashboard.dto.DashboardSummaryResponse;
import com.mstan.shop.dashboard.dto.OrderStatusCountResponse;
import com.mstan.shop.dashboard.dto.PaymentBreakdownResponse;
import com.mstan.shop.dashboard.dto.PromoUsageResponse;
import com.mstan.shop.dashboard.dto.RevenuePointResponse;
import com.mstan.shop.dashboard.dto.TopCategoryResponse;
import com.mstan.shop.dashboard.dto.TopProductResponse;
import com.mstan.shop.newsletter.repository.NewsletterSubscriptionRepository;
import com.mstan.shop.order.entity.OrderStatus;
import com.mstan.shop.order.repository.OrderItemRepository;
import com.mstan.shop.order.repository.OrderRepository;
import com.mstan.shop.payment.entity.PaymentMethod;
import com.mstan.shop.payment.entity.PaymentStatus;
import com.mstan.shop.payment.repository.PaymentRepository;
import com.mstan.shop.promo.repository.PromoCodeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final List<OrderStatus> REVENUE_STATUSES = List.of(OrderStatus.PAYEE, OrderStatus.EXPEDIEE, OrderStatus.LIVREE);
    private static final Duration DEFAULT_PERIOD = Duration.ofDays(30);

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentRepository paymentRepository;
    private final NewsletterSubscriptionRepository newsletterSubscriptionRepository;
    private final PromoCodeRepository promoCodeRepository;

    public DashboardSummaryResponse summary(Instant from, Instant to, long lowStockCount) {
        Instant[] range = resolveRange(from, to);

        BigDecimal revenue = orderRepository.sumRevenueBetween(REVENUE_STATUSES, range[0], range[1]);
        long orderCount = orderRepository.countBetween(REVENUE_STATUSES, range[0], range[1]);
        BigDecimal averageBasket = orderCount == 0
                ? BigDecimal.ZERO
                : revenue.divide(BigDecimal.valueOf(orderCount), 2, RoundingMode.HALF_UP);
        long pendingOrders = orderRepository.countByStatus(OrderStatus.EN_ATTENTE);
        long newNewsletterSubscribers = newsletterSubscriptionRepository.countByCreatedAtBetween(range[0], range[1]);

        return new DashboardSummaryResponse(revenue, orderCount, averageBasket, pendingOrders, lowStockCount, newNewsletterSubscribers);
    }

    public List<RevenuePointResponse> revenueTimeseries(Instant from, Instant to) {
        Instant[] range = resolveRange(from, to);
        List<String> statuses = REVENUE_STATUSES.stream().map(Enum::name).toList();

        return orderRepository.revenueByDay(statuses, range[0], range[1]).stream()
                .map(row -> new RevenuePointResponse(
                        toLocalDate(row[0]),
                        (BigDecimal) row[1],
                        ((Number) row[2]).longValue()
                ))
                .toList();
    }

    public List<OrderStatusCountResponse> ordersByStatus() {
        return orderRepository.countGroupedByStatus().stream()
                .map(row -> new OrderStatusCountResponse((OrderStatus) row[0], ((Number) row[1]).longValue()))
                .toList();
    }

    public List<PaymentBreakdownResponse> paymentsBreakdown(Instant from, Instant to) {
        Instant[] range = resolveRange(from, to);
        return paymentRepository.breakdownBetween(range[0], range[1]).stream()
                .map(row -> new PaymentBreakdownResponse(
                        (PaymentMethod) row[0],
                        (PaymentStatus) row[1],
                        ((Number) row[2]).longValue(),
                        (BigDecimal) row[3]
                ))
                .toList();
    }

    public List<TopProductResponse> topProducts(Instant from, Instant to, int limit) {
        Instant[] range = resolveRange(from, to);
        Pageable pageable = PageRequest.of(0, limit);
        return orderItemRepository.topProducts(REVENUE_STATUSES, range[0], range[1], pageable).stream()
                .map(row -> new TopProductResponse(
                        (java.util.UUID) row[0],
                        (String) row[1],
                        ((Number) row[2]).longValue(),
                        (BigDecimal) row[3]
                ))
                .toList();
    }

    public List<TopCategoryResponse> topCategories(Instant from, Instant to, int limit) {
        Instant[] range = resolveRange(from, to);
        Pageable pageable = PageRequest.of(0, limit);
        return orderItemRepository.topCategories(REVENUE_STATUSES, range[0], range[1], pageable).stream()
                .map(row -> new TopCategoryResponse(
                        (java.util.UUID) row[0],
                        (String) row[1],
                        (BigDecimal) row[2]
                ))
                .toList();
    }

    public List<PromoUsageResponse> promoUsage() {
        Map<String, BigDecimal> discountByCode = orderRepository.discountByPromoCode().stream()
                .collect(java.util.stream.Collectors.toMap(row -> (String) row[0], row -> (BigDecimal) row[1]));

        return promoCodeRepository.findAll().stream()
                .map(promoCode -> new PromoUsageResponse(
                        promoCode.getCode(),
                        promoCode.getUsesCount(),
                        discountByCode.getOrDefault(promoCode.getCode(), BigDecimal.ZERO)
                ))
                .toList();
    }

    private Instant[] resolveRange(Instant from, Instant to) {
        Instant resolvedTo = to != null ? to : Instant.now();
        Instant resolvedFrom = from != null ? from : resolvedTo.minus(DEFAULT_PERIOD);
        return new Instant[]{resolvedFrom, resolvedTo};
    }

    private LocalDate toLocalDate(Object value) {
        if (value instanceof java.sql.Timestamp timestamp) {
            return timestamp.toInstant().atZone(ZoneOffset.UTC).toLocalDate();
        }
        return ((Instant) value).atZone(ZoneOffset.UTC).toLocalDate();
    }
}
