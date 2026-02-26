package com.badmintonshop.controller.api;

import com.badmintonshop.dto.wishlist.WishlistItemDTO;
import com.badmintonshop.security.CustomOAuth2User;
import com.badmintonshop.security.CustomUserDetails;
import com.badmintonshop.service.WishlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API Controller for Wishlist
 */
@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishlistController {

    private final WishlistService wishlistService;

    /**
     * Helper method to get current user ID from both regular and OAuth2 login
     */
    private Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated()) {
            Object principal = auth.getPrincipal();
            if (principal instanceof CustomUserDetails) {
                return ((CustomUserDetails) principal).getUserId();
            }
            if (principal instanceof CustomOAuth2User) {
                return ((CustomOAuth2User) principal).getUserId();
            }
        }
        return null;
    }

    /**
     * Get user's wishlist
     * GET /api/wishlist
     */
    @GetMapping
    public ResponseEntity<Page<WishlistItemDTO>> getWishlist(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {

        Long userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by("addedAt").descending());
        Page<WishlistItemDTO> wishlist = wishlistService.getUserWishlist(userId, pageable);
        
        return ResponseEntity.ok(wishlist);
    }

    /**
     * Get all wishlist items (no pagination)
     * GET /api/wishlist/all
     */
    @GetMapping("/all")
    public ResponseEntity<List<WishlistItemDTO>> getAllWishlistItems() {

        Long userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<WishlistItemDTO> items = wishlistService.getAllUserWishlistItems(userId);
        return ResponseEntity.ok(items);
    }

    /**
     * Add product to wishlist
     * POST /api/wishlist/{productId}
     */
    @PostMapping("/{productId}")
    public ResponseEntity<WishlistItemDTO> addToWishlist(@PathVariable Long productId) {

        Long userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        WishlistItemDTO item = wishlistService.addToWishlist(userId, productId);
        return ResponseEntity.status(HttpStatus.CREATED).body(item);
    }

    /**
     * Remove product from wishlist
     * DELETE /api/wishlist/{productId}
     */
    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> removeFromWishlist(@PathVariable Long productId) {

        Long userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        wishlistService.removeFromWishlist(userId, productId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Toggle product in wishlist (add if not exists, remove if exists)
     * POST /api/wishlist/{productId}/toggle
     */
    @PostMapping("/{productId}/toggle")
    public ResponseEntity<Map<String, Object>> toggleWishlist(@PathVariable Long productId) {

        Long userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        boolean inWishlist = wishlistService.toggleWishlist(userId, productId);
        return ResponseEntity.ok(Map.of(
                "inWishlist", inWishlist,
                "message", inWishlist ? "Đã thêm vào yêu thích" : "Đã xóa khỏi yêu thích"
        ));
    }

    /**
     * Check if product is in wishlist
     * GET /api/wishlist/check/{productId}
     */
    @GetMapping("/check/{productId}")
    public ResponseEntity<Map<String, Boolean>> checkWishlist(@PathVariable Long productId) {

        Long userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.ok(Map.of("inWishlist", false, "loggedIn", false));
        }

        boolean inWishlist = wishlistService.isInWishlist(userId, productId);
        return ResponseEntity.ok(Map.of("inWishlist", inWishlist, "loggedIn", true));
    }

    /**
     * Get wishlist count
     * GET /api/wishlist/count
     */
    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getWishlistCount() {

        Long userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.ok(Map.of("count", 0L));
        }

        long count = wishlistService.getWishlistCount(userId);
        return ResponseEntity.ok(Map.of("count", count));
    }

    /**
     * Clear wishlist
     * DELETE /api/wishlist
     */
    @DeleteMapping
    public ResponseEntity<Void> clearWishlist() {

        Long userId = getCurrentUserId();
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        wishlistService.clearWishlist(userId);
        return ResponseEntity.noContent().build();
    }
}
