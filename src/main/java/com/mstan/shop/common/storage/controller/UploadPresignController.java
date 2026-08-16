package com.mstan.shop.common.storage.controller;

import com.mstan.shop.common.storage.dto.PresignUploadRequest;
import com.mstan.shop.common.storage.dto.PresignedUploadResponse;
import com.mstan.shop.common.storage.service.UploadPresignService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Admin - Uploads", description = "Génération d'URLs présignées pour upload direct vers R2, réservé aux administrateurs")
@RestController
@RequestMapping("/api/admin/uploads/presign")
@RequiredArgsConstructor
public class UploadPresignController {

    private final UploadPresignService uploadPresignService;

    @PostMapping
    public PresignedUploadResponse presign(@Valid @RequestBody PresignUploadRequest request) {
        return uploadPresignService.presign(request.scope(), request.entityId(), request.fileName(), request.contentType());
    }
}
