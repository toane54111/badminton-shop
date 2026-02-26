package com.badmintonshop.repository;

import com.badmintonshop.entity.CouponUsage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CouponUsageRepository extends JpaRepository<CouponUsage, Long> {

    // Count uses by user for a specific coupon
    long countByCouponCouponIdAndUserUserId(Long couponId, Long userId);

    // Check if user already used this coupon
    boolean existsByCouponCouponIdAndUserUserId(Long couponId, Long userId);

    // Find usage history by coupon
    Page<CouponUsage> findByCouponCouponId(Long couponId, Pageable pageable);
}
