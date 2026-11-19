package com.mstan.shop.whatsapp.service;

import com.mstan.shop.whatsapp.dto.GenukaTemplateResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class GenukaTemplateClient {

    private final RestClient restClient;

    public GenukaTemplateClient(
            @Value("${app.notification.genuka.base-url}") String baseUrl,
            @Value("${app.notification.genuka.api-key}") String apiKey
    ) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .build();
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
                .body(new ParameterizedTypeReference<List<GenukaTemplateResponse>>() {});
    }
}
