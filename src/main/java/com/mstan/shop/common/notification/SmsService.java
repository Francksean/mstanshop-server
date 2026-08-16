package com.mstan.shop.common.notification;

import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class SmsService {

    @Value("${app.notification.twilio.sms-from}")
    private String smsFrom;

    @Async
    public void sendOrderConfirmation(String toPhone, String orderNumber, String totalFormatted) {
        send(toPhone, "MSTAN Shop : commande %s confirmée, total %s. Merci pour votre confiance !"
                .formatted(orderNumber, totalFormatted));
    }

    @Async
    public void sendPaymentFailed(String toPhone, String orderNumber) {
        send(toPhone, "MSTAN Shop : le paiement de votre commande %s a échoué. Vous pouvez réessayer depuis votre compte."
                .formatted(orderNumber));
    }

    @Async
    public void sendOrderCancellation(String toPhone, String orderNumber) {
        send(toPhone, "MSTAN Shop : votre commande %s a été annulée, le stock a été recrédité."
                .formatted(orderNumber));
    }

    private void send(String to, String body) {
        try {
            Message.creator(new PhoneNumber(to), new PhoneNumber(smsFrom), body).create();
            log.info("SMS envoyé à {}", to);
        } catch (Exception e) {
            log.error("Échec de l'envoi du SMS à {}", to, e);
        }
    }
}
