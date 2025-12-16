package com.badmintonshop.service;

import com.badmintonshop.dto.wishlist.WishlistItemDTO;
import com.badmintonshop.entity.*;
import com.badmintonshop.exception.ResourceNotFoundException;
import com.badmintonshop.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    /**
     * Get user's wishlist with pagination
     */
    @Transactional(readOnly = true)
    public Page<WishlistItemDTO> getUserWishlist(Long userId, Pageable pageable) {
        Page<Wishlist> wishlist = wishlistRepository.findByUserUserId(userId, pageable);
        return wishlist.map(this::mapToDTO);
    }

    /**
     * Get all wishlist items for a user
     */
    @Transactional(readOnly = true)
    public List<WishlistItemDTO> getAllUserWishlistItems(Long userId) {
        List<Wishlist> wishlist = wishlistRepository.findByUserWithProducts(userId);
        return wishlist.stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    /**
     * Add product to wishlist
     */
    public WishlistItemDTO addToWishlist(Long userId, Long productId) {
        // Check if already in wishlist
        if (wishlistRepository.existsByUserUserIdAndProductProductId(userId, productId)) {
            throw new IllegalStateException("Product is already in your wishlist");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found"));

        Wishlist wishlist = Wishlist.builder()
                .user(user)
                .product(product)
                .build();

        wishlist = wishlistRepository.save(wishlist);
        log.info("Added product {} to wishlist for user {}", productId, userId);
        
        return mapToDTO(wishlist);
    }

    /**
     * Remove product from wishlist
     */
    public void removeFromWishlist(Long userId, Long productId) {
        Wishlist wishlist = wishlistRepository.findByUserUserIdAndProductProductId(userId, productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not in wishlist"));
        
        wishlistRepository.delete(wishlist);
        log.info("Removed product {} from wishlist for user {}", productId, userId);
    }

    /**
     * Toggle product in wishlist (add if not exists, remove if exists)
     */
    public boolean toggleWishlist(Long userId, Long productId) {
        if (wishlistRepository.existsByUserUserIdAndProductProductId(userId, productId)) {
            removeFromWishlist(userId, productId);
            return false; // Removed
        } else {
            addToWishlist(userId, productId);
            return true; // Added
        }
    }

    /**
     * Check if product is in user's wishlist
     */
    @Transactional(readOnly = true)
    public boolean isInWishlist(Long userId, Long productId) {
        return wishlistRepository.existsByUserUserIdAndProductProductId(userId, productId);
    }

    /**
     * Get wishlist count for user
     */
    @Transactional(readOnly = true)
    public long getWishlistCount(Long userId) {
        return wishlistRepository.countByUserUserId(userId);
    }

    /**
     * Clear user's wishlist
     */
    public void clearWishlist(Long userId) {
        List<Wishlist> items = wishlistRepository.findByUserUserId(userId);
        wishlistRepository.deleteAll(items);
        log.info("Cleared wishlist for user {}", userId);
    }

    // Helper method to map entity to DTO
    private WishlistItemDTO mapToDTO(Wishlist wishlist) {
        Product product = wishlist.getProduct();
        ProductImage primaryImage = product.getPrimaryImage();
        
        return WishlistItemDTO.builder()
                .wishlistId(wishlist.getWishlistId())
                .productId(product.getProductId())
                .productName(product.getName())
                .productSlug(product.getSlug())
                .productImage(primaryImage != null ? primaryImage.getImageUrl() : null)
                .basePrice(product.getBasePrice())
                .compareAtPrice(product.getCompareAtPrice())
                .discountPercentage(product.getDiscountPercentage())
                .brandName(product.getBrand() != null ? product.getBrand().getName() : null)
                .inStock(product.isAvailable())
                .notes(wishlist.getNotes())
                .addedAt(wishlist.getAddedAt())
                .build();
    }
}
