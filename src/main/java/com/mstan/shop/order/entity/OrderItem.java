package com.mstan.shop.order.entity;

import com.mstan.shop.common.util.BaseEntity;
import com.mstan.shop.product.entity.Product;
import com.mstan.shop.product.entity.ProductVariant;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class OrderItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** Purely informational; display code must use the snapshot columns below, never this live reference. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "variant_id")
    private ProductVariant variant;

    /** Snapshot of the product name at order time, in case the product is later renamed/removed. */
    @Column(nullable = false)
    private String productName;

    /** Snapshot of the product thumbnail URL at order time, in case the product's images change/are removed later. */
    private String productThumbnailUrl;

    /** Snapshot of the chosen variant's color at order time, in case the variant is later removed. */
    private String variantColorName;

    private String variantColorHex;

    /** Snapshot of the chosen variant's size at order time, in case the variant is later removed. */
    private String variantSize;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private int quantity;
}
