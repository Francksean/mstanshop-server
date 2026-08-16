package com.mstan.shop.stock;

import com.mstan.shop.common.email.EmailService;
import com.mstan.shop.common.email.LowStockEmailItem;
import com.mstan.shop.product.dto.LowStockItemResponse;
import com.mstan.shop.product.service.ProductService;
import com.mstan.shop.user.entity.RoleName;
import com.mstan.shop.user.entity.User;
import com.mstan.shop.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class StockAlertScheduler {

    private final ProductService productService;
    private final UserRepository userRepository;
    private final EmailService emailService;

    @Value("${app.stock.low-threshold}")
    private int lowStockThreshold;

    @Scheduled(cron = "${app.stock.alert-cron:0 0 8 * * *}")
    public void sendDailyLowStockDigest() {
        List<LowStockItemResponse> items = productService.getLowStockItems(lowStockThreshold);
        if (items.isEmpty()) {
            return;
        }

        List<User> admins = userRepository.findByRoles_Name(RoleName.ROLE_ADMIN);
        if (admins.isEmpty()) {
            log.warn("Aucun administrateur trouvé pour envoyer l'alerte de stock bas");
            return;
        }

        List<LowStockEmailItem> emailItems = items.stream()
                .map(item -> new LowStockEmailItem(item.productName(), item.variantColorName(), item.currentStock(), item.threshold()))
                .toList();

        admins.forEach(admin -> emailService.sendLowStockAlert(admin.getEmail(), emailItems));
    }
}
