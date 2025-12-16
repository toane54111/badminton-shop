package com.badmintonshop.repository;

import com.badmintonshop.entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CartItemRepository extends JpaRepository<CartItem, Long> {

    // Find items by cart
    List<CartItem> findByCartCartId(Long cartId);
    
    // Find item by cart and product
    Optional<CartItem> findByCartCartIdAndProductProductId(Long cartId, Long productId);
    
    // Find item by cart, product and variant
    Optional<CartItem> findByCartCartIdAndProductProductIdAndVariantVariantId(Long cartId, Long productId, Long variantId);
    
    // Find item by cart and product (no variant)
    @Query("SELECT ci FROM CartItem ci WHERE ci.cart.cartId = :cartId AND ci.product.productId = :productId AND ci.variant IS NULL")
    Optional<CartItem> findByCartAndProductNoVariant(@Param("cartId") Long cartId, @Param("productId") Long productId);
    
    // Count items in cart
    long countByCartCartId(Long cartId);
    
    // Delete all items in cart
    void deleteByCartCartId(Long cartId);
    
    // Calculate total quantity in cart
    @Query("SELECT COALESCE(SUM(ci.quantity), 0) FROM CartItem ci WHERE ci.cart.cartId = :cartId")
    int getTotalQuantity(@Param("cartId") Long cartId);
}
