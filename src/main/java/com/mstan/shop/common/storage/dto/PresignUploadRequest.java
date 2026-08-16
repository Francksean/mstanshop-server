package com.mstan.shop.common.storage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record PresignUploadRequest(

        @NotNull(message = "Le type d'upload est obligatoire")
        UploadScope scope,

        @NotNull(message = "L'identifiant de l'entité est obligatoire")
        UUID entityId,

        @NotBlank(message = "Le nom du fichier est obligatoire")
        String fileName,

        @NotBlank(message = "Le type de contenu est obligatoire")
        String contentType
) {
}
