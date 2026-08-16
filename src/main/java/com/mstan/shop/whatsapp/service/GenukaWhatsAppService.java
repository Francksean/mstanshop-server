package com.mstan.shop.whatsapp.service;

import com.mstan.shop.order.entity.Order;
import com.mstan.shop.whatsapp.dto.GenukaSendMessageRequest;
import com.mstan.shop.whatsapp.dto.GenukaSendMessageResponse;
import com.mstan.shop.whatsapp.dto.GenukaTemplatePayload;
import com.mstan.shop.whatsapp.entity.WhatsAppMessage;
import com.mstan.shop.whatsapp.entity.WhatsAppMessageStatus;
import com.mstan.shop.whatsapp.entity.WhatsAppTemplate;
import com.mstan.shop.whatsapp.entity.WhatsAppTemplateEventCode;
import com.mstan.shop.whatsapp.entity.WhatsAppTemplateStatus;
import com.mstan.shop.whatsapp.repository.WhatsAppMessageRepository;
import com.mstan.shop.whatsapp.repository.WhatsAppTemplateRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Slf4j
@Service
public class GenukaWhatsAppService {

    private final WhatsAppMessageRepository whatsAppMessageRepository;
    private final WhatsAppTemplateRepository whatsAppTemplateRepository;
    private final RestClient restClient;
    private final String connectionId;

    public GenukaWhatsAppService(
            WhatsAppMessageRepository whatsAppMessageRepository,
            WhatsAppTemplateRepository whatsAppTemplateRepository,
            @Value("${app.notification.genuka.base-url}") String baseUrl,
            @Value("${app.notification.genuka.api-key}") String apiKey,
            @Value("${app.notification.genuka.connection-id}") String connectionId
    ) {
        this.whatsAppMessageRepository = whatsAppMessageRepository;
        this.whatsAppTemplateRepository = whatsAppTemplateRepository;
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
        this.connectionId = connectionId;
    }

    @Async
    public void sendOrderConfirmation(Order order, String phone, String totalFormatted) {
        send(order, phone, WhatsAppTemplateEventCode.ORDER_CONFIRMATION, List.of(order.getOrderNumber(), totalFormatted));
    }

    @Async
    public void sendPaymentFailed(Order order, String phone) {
        send(order, phone, WhatsAppTemplateEventCode.PAYMENT_FAILED, List.of(order.getOrderNumber()));
    }

    @Async
    public void sendOrderCancellation(Order order, String phone) {
        send(order, phone, WhatsAppTemplateEventCode.ORDER_CANCELLATION, List.of(order.getOrderNumber()));
    }

    private void send(Order order, String phone, WhatsAppTemplateEventCode eventCode, List<String> variables) {
        WhatsAppTemplate template = whatsAppTemplateRepository.findByEventCode(eventCode).orElse(null);

        if (template == null || template.getStatus() != WhatsAppTemplateStatus.APPROVED) {
            WhatsAppMessage message = new WhatsAppMessage();
            message.setOrder(order);
            message.setPhoneNumber(phone);
            message.setTemplateName(template != null ? template.getName() : eventCode.name());
            message.setStatus(WhatsAppMessageStatus.FAILED);
            message.setErrorMessage("Aucun template WhatsApp approuvé pour l'événement " + eventCode);
            whatsAppMessageRepository.save(message);
            log.warn("Envoi WhatsApp {} annulé pour {} : aucun template approuvé", eventCode, phone);
            return;
        }

        send(order, phone, template.getName(), template.getLanguage(), variables);
    }

    private void send(Order order, String phone, String templateName, String language, List<String> variables) {
        WhatsAppMessage message = new WhatsAppMessage();
        message.setOrder(order);
        message.setPhoneNumber(phone);
        message.setTemplateName(templateName);
        message.setStatus(WhatsAppMessageStatus.PENDING);
        whatsAppMessageRepository.save(message);

        try {
            GenukaSendMessageRequest request = new GenukaSendMessageRequest(
                    connectionId,
                    phone,
                    new GenukaTemplatePayload(templateName, language, variables)
            );

            GenukaSendMessageResponse response = restClient.post()
                    .uri("/messages")
                    .body(request)
                    .retrieve()
                    .body(GenukaSendMessageResponse.class);

            message.setProviderMessageId(response != null ? response.id() : null);
            message.setStatus(WhatsAppMessageStatus.SENT);
            log.info("WhatsApp (Genuka) envoyé à {} via le template {}", phone, templateName);
        } catch (Exception e) {
            message.setStatus(WhatsAppMessageStatus.FAILED);
            message.setErrorMessage(e.getMessage());
            log.error("Échec de l'envoi WhatsApp (Genuka) à {} via le template {}", phone, templateName, e);
        }

        whatsAppMessageRepository.save(message);
    }
}
