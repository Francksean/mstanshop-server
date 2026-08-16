package com.mstan.shop.whatsapp.entity;

import com.mstan.shop.common.util.BaseEntity;
import com.mstan.shop.order.entity.Order;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "whatsapp_messages")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class WhatsAppMessage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private String phoneNumber;

    @Column(nullable = false)
    private String templateName;

    private String providerMessageId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WhatsAppMessageStatus status;

    @Column(length = 1000)
    private String errorMessage;
}
