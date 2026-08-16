package com.mstan.shop.category.entity;

import com.mstan.shop.common.util.BaseEntity;
import com.mstan.shop.promo.entity.PromoDiscountType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "categories", uniqueConstraints = @UniqueConstraint(columnNames = "slug"))
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class Category extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(length = 1000)
    private String description;

    private String bannerUrl;

    private String bannerObjectKey;

    private String thumbnailUrl;

    private String thumbnailObjectKey;

    @Enumerated(EnumType.STRING)
    private PromoDiscountType promoDiscountType;

    private BigDecimal promoDiscountValue;

    private Instant promoStartsAt;

    private Instant promoEndsAt;

    @Column(nullable = false)
    private boolean promoActive = false;
}
