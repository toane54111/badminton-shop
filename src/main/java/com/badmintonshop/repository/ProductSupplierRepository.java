package com.badmintonshop.repository;

import com.badmintonshop.entity.ProductSupplier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for ProductSupplier entity (many-to-many relationship)
 */
@Repository
public interface ProductSupplierRepository extends JpaRepository<ProductSupplier, Long> {

    /**
     * Find suppliers for a product
     */
    List<ProductSupplier> findByProductProductId(Long productId);

    /**
     * Find products for a supplier
     */
    List<ProductSupplier> findBySupplierSupplierId(Long supplierId);

    /**
     * Find specific product-supplier relationship
     */
    Optional<ProductSupplier> findByProductProductIdAndSupplierSupplierId(Long productId, Long supplierId);

    /**
     * Check if relationship exists
     */
    boolean existsByProductProductIdAndSupplierSupplierId(Long productId, Long supplierId);

    /**
     * Delete by product and supplier
     */
    void deleteByProductProductIdAndSupplierSupplierId(Long productId, Long supplierId);

    /**
     * Find preferred supplier for product
     */
    @Query("SELECT ps FROM ProductSupplier ps WHERE ps.product.productId = :productId AND ps.isPreferred = true")
    Optional<ProductSupplier> findPreferredSupplierForProduct(@Param("productId") Long productId);
}
