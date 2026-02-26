package com.badmintonshop.repository;

import com.badmintonshop.entity.RestockNotification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RestockNotificationRepository extends JpaRepository<RestockNotification, Long> {

    // Find by user
    List<RestockNotification> findByUserUserIdAndIsSentFalse(Long userId);

    // Find by product (for triggering when restocked)
    List<RestockNotification> findByProductProductIdAndIsSentFalse(Long productId);

    // Find by product and variant
    List<RestockNotification> findByProductProductIdAndVariantVariantIdAndIsSentFalse(Long productId, Long variantId);

    // Check if user already subscribed
    Optional<RestockNotification> findByUserUserIdAndProductProductIdAndVariantVariantIdAndIsSentFalse(
            Long userId, Long productId, Long variantId);

    // Find by user and product (any variant)
    Optional<RestockNotification> findByUserUserIdAndProductProductIdAndVariantIsNullAndIsSentFalse(
            Long userId, Long productId);

    // Count subscriptions by user
    long countByUserUserIdAndIsSentFalse(Long userId);
}
