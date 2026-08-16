package com.mstan.shop.auth.dto;

import com.mstan.shop.cart.dto.AddCartItemRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record LoginRequest(

        @NotBlank(message = "L'email est obligatoire")
        @Email(message = "L'email doit être valide")
        String email,

        @NotBlank(message = "Le mot de passe est obligatoire")
        String password,

        @Schema(description = "Panier local (invité) à fusionner dans le panier du compte une fois connecté. "
                + "Optionnel — les lignes invalides (produit/variante introuvable, stock insuffisant) sont "
                + "ignorées individuellement sans faire échouer la connexion.")
        @Valid
        List<AddCartItemRequest> cartItems
) {
}
