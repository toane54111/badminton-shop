package com.badmintonshop.repository;

import com.badmintonshop.entity.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

/**
 * Repository for Inventory entity with atomic operations
 */
@Repository
public interface InventoryRepository extends JpaRepository<Inventory, Long> {

    /**
     * Find inventory by product ID
     */
    List<Inventory> findByProductProductId(Long productId);

    /**
     * Find inventory by variant ID
     */
    Optional<Inventory> findByVariantVariantId(Long variantId);

    /**
     * Find inventory by product ID with lock for atomic updates
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Inventory i WHERE i.product.productId = :productId")
    List<Inventory> findByProductIdForUpdate(@Param("productId") Long productId);

    /**
     * Find low stock inventory
     */
    @Query("SELECT i FROM Inventory i WHERE i.quantityAvailable <= i.lowStockThreshold")
    List<Inventory> findLowStock();

    /**
     * Find low stock with pagination
     */
    @Query("SELECT i FROM Inventory i WHERE i.quantityAvailable <= i.lowStockThreshold")
    Page<Inventory> findLowStock(Pageable pageable);

    /**
     * Find out of stock inventory
     */
    @Query("SELECT i FROM Inventory i WHERE i.quantityAvailable <= 0")
    List<Inventory> findOutOfStock();

    /**
     * Update quantity atomically (for race condition handling)
     */
    @Modifying
    @Query("UPDATE Inventory i SET i.quantityAvailable = i.quantityAvailable + :adjustment WHERE i.inventoryId = :inventoryId AND i.quantityAvailable + :adjustment >= 0")
    int adjustQuantity(@Param("inventoryId") Long inventoryId, @Param("adjustment") int adjustment);

    /**
     * Set quantity directly
     */
    @Modifying
    @Query("UPDATE Inventory i SET i.quantityAvailable = :quantity WHERE i.inventoryId = :inventoryId")
    void setQuantity(@Param("inventoryId") Long inventoryId, @Param("quantity") int quantity);

    /**
     * Count low stock items
     */
    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.quantityAvailable <= i.lowStockThreshold")
    long countLowStock();

    /**
     * Count out of stock items
     */
    @Query("SELECT COUNT(i) FROM Inventory i WHERE i.quantityAvailable <= 0")
    long countOutOfStock();

    /**
     * Search inventory with filters
     */
    @Query("SELECT i FROM Inventory i WHERE " +
            "(:productId IS NULL OR i.product.productId = :productId) " +
            "AND (:lowStock IS NULL OR (:lowStock = true AND i.quantityAvailable <= i.lowStockThreshold))")
    Page<Inventory> searchInventory(@Param("productId") Long productId,
            @Param("lowStock") Boolean lowStock,
            Pageable pageable);
}
