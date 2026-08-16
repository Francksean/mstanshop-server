package com.mstan.shop.whatsapp.service;

import com.mstan.shop.whatsapp.dto.GenukaCreateTemplateRequest;
import com.mstan.shop.whatsapp.dto.GenukaTemplateResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class GenukaTemplateClient {

    private final RestClient restClient;
    private final String connectionId;

    public GenukaTemplateClient(
            @Value("${app.notification.genuka.base-url}") String baseUrl,
            @Value("${app.notification.genuka.api-key}") String apiKey,
            @Value("${app.notification.genuka.connection-id}") String connectionId
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
        this.connectionId = connectionId;
    }

    public GenukaTemplateResponse create(String name, String language, String category, com.fasterxml.jackson.databind.JsonNode components) {
        GenukaCreateTemplateRequest request = new GenukaCreateTemplateRequest(
                connectionId, name, language, category, null, components
        );
        return restClient.post()
                .uri("/templates")
                .body(request)
                .retrieve()
                .body(GenukaTemplateResponse.class);
    }

    public GenukaTemplateResponse get(String genukaTemplateId) {
        return restClient.get()
                .uri("/templates/{id}", genukaTemplateId)
                .retrieve()
                .body(GenukaTemplateResponse.class);
    }

    public List<GenukaTemplateResponse> list() {
        return restClient.get()
                .uri("/templates")
                .retrieve()
                .body(new org.springframework.core.ParameterizedTypeReference<List<GenukaTemplateResponse>>() {});
    }

    public void delete(String genukaTemplateId) {
        restClient.delete()
                .uri("/templates/{id}", genukaTemplateId)
                .retrieve()
                .toBodilessEntity();
    }
}
