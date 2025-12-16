package com.badmintonshop.repository;

import com.badmintonshop.entity.ProductVariant;
import com.badmintonshop.entity.enums.VariantStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ProductVariant entity
 */
@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {

    /**
     * Find variants by product ID
     */
    List<ProductVariant> findByProductProductIdOrderByVariantIdAsc(Long productId);

    /**
     * Find active variants by product ID
     */
    @Query("SELECT v FROM ProductVariant v WHERE v.product.productId = :productId AND v.status = 'ACTIVE'")
    List<ProductVariant> findActiveVariantsByProductId(@Param("productId") Long productId);

    /**
     * Find variant by SKU
     */
    Optional<ProductVariant> findBySku(String sku);

    /**
     * Check if SKU exists
     */
    boolean existsBySku(String sku);

    /**
     * Find variants by status
     */
    List<ProductVariant> findByStatus(VariantStatus status);

    /**
     * Count variants by product
     */
    long countByProductProductId(Long productId);

    /**
     * Check if variant belongs to product
     */
    boolean existsByVariantIdAndProductProductId(Long variantId, Long productId);

    /**
     * Delete variants by product ID
     */
    void deleteByProductProductId(Long productId);
}
