package com.mstan.shop.whatsapp.entity;

import com.mstan.shop.common.util.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "whatsapp_templates")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class WhatsAppTemplate extends BaseEntity {

    @Column(nullable = false)
    private String genukaTemplateId;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String language;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WhatsAppTemplateCategory category;

    @Enumerated(EnumType.STRING)
    private WhatsAppTemplateEventCode eventCode;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String components;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WhatsAppTemplateStatus status;
}
