package com.mstan.shop.supplier.repository;

import com.mstan.shop.supplier.entity.Supplier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SupplierRepository extends JpaRepository<Supplier, UUID> {

    boolean existsByNameIgnoreCase(String name);
}
