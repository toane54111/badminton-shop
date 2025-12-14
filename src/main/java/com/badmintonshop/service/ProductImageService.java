package com.badmintonshop.service;

import com.badmintonshop.dto.product.ProductImageDTO;
import com.badmintonshop.entity.Product;
import com.badmintonshop.entity.ProductImage;
import com.badmintonshop.repository.ProductImageRepository;
import com.badmintonshop.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Service for Product Image operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class ProductImageService {

    private final ProductImageRepository productImageRepository;
    private final ProductRepository productRepository;

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    @Value("${app.upload.allowed-extensions:jpg,jpeg,png,gif,webp}")
    private String allowedExtensions;

    /**
     * Get images for a product
     */
    public List<ProductImageDTO> getImagesByProductId(Long productId) {
        return productImageRepository.findByProductIdOrderByDisplayOrder(productId).stream()
                .map(ProductImageDTO::fromEntity)
                .collect(Collectors.toList());
    }

    /**
     * Add image to product
     */
    @Transactional
    public ProductImageDTO addImage(Long productId, String imageUrl, String altText, boolean isPrimary) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm: " + productId));

        // Get next display order
        Integer maxOrder = productImageRepository.findMaxDisplayOrder(productId);
        int nextOrder = (maxOrder != null ? maxOrder : 0) + 1;

        // If setting as primary, reset other primary images
        if (isPrimary) {
            productImageRepository.resetPrimaryForProduct(productId);
        }

        ProductImage image = ProductImage.builder()
                .product(product)
                .imageUrl(imageUrl)
                .altText(altText)
                .displayOrder(nextOrder)
                .isPrimary(isPrimary)
                .createdAt(LocalDateTime.now())
                .build();

        image = productImageRepository.save(image);
        log.info("Added image to product {}: {}", productId, imageUrl);
        return ProductImageDTO.fromEntity(image);
    }

    /**
     * Upload and add image
     */
    @Transactional
    public ProductImageDTO uploadImage(Long productId, MultipartFile file, String altText, boolean isPrimary)
            throws IOException {
        // Validate file
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File không được để trống");
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            throw new IllegalArgumentException("Tên file không hợp lệ");
        }

        // Check extension
        String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();
        if (!allowedExtensions.contains(extension)) {
            throw new IllegalArgumentException("Định dạng file không được hỗ trợ: " + extension);
        }

        // Generate unique filename
        String filename = UUID.randomUUID().toString() + "." + extension;
        String subDir = "products/" + productId;
        Path uploadPath = Paths.get(uploadDir, subDir);

        // Create directory if not exists
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Save file
        Path filePath = uploadPath.resolve(filename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        String imageUrl = "/" + uploadDir + "/" + subDir + "/" + filename;
        return addImage(productId, imageUrl, altText, isPrimary);
    }

    /**
     * Delete image
     */
    @Transactional
    public void deleteImage(Long productId, Long imageId) {
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hình ảnh: " + imageId));

        if (!image.getProduct().getProductId().equals(productId)) {
            throw new IllegalArgumentException("Hình ảnh không thuộc sản phẩm này");
        }

        productImageRepository.delete(image);
        log.info("Deleted image {} from product {}", imageId, productId);
    }

    /**
     * Set image as primary
     */
    @Transactional
    public void setPrimaryImage(Long productId, Long imageId) {
        if (!productImageRepository.existsByImageIdAndProductProductId(imageId, productId)) {
            throw new IllegalArgumentException("Hình ảnh không thuộc sản phẩm này");
        }

        // Reset all primary images for this product
        productImageRepository.resetPrimaryForProduct(productId);

        // Set new primary
        ProductImage image = productImageRepository.findById(imageId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hình ảnh: " + imageId));
        image.setIsPrimary(true);
        productImageRepository.save(image);

        log.info("Set image {} as primary for product {}", imageId, productId);
    }

    /**
     * Reorder images
     */
    @Transactional
    public void reorderImages(Long productId, List<Long> imageIds) {
        for (int i = 0; i < imageIds.size(); i++) {
            Long imageId = imageIds.get(i);
            if (!productImageRepository.existsByImageIdAndProductProductId(imageId, productId)) {
                throw new IllegalArgumentException("Hình ảnh " + imageId + " không thuộc sản phẩm này");
            }
            productImageRepository.updateDisplayOrder(imageId, i);
        }
        log.info("Reordered {} images for product {}", imageIds.size(), productId);
    }

    /**
     * Delete all images for a product
     */
    @Transactional
    public void deleteAllImagesForProduct(Long productId) {
        productImageRepository.deleteByProductId(productId);
        log.info("Deleted all images for product {}", productId);
    }
}
