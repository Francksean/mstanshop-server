package com.mstan.shop.category.repository;

import com.mstan.shop.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {

    boolean existsByNameIgnoreCase(String name);

    Optional<Category> findBySlug(String slug);

    List<Category> findByParentId(UUID parentId);

    boolean existsByParentId(UUID parentId);

    @Query("select c.id from Category c where c.parent.id in :parentIds")
    List<UUID> findIdsByParentIn(@Param("parentIds") List<UUID> parentIds);
}
