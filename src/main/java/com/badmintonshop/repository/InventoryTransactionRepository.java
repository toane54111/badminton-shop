package com.badmintonshop.repository;

import com.badmintonshop.entity.InventoryTransaction;
import com.badmintonshop.entity.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository for InventoryTransaction entity
 */
@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, Long> {

        /**
         * Find transactions by product ID
         */
        List<InventoryTransaction> findByProductProductIdOrderByCreatedAtDesc(Long productId);

        /**
         * Find transactions by variant ID
         */
        List<InventoryTransaction> findByVariantVariantIdOrderByCreatedAtDesc(Long variantId);

        /**
         * Find transactions by type
         */
        List<InventoryTransaction> findByTransactionType(TransactionType type);

        /**
         * Find transactions within date range
         */
        @Query("SELECT t FROM InventoryTransaction t WHERE t.createdAt BETWEEN :startDate AND :endDate ORDER BY t.createdAt DESC")
        List<InventoryTransaction> findByDateRange(@Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        /**
         * Find transactions with pagination
         */
        @Query("SELECT t FROM InventoryTransaction t ORDER BY t.createdAt DESC")
        Page<InventoryTransaction> findAllOrderByCreatedAtDesc(Pageable pageable);

        /**
         * Search transactions with filters
         */
        @Query("SELECT t FROM InventoryTransaction t WHERE " +
                        "(:productId IS NULL OR t.product.productId = :productId) " +
                        "AND (:transactionType IS NULL OR t.transactionType = :transactionType) " +
                        "AND (:startDate IS NULL OR t.createdAt >= :startDate) " +
                        "AND (:endDate IS NULL OR t.createdAt <= :endDate) " +
                        "ORDER BY t.createdAt DESC")
        Page<InventoryTransaction> searchTransactions(
                        @Param("productId") Long productId,
                        @Param("transactionType") TransactionType transactionType,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate,
                        Pageable pageable);
}
