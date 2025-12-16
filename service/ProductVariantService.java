package com.badmintonshop.service;

import com.badmintonshop.dto.product.ProductVariantDTO;
import com.badmintonshop.entity.Product;
import com.badmintonshop.entity.ProductVariant;
import com.badmintonshop.entity.enums.VariantStatus;
import com.badmintonshop.repository.ProductRepository;
import com.badmintonshop.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service for Product Variant operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductVariantService {

    private final ProductVariantRepository productVariantRepository;
    private final ProductRepository productRepository;

    /**
     * Get variants for a product
     */
    public List<ProductVariantDTO> getVariantsByProductId(Long productId) {
        return productVariantRepository.findByProductProductIdOrderByVariantIdAsc(productId).stream()
                .map(ProductVariantDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get active variants for a product
     */
    public List<ProductVariantDTO> getActiveVariantsByProductId(Long productId) {
        return productVariantRepository.findActiveVariantsByProductId(productId).stream()
                .map(ProductVariantDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Get variant by ID
     */
    public Optional<ProductVariantDTO> getVariantById(Long variantId) {
        return productVariantRepository.findById(variantId)
                .map(ProductVariantDTO::fromEntity);
    }

    /**
     * Get variant by SKU
     */
    public Optional<ProductVariantDTO> getVariantBySku(String sku) {
        return productVariantRepository.findBySku(sku)
                .map(ProductVariantDTO::fromEntity);
    }

    /**
     * Create new variant
     */
    @Transactional
    public ProductVariantDTO createVariant(Long productId, ProductVariantDTO dto) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm: " + productId));

        // Check SKU uniqueness
        if (productVariantRepository.existsBySku(dto.getSku())) {
            throw new IllegalArgumentException("SKU đã tồn tại: " + dto.getSku());
        }

        ProductVariant variant = ProductVariant.builder()
                .product(product)
                .sku(dto.getSku())
                .barcode(dto.getBarcode())
                .attributes(dto.getAttributes())
                .variantName(dto.getVariantName())
                .priceAdjustment(dto.getPriceAdjustment() != null ? dto.getPriceAdjustment() : BigDecimal.ZERO)
                .imageUrl(dto.getImageUrl())
                .status(dto.getStatus() != null ? dto.getStatus() : VariantStatus.ACTIVE)
                .build();

        variant = productVariantRepository.save(variant);
        log.info("Created variant {} for product {}", variant.getSku(), productId);
        return ProductVariantDTO.fromEntity(variant);
    }

    /**
     * Update variant
     */
    @Transactional
    public ProductVariantDTO updateVariant(Long productId, Long variantId, ProductVariantDTO dto) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy biến thể: " + variantId));

        if (!variant.getProduct().getProductId().equals(productId)) {
            throw new IllegalArgumentException("Biến thể không thuộc sản phẩm này");
        }

        // Check SKU if changed
        if (dto.getSku() != null && !variant.getSku().equals(dto.getSku())) {
            if (productVariantRepository.existsBySku(dto.getSku())) {
                throw new IllegalArgumentException("SKU đã tồn tại: " + dto.getSku());
            }
            variant.setSku(dto.getSku());
        }

        if (dto.getBarcode() != null)
            variant.setBarcode(dto.getBarcode());
        if (dto.getAttributes() != null)
            variant.setAttributes(dto.getAttributes());
        if (dto.getVariantName() != null)
            variant.setVariantName(dto.getVariantName());
        if (dto.getPriceAdjustment() != null)
            variant.setPriceAdjustment(dto.getPriceAdjustment());
        if (dto.getImageUrl() != null)
            variant.setImageUrl(dto.getImageUrl());
        if (dto.getStatus() != null)
            variant.setStatus(dto.getStatus());

        variant = productVariantRepository.save(variant);
        log.info("Updated variant {} for product {}", variant.getSku(), productId);
        return ProductVariantDTO.fromEntity(variant);
    }

    /**
     * Delete variant (soft delete)
     */
    @Transactional
    public void deleteVariant(Long productId, Long variantId) {
        ProductVariant variant = productVariantRepository.findById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy biến thể: " + variantId));

        if (!variant.getProduct().getProductId().equals(productId)) {
            throw new IllegalArgumentException("Biến thể không thuộc sản phẩm này");
        }

        variant.setDeletedAt(LocalDateTime.now());
        variant.setStatus(VariantStatus.INACTIVE);
        productVariantRepository.save(variant);
        log.info("Soft deleted variant {} from product {}", variantId, productId);
    }

    /**
     * Hard delete variant
     */
    @Transactional
    public void hardDeleteVariant(Long productId, Long variantId) {
        if (!productVariantRepository.existsByVariantIdAndProductProductId(variantId, productId)) {
            throw new IllegalArgumentException("Biến thể không thuộc sản phẩm này");
        }
        productVariantRepository.deleteById(variantId);
        log.info("Hard deleted variant {} from product {}", variantId, productId);
    }
}
