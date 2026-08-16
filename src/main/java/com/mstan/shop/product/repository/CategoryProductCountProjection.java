package com.mstan.shop.product.repository;

import java.util.UUID;

public interface CategoryProductCountProjection {

    UUID getCategoryId();

    Long getCnt();
}
