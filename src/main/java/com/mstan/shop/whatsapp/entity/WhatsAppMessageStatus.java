package com.mstan.shop.whatsapp.entity;

public enum WhatsAppMessageStatus {
    PENDING,
    SENT,
    DELIVERED,
    READ,
    FAILED;

    /**
     * Rang de progression logique (sent < delivered < read) utilisé pour ignorer un statut
     * webhook arrivé en désordre qui ferait régresser l'état — FAILED est terminal, hors échelle.
     */
    public int progressRank() {
        return switch (this) {
            case PENDING -> 0;
            case SENT -> 1;
            case DELIVERED -> 2;
            case READ -> 3;
            case FAILED -> -1;
        };
    }
}
