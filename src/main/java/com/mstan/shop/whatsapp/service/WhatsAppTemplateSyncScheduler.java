package com.mstan.shop.whatsapp.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WhatsAppTemplateSyncScheduler {

    private final WhatsAppTemplateService whatsAppTemplateService;

    @Scheduled(cron = "${app.whatsapp.template-sync-cron:0 0 * * * *}")
    public void syncTemplates() {
        try {
            whatsAppTemplateService.syncFromGenuka();
        } catch (Exception e) {
            log.error("Échec de la synchronisation des templates WhatsApp depuis Genuka", e);
        }
    }
}
