package com.mstan.shop.dashboard.controller;

import com.mstan.shop.dashboard.dto.DashboardSummaryResponse;
import com.mstan.shop.dashboard.dto.OrderStatusCountResponse;
import com.mstan.shop.dashboard.dto.PaymentBreakdownResponse;
import com.mstan.shop.dashboard.dto.PromoUsageResponse;
import com.mstan.shop.dashboard.dto.RevenuePointResponse;
import com.mstan.shop.dashboard.dto.TopCategoryResponse;
import com.mstan.shop.dashboard.dto.TopProductResponse;
import com.mstan.shop.dashboard.service.DashboardService;
import com.mstan.shop.product.dto.LowStockItemResponse;
import com.mstan.shop.product.service.ProductService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.List;

@Tag(name = "Admin - Dashboard", description = "KPIs et graphiques agrégés, réservé aux administrateurs")
@RestController
@RequestMapping("/api/admin/dashboard")
@RequiredArgsConstructor
public class DashboardAdminController {

    private final DashboardService dashboardService;
    private final ProductService productService;

    @Value("${app.stock.low-threshold}")
    private int lowStockThreshold;

    @GetMapping("/summary")
    public DashboardSummaryResponse summary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        long lowStockCount = productService.getLowStockItems(lowStockThreshold).size();
        return dashboardService.summary(from, to, lowStockCount);
    }

    @GetMapping("/revenue-timeseries")
    public List<RevenuePointResponse> revenueTimeseries(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        return dashboardService.revenueTimeseries(from, to);
    }

    @GetMapping("/orders-by-status")
    public List<OrderStatusCountResponse> ordersByStatus() {
        return dashboardService.ordersByStatus();
    }

    @GetMapping("/payments-breakdown")
    public List<PaymentBreakdownResponse> paymentsBreakdown(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to
    ) {
        return dashboardService.paymentsBreakdown(from, to);
    }

    @GetMapping("/top-products")
    public List<TopProductResponse> topProducts(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return dashboardService.topProducts(from, to, limit);
    }

    @GetMapping("/top-categories")
    public List<TopCategoryResponse> topCategories(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to,
            @RequestParam(defaultValue = "10") int limit
    ) {
        return dashboardService.topCategories(from, to, limit);
    }

    @GetMapping("/low-stock")
    public List<LowStockItemResponse> lowStock() {
        return productService.getLowStockItems(lowStockThreshold);
    }

    @GetMapping("/promo-usage")
    public List<PromoUsageResponse> promoUsage() {
        return dashboardService.promoUsage();
    }
}
