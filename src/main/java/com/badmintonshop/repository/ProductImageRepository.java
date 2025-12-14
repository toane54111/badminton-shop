package com.badmintonshop.repository;

import com.badmintonshop.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ProductImage entity
 */
@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Long> {

    /**
     * Find images by product ID ordered by display order
     */
    @Query("SELECT pi FROM ProductImage pi WHERE pi.product.productId = :productId ORDER BY pi.displayOrder ASC")
    List<ProductImage> findByProductIdOrderByDisplayOrder(@Param("productId") Long productId);

    /**
     * Find primary image for a product
     */
    @Query("SELECT pi FROM ProductImage pi WHERE pi.product.productId = :productId AND pi.isPrimary = true")
    Optional<ProductImage> findPrimaryImageByProductId(@Param("productId") Long productId);

    /**
     * Count images by product
     */
    long countByProductProductId(Long productId);

    /**
     * Delete all images by product ID
     */
    @Modifying
    @Query("DELETE FROM ProductImage pi WHERE pi.product.productId = :productId")
    void deleteByProductId(@Param("productId") Long productId);

    /**
     * Set image as primary (reset all others)
     */
    @Modifying
    @Query("UPDATE ProductImage pi SET pi.isPrimary = false WHERE pi.product.productId = :productId")
    void resetPrimaryForProduct(@Param("productId") Long productId);

    /**
     * Update display order for an image
     */
    @Modifying
    @Query("UPDATE ProductImage pi SET pi.displayOrder = :displayOrder WHERE pi.imageId = :imageId")
    void updateDisplayOrder(@Param("imageId") Long imageId, @Param("displayOrder") Integer displayOrder);

    /**
     * Find max display order for a product
     */
    @Query("SELECT COALESCE(MAX(pi.displayOrder), 0) FROM ProductImage pi WHERE pi.product.productId = :productId")
    Integer findMaxDisplayOrder(@Param("productId") Long productId);

    /**
     * Check if image belongs to product
     */
    boolean existsByImageIdAndProductProductId(Long imageId, Long productId);
}
