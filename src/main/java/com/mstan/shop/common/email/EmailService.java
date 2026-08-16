package com.mstan.shop.common.email;

import com.mstan.shop.common.util.CurrencyFormatter;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.mail.from}")
    private String from;

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    @Value("${app.backend-base-url}")
    private String backendBaseUrl;

    @Async
    public void sendRegistrationConfirmation(String to, String firstName) {
        Context context = new Context();
        context.setVariable("firstName", firstName);
        context.setVariable("shopUrl", frontendBaseUrl);

        sendHtml(to, "Bienvenue sur MSTAN Shop", "email/registration-confirmation", context);
    }

    @Async
    public void sendPasswordResetLink(String to, String resetToken) {
        Context context = new Context();
        context.setVariable("resetUrl", frontendBaseUrl + "/reset-password?token=" + resetToken);

        sendHtml(to, "Réinitialisation de votre mot de passe", "email/password-reset", context);
    }

    @Async
    public void sendOrderConfirmation(String to, OrderEmailData order) {
        Context context = new Context();
        context.setVariable("orderNumber", order.orderNumber());
        context.setVariable("items", toItemViews(order.items()));
        context.setVariable("subtotalFormatted", CurrencyFormatter.format(order.subtotalAmount()));
        context.setVariable("discountFormatted",
                order.discountAmount() != null ? CurrencyFormatter.format(order.discountAmount()) : null);
        context.setVariable("promoCode", order.promoCode());
        context.setVariable("totalFormatted", CurrencyFormatter.format(order.totalAmount()));
        context.setVariable("addressStreet", order.addressStreet());
        context.setVariable("addressCity", order.addressCity());
        context.setVariable("addressPostalCode", order.addressPostalCode());
        context.setVariable("addressCountry", order.addressCountry());
        context.setVariable("orderUrl", frontendBaseUrl + "/orders/" + order.orderId());

        sendHtml(to, "Confirmation de votre commande " + order.orderNumber(), "email/order-confirmation", context);
    }

    @Async
    public void sendOrderCancellation(String to, OrderEmailData order) {
        Context context = new Context();
        context.setVariable("orderNumber", order.orderNumber());
        context.setVariable("items", toItemViews(order.items()));
        context.setVariable("totalFormatted", CurrencyFormatter.format(order.totalAmount()));
        context.setVariable("ordersUrl", frontendBaseUrl + "/orders");

        sendHtml(to, "Annulation de votre commande " + order.orderNumber(), "email/order-cancellation", context);
    }

    @Async
    public void sendPaymentFailed(String to, String orderNumber) {
        Context context = new Context();
        context.setVariable("orderNumber", orderNumber);
        context.setVariable("retryUrl", frontendBaseUrl + "/cart");

        sendHtml(to, "Échec du paiement de votre commande " + orderNumber, "email/payment-failed", context);
    }

    @Async
    public void sendLowStockAlert(String to, List<LowStockEmailItem> items) {
        Context context = new Context();
        context.setVariable("items", items);
        context.setVariable("adminUrl", frontendBaseUrl + "/admin/products");

        sendHtml(to, "Alerte stock bas", "email/low-stock-alert", context);
    }

    private List<ItemView> toItemViews(List<OrderEmailItem> items) {
        return items.stream()
                .map(item -> new ItemView(
                        item.name(),
                        item.colorLabel(),
                        item.thumbnailUrl() != null ? resolveAbsoluteUrl(item.thumbnailUrl()) : null,
                        item.quantity(),
                        CurrencyFormatter.format(item.unitPrice()),
                        CurrencyFormatter.format(item.lineTotal())
                ))
                .toList();
    }

    /** Images are stored as relative /uploads/... paths; emails need an absolute URL to render in a mail client. */
    private String resolveAbsoluteUrl(String relativeOrAbsoluteUrl) {
        if (relativeOrAbsoluteUrl.startsWith("http://") || relativeOrAbsoluteUrl.startsWith("https://")) {
            return relativeOrAbsoluteUrl;
        }
        return backendBaseUrl + relativeOrAbsoluteUrl;
    }

    private void sendHtml(String to, String subject, String templateName, Context context) {
        try {
            String html = templateEngine.process(templateName, context);

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(html, true);

            mailSender.send(message);
            log.info("Email envoyé à {} : {}", to, subject);
        } catch (Exception e) {
            log.error("Échec de l'envoi de l'email à {} : {}", to, subject, e);
        }
    }

    private record ItemView(
            String name,
            String colorLabel,
            String thumbnailUrl,
            int quantity,
            String unitPriceFormatted,
            String lineTotalFormatted
    ) {
    }
}
