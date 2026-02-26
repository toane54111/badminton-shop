package com.badmintonshop.repository;

import com.badmintonshop.entity.Wishlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, Long> {

    // Find wishlist items by user with pagination
    Page<Wishlist> findByUserUserId(Long userId, Pageable pageable);
    
    // Find all wishlist items by user
    List<Wishlist> findByUserUserId(Long userId);
    
    // Check if product is in user's wishlist
    boolean existsByUserUserIdAndProductProductId(Long userId, Long productId);
    
    // Find specific wishlist item
    Optional<Wishlist> findByUserUserIdAndProductProductId(Long userId, Long productId);
    
    // Delete by user and product
    void deleteByUserUserIdAndProductProductId(Long userId, Long productId);
    
    // Count items in user's wishlist
    long countByUserUserId(Long userId);
    
    // Find wishlist with product details
    @Query("SELECT w FROM Wishlist w JOIN FETCH w.product p LEFT JOIN FETCH p.images WHERE w.user.userId = :userId ORDER BY w.addedAt DESC")
    List<Wishlist> findByUserWithProducts(@Param("userId") Long userId);
}
