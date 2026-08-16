package com.mstan.shop.common.notification;

import com.twilio.Twilio;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TwilioConfig {

    @Value("${app.notification.twilio.account-sid}")
    private String accountSid;

    @Value("${app.notification.twilio.auth-token}")
    private String authToken;

    @PostConstruct
    public void init() {
        if (accountSid == null || accountSid.isBlank() || authToken == null || authToken.isBlank()) {
            log.warn("Identifiants Twilio non configurés — l'envoi de SMS/WhatsApp échouera tant que app.notification.twilio.* n'est pas renseigné");
            return;
        }
        Twilio.init(accountSid, authToken);
    }
}
