package com.badmintonshop.service;

import com.badmintonshop.dto.product.ProductListDTO;
import com.badmintonshop.dto.product.ProductRequest;
import com.badmintonshop.dto.product.ProductResponse;
import com.badmintonshop.entity.Brand;
import com.badmintonshop.entity.Category;
import com.badmintonshop.entity.Product;
import com.badmintonshop.entity.ProductImage;
import com.badmintonshop.entity.enums.ProductStatus;
import com.badmintonshop.entity.enums.ProductType;
import com.badmintonshop.repository.BrandRepository;
import com.badmintonshop.repository.CategoryRepository;
import com.badmintonshop.repository.ProductImageRepository;
import com.badmintonshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for Product operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductImageRepository productImageRepository;
    private final PromotionPriceService promotionPriceService;

    /**
     * Get all products with pagination (with promotion prices applied)
     */
    public Page<ProductListDTO> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable)
                .map(this::mapToProductListDTO);
    }

    /**
     * Get product by ID
     */
    public Optional<ProductResponse> getProductById(Long id) {
        return productRepository.findByIdWithImages(id)
                .map(this::mapToProductResponse);
    }

    /**
     * Get product by slug
     */
    public Optional<ProductResponse> getProductBySlug(String slug) {
        return productRepository.findBySlug(slug)
                .map(this::mapToProductResponse);
    }

    /**
     * Get featured products
     */
    public List<ProductListDTO> getFeaturedProducts(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return productRepository.findFeaturedProducts(pageable)
                .getContent().stream()
                .map(this::mapToProductListDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get new arrivals
     */
    public List<ProductListDTO> getNewArrivals(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return productRepository.findNewArrivals(pageable)
                .getContent().stream()
                .map(this::mapToProductListDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get best sellers
     */
    public List<ProductListDTO> getBestSellers(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return productRepository.findBestSellers(pageable)
                .getContent().stream()
                .map(this::mapToProductListDTO)
                .collect(Collectors.toList());
    }

    /**
     * Get products on sale
     */
    public Page<ProductListDTO> getProductsOnSale(Pageable pageable) {
        return productRepository.findProductsOnSale(pageable)
                .map(this::mapToProductListDTO);
    }

    /**
     * Search products by keyword
     */
    public Page<ProductListDTO> searchProducts(String keyword, Pageable pageable) {
        return productRepository.searchByKeyword(keyword, pageable)
                .map(this::mapToProductListDTO);
    }

    /**
     * Advanced search with filters
     */
    public Page<ProductListDTO> searchProductsAdvanced(
            String keyword,
            Long categoryId,
            Long brandId,
            ProductType productType,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            ProductStatus status,
            Boolean isPublished,
            Pageable pageable) {
        return productRepository.searchProducts(
                keyword, categoryId, brandId, productType,
                minPrice, maxPrice, status, pageable)
                .map(this::mapToProductListDTO);
    }

    /**
     * Get products by category
     */
    public Page<ProductListDTO> getProductsByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findByCategoryId(categoryId, pageable)
                .map(this::mapToProductListDTO);
    }

    /**
     * Get products by brand
     */
    public Page<ProductListDTO> getProductsByBrand(Long brandId, Pageable pageable) {
        return productRepository.findByBrandId(brandId, pageable)
                .map(this::mapToProductListDTO);
    }

    /**
     * Create new product
     */
    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        // Generate slug
        String slug = request.getSlug();
        if (slug == null || slug.isEmpty()) {
            slug = generateSlug(request.getName());
        }

        // Check slug uniqueness
        if (productRepository.existsBySlug(slug)) {
            slug = slug + "-" + System.currentTimeMillis();
        }

        // Check SKU uniqueness
        if (productRepository.existsBySku(request.getSku())) {
            throw new IllegalArgumentException("SKU đã tồn tại: " + request.getSku());
        }

        // Get category
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục: " + request.getCategoryId()));

        // Get brand (optional)
        Brand brand = null;
        if (request.getBrandId() != null) {
            brand = brandRepository.findById(request.getBrandId())
                    .orElseThrow(
                            () -> new IllegalArgumentException("Không tìm thấy thương hiệu: " + request.getBrandId()));
        }

        Product product = Product.builder()
                .name(request.getName())
                .slug(slug)
                .sku(request.getSku())
                .category(category)
                .brand(brand)
                .productType(request.getProductType())
                .shortDescription(request.getShortDescription())
                .fullDescription(request.getFullDescription())
                .basePrice(request.getBasePrice())
                .compareAtPrice(request.getCompareAtPrice())
                .costPrice(request.getCostPrice())
                .weight(request.getWeight())
                .dimensions(request.getDimensions())
                .racketFlexibility(request.getRacketFlexibility())
                .racketBalancePoint(request.getRacketBalancePoint())
                .racketShaftDiameter(request.getRacketShaftDiameter())
                .racketFrameShape(request.getRacketFrameShape())
                .racketRecommendedTension(request.getRacketRecommendedTension())
                .racketMaxTension(request.getRacketMaxTension())
                .racketMaterial(request.getRacketMaterial())
                .status(request.getStatus() != null ? request.getStatus() : ProductStatus.DRAFT)
                .isFeatured(request.getIsFeatured() != null ? request.getIsFeatured() : false)
                .isNewArrival(request.getIsNewArrival() != null ? request.getIsNewArrival() : false)
                .isBestSeller(request.getIsBestSeller() != null ? request.getIsBestSeller() : false)
                .displayOrder(request.getDisplayOrder() != null ? request.getDisplayOrder() : 0)
                .metaTitle(request.getMetaTitle())
                .metaDescription(request.getMetaDescription())
                .metaKeywords(request.getMetaKeywords())
                .build();

        product = productRepository.save(product);

        // Add images if provided
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            for (int i = 0; i < request.getImageUrls().size(); i++) {
                String imageUrl = request.getImageUrls().get(i);
                ProductImage image = ProductImage.builder()
                        .product(product)
                        .imageUrl(imageUrl)
                        .displayOrder(i)
                        .isPrimary(i == 0 || imageUrl.equals(request.getPrimaryImageUrl()))
                        .build();
                productImageRepository.save(image);
            }
        }

        log.info("Created product: {} ({})", product.getName(), product.getSku());
        return ProductResponse.fromEntity(productRepository.findByIdWithImages(product.getProductId()).orElse(product));
    }

    /**
     * Update product
     */
    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm: " + id));

        // Update basic fields
        if (request.getName() != null)
            product.setName(request.getName());
        if (request.getSlug() != null && !request.getSlug().isEmpty()) {
            if (!product.getSlug().equals(request.getSlug()) && productRepository.existsBySlug(request.getSlug())) {
                throw new IllegalArgumentException("Slug đã tồn tại: " + request.getSlug());
            }
            product.setSlug(request.getSlug());
        }
        if (request.getSku() != null) {
            if (!product.getSku().equals(request.getSku()) && productRepository.existsBySku(request.getSku())) {
                throw new IllegalArgumentException("SKU đã tồn tại: " + request.getSku());
            }
            product.setSku(request.getSku());
        }

        // Update category
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(
                            () -> new IllegalArgumentException("Không tìm thấy danh mục: " + request.getCategoryId()));
            product.setCategory(category);
        }

        // Update brand
        if (request.getBrandId() != null) {
            Brand brand = brandRepository.findById(request.getBrandId())
                    .orElseThrow(
                            () -> new IllegalArgumentException("Không tìm thấy thương hiệu: " + request.getBrandId()));
            product.setBrand(brand);
        }

        // Update other fields
        if (request.getProductType() != null)
            product.setProductType(request.getProductType());
        if (request.getShortDescription() != null)
            product.setShortDescription(request.getShortDescription());
        if (request.getFullDescription() != null)
            product.setFullDescription(request.getFullDescription());
        if (request.getBasePrice() != null)
            product.setBasePrice(request.getBasePrice());
        if (request.getCompareAtPrice() != null)
            product.setCompareAtPrice(request.getCompareAtPrice());
        if (request.getCostPrice() != null)
            product.setCostPrice(request.getCostPrice());
        if (request.getWeight() != null)
            product.setWeight(request.getWeight());
        if (request.getDimensions() != null)
            product.setDimensions(request.getDimensions());
        if (request.getRacketFlexibility() != null)
            product.setRacketFlexibility(request.getRacketFlexibility());
        if (request.getRacketBalancePoint() != null)
            product.setRacketBalancePoint(request.getRacketBalancePoint());
        if (request.getRacketShaftDiameter() != null)
            product.setRacketShaftDiameter(request.getRacketShaftDiameter());
        if (request.getRacketFrameShape() != null)
            product.setRacketFrameShape(request.getRacketFrameShape());
        if (request.getRacketRecommendedTension() != null)
            product.setRacketRecommendedTension(request.getRacketRecommendedTension());
        if (request.getRacketMaxTension() != null)
            product.setRacketMaxTension(request.getRacketMaxTension());
        if (request.getRacketMaterial() != null)
            product.setRacketMaterial(request.getRacketMaterial());
        if (request.getStatus() != null)
            product.setStatus(request.getStatus());
        if (request.getIsFeatured() != null)
            product.setIsFeatured(request.getIsFeatured());
        if (request.getIsNewArrival() != null)
            product.setIsNewArrival(request.getIsNewArrival());
        if (request.getIsBestSeller() != null)
            product.setIsBestSeller(request.getIsBestSeller());
        if (request.getDisplayOrder() != null)
            product.setDisplayOrder(request.getDisplayOrder());
        if (request.getMetaTitle() != null)
            product.setMetaTitle(request.getMetaTitle());
        if (request.getMetaDescription() != null)
            product.setMetaDescription(request.getMetaDescription());
        if (request.getMetaKeywords() != null)
            product.setMetaKeywords(request.getMetaKeywords());

        product = productRepository.save(product);
        log.info("Updated product: {} ({})", product.getName(), product.getSku());
        return ProductResponse.fromEntity(product);
    }

    /**
     * Delete product (soft delete)
     */
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm: " + id));

        product.setDeletedAt(LocalDateTime.now());
        product.setStatus(ProductStatus.DISCONTINUED);
        productRepository.save(product);
        log.info("Soft deleted product: {} ({})", product.getName(), product.getSku());
    }

    /**
     * Increment view count
     */
    @Transactional
    public void incrementViewCount(Long productId) {
        productRepository.incrementViewCount(productId);
    }

    /**
     * Get low stock products
     */
    public List<ProductListDTO> getLowStockProducts(int threshold) {
        return productRepository.findLowStockProducts(threshold).stream()
                .map(this::mapToProductListDTO)
                .collect(Collectors.toList());
    }

    /**
     * Generate slug from name
     */
    private String generateSlug(String name) {
        if (name == null)
            return "";
        return name.toLowerCase()
                .replaceAll("[àáạảãâầấậẩẫăằắặẳẵ]", "a")
                .replaceAll("[èéẹẻẽêềếệểễ]", "e")
                .replaceAll("[ìíịỉĩ]", "i")
                .replaceAll("[òóọỏõôồốộổỗơờớợởỡ]", "o")
                .replaceAll("[ùúụủũưừứựửữ]", "u")
                .replaceAll("[ỳýỵỷỹ]", "y")
                .replaceAll("đ", "d")
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .replaceAll("^-|-$", "");
    }

    /**
     * Get deleted products for trash
     */
    public Page<ProductListDTO> getDeletedProducts(Pageable pageable) {
        return productRepository.findDeleted(pageable)
                .map(this::mapToProductListDTO);
    }

    // ==================== PROMOTION PRICE MAPPING ====================

    /**
     * Map Product entity to ProductListDTO with promotion prices applied
     */
    private ProductListDTO mapToProductListDTO(Product product) {
        ProductListDTO dto = ProductListDTO.fromEntity(product);
        if (dto == null) return null;
        
        applyPromotionToProductListDTO(dto);
        return dto;
    }

    /**
     * Map Product entity to ProductResponse with promotion prices applied
     */
    private ProductResponse mapToProductResponse(Product product) {
        ProductResponse dto = ProductResponse.fromEntity(product);
        if (dto == null) return null;
        
        applyPromotionToProductResponse(dto);
        return dto;
    }

    /**
     * Apply promotion pricing to ProductListDTO
     */
    private void applyPromotionToProductListDTO(ProductListDTO dto) {
        Long productId = dto.getProductId();
        Long categoryId = dto.getCategoryId();
        BigDecimal basePrice = dto.getBasePrice();

        if (promotionPriceService.hasActivePromotion(productId, categoryId)) {
            dto.setHasActivePromotion(true);
            dto.setPromotionPrice(promotionPriceService.calculatePromotionPrice(basePrice, productId, categoryId));
            dto.setPromotionDiscount(promotionPriceService.calculatePromotionDiscount(basePrice, productId, categoryId));
            
            PromotionPriceService.PromotionInfo info = promotionPriceService.getAppliedPromotionInfo(productId, categoryId);
            if (info != null) {
                dto.setPromotionName(info.discountText());
            }
        } else {
            dto.setHasActivePromotion(false);
        }
    }

    /**
     * Apply promotion pricing to ProductResponse
     */
    private void applyPromotionToProductResponse(ProductResponse dto) {
        Long productId = dto.getProductId();
        Long categoryId = dto.getCategoryId();
        BigDecimal basePrice = dto.getBasePrice();

        if (promotionPriceService.hasActivePromotion(productId, categoryId)) {
            dto.setHasActivePromotion(true);
            dto.setPromotionPrice(promotionPriceService.calculatePromotionPrice(basePrice, productId, categoryId));
            dto.setPromotionDiscount(promotionPriceService.calculatePromotionDiscount(basePrice, productId, categoryId));
            
            PromotionPriceService.PromotionInfo info = promotionPriceService.getAppliedPromotionInfo(productId, categoryId);
            if (info != null) {
                dto.setPromotionName(info.discountText());
            }
        } else {
            dto.setHasActivePromotion(false);
        }
    }

    /**
     * Restore product from trash
     */
    @Transactional
    public void restoreProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm: " + id));

        product.setDeletedAt(null);
        product.setStatus(ProductStatus.DRAFT);
        productRepository.save(product);
        log.info("Restored product: {} ({})", product.getName(), product.getSku());
    }

    /**
     * Hard delete product (permanently)
     */
    @Transactional
    public void hardDeleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm: " + id));

        // Delete related images first
        productImageRepository.deleteByProductId(product.getProductId());

        // Delete the product
        productRepository.delete(product);
        log.info("Hard deleted product: {} ({})", product.getName(), product.getSku());
    }

    public List<ProductListDTO> getAllActiveProductsBasicInfo() {
        return productRepository.findAllActive().stream()
                .map(p -> ProductListDTO.builder()
                        .productId(p.getProductId())
                        .name(p.getName())
                        .build())
                .collect(Collectors.toList());
    }
}
