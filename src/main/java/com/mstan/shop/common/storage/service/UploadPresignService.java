package com.mstan.shop.common.storage.service;

import com.mstan.shop.common.storage.dto.PresignedUploadResponse;
import com.mstan.shop.common.storage.dto.UploadScope;
import com.mstan.shop.common.util.FileNameUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UploadPresignService {

    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/png", "image/jpeg", "image/webp", "image/gif"
    );

    private final R2StorageService r2StorageService;

    public PresignedUploadResponse presign(UploadScope scope, UUID entityId, String fileName, String contentType) {
        if (!ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Type de contenu non autorisé pour un upload d'image : " + contentType);
        }

        String objectKey = buildObjectKey(scope, entityId, fileName);
        return r2StorageService.createPresignedUpload(objectKey, contentType);
    }

    private String buildObjectKey(UploadScope scope, UUID entityId, String fileName) {
        String sanitized = FileNameUtil.sanitize(fileName);
        String uuid = UUID.randomUUID().toString();
        return switch (scope) {
            case PRODUCT_IMAGE -> "products/%s/%s_%s".formatted(entityId, uuid, sanitized);
            case CATEGORY_BANNER -> "categories/%s/banner_%s_%s".formatted(entityId, uuid, sanitized);
            case CATEGORY_THUMBNAIL -> "categories/%s/thumbnail_%s_%s".formatted(entityId, uuid, sanitized);
        };
    }
}
