package com.badmintonshop.controller.api;

import com.badmintonshop.dto.banner.BannerDTO;
import com.badmintonshop.entity.enums.BannerPosition;
import com.badmintonshop.service.BannerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API Controller for Banners (Public - for guests and users)
 */
@RestController
@RequestMapping("/api/banners")
@RequiredArgsConstructor
@Slf4j
public class BannerController {

    private final BannerService bannerService;

    /**
     * Get displayable banners by position
     * GET /api/banners?position=HOME_SLIDER
     */
    @GetMapping
    public ResponseEntity<List<BannerDTO>> getBannersByPosition(
            @RequestParam(required = false) BannerPosition position) {
        
        List<BannerDTO> banners;
        if (position != null) {
            banners = bannerService.getDisplayableBannersByPosition(position);
            log.debug("Returning {} banners for position {}", banners.size(), position);
        } else {
            banners = bannerService.getAllDisplayableBanners();
            log.debug("Returning {} banners (all positions)", banners.size());
        }
        
        return ResponseEntity.ok(banners);
    }

    /**
     * Get banner by ID
     * GET /api/banners/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<BannerDTO> getBannerById(@PathVariable Long id) {
        return bannerService.getBannerById(id)
                .filter(b -> b.getIsActive()) // Only return active banners publicly
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
