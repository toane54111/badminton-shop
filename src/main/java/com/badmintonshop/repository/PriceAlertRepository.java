package com.badmintonshop.repository;

import com.badmintonshop.entity.PriceAlert;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface PriceAlertRepository extends JpaRepository<PriceAlert, Long> {

    // Find by user
    List<PriceAlert> findByUserUserIdAndIsActiveTrue(Long userId);

    // Find by product (active alerts for triggering)
    List<PriceAlert> findByProductProductIdAndIsActiveTrueAndIsTriggeredFalse(Long productId);

    // Check if user already has alert for product
    Optional<PriceAlert> findByUserUserIdAndProductProductIdAndIsActiveTrue(Long userId, Long productId);

    // Find alerts that should be triggered
    @Query("SELECT p FROM PriceAlert p WHERE p.product.productId = :productId AND p.isActive = true AND p.isTriggered = false AND p.targetPrice >= :newPrice")
    List<PriceAlert> findAlertsThatShouldTrigger(@Param("productId") Long productId,
            @Param("newPrice") BigDecimal newPrice);

    // Count active alerts by user
    long countByUserUserIdAndIsActiveTrue(Long userId);

    // Find all triggered alerts for a product
    List<PriceAlert> findByProductProductIdAndIsTriggeredTrue(Long productId);
}
