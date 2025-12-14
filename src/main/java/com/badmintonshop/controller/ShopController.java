package com.badmintonshop.controller;

import com.badmintonshop.dto.product.BrandDTO;
import com.badmintonshop.dto.product.CategoryTreeDTO;
import com.badmintonshop.dto.product.ProductListDTO;
import com.badmintonshop.dto.product.ProductResponse;
import com.badmintonshop.entity.enums.ProductType;
import com.badmintonshop.service.BrandService;
import com.badmintonshop.service.CategoryService;
import com.badmintonshop.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controller for visitor-facing shop pages
 * URLs: /products, /brands, /categories (public access)
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class ShopController {

    private final ProductService productService;
    private final BrandService brandService;
    private final CategoryService categoryService;

    /**
     * Products listing page with filters
     * GET /products
     */
    @GetMapping("/products")
    public String productsPage(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Long brandId,
            @RequestParam(required = false) ProductType productType,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Model model) {

        log.info("Loading products page: keyword={}, category={}, brand={}", keyword, categoryId, brandId);

        Sort sort = sortDir.equalsIgnoreCase("asc")
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ProductListDTO> products = productService.searchProductsAdvanced(
                keyword, categoryId, brandId, productType,
                minPrice, maxPrice, null, true, pageable);

        // For filters
        List<CategoryTreeDTO> categories = categoryService.getCategoryTree();
        List<BrandDTO> brands = brandService.getAllActiveBrands();

        model.addAttribute("products", products);
        model.addAttribute("categories", categories);
        model.addAttribute("brands", brands);
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("brandId", brandId);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);

        return "shop/products";
    }

    /**
     * Product detail page
     * GET /products/{slug}
     */
    @GetMapping("/products/{slug}")
    public String productDetailPage(@PathVariable String slug, Model model) {
        log.info("Loading product detail: slug={}", slug);

        return productService.getProductBySlug(slug)
                .map(product -> {
                    productService.incrementViewCount(product.getProductId());
                    model.addAttribute("product", product);

                    // Related products
                    if (product.getCategoryId() != null) {
                        Pageable pageable = PageRequest.of(0, 4);
                        Page<ProductListDTO> related = productService.getProductsByCategory(product.getCategoryId(),
                                pageable);
                        model.addAttribute("relatedProducts", related.getContent());
                    }

                    return "shop/product-detail";
                })
                .orElse("error/404");
    }

    /**
     * Featured products page
     * GET /products/featured
     */
    @GetMapping("/products/featured")
    public String featuredProductsPage(Model model) {
        log.info("Loading featured products page");
        model.addAttribute("products", productService.getFeaturedProducts(20));
        model.addAttribute("pageTitle", "Sản phẩm nổi bật");
        return "shop/product-list";
    }

    /**
     * New arrivals page
     * GET /products/new-arrivals
     */
    @GetMapping("/products/new-arrivals")
    public String newArrivalsPage(Model model) {
        log.info("Loading new arrivals page");
        model.addAttribute("products", productService.getNewArrivals(20));
        model.addAttribute("pageTitle", "Sản phẩm mới");
        return "shop/product-list";
    }

    /**
     * Best sellers page
     * GET /products/best-sellers
     */
    @GetMapping("/products/best-sellers")
    public String bestSellersPage(Model model) {
        log.info("Loading best sellers page");
        model.addAttribute("products", productService.getBestSellers(20));
        model.addAttribute("pageTitle", "Bán chạy nhất");
        return "shop/product-list";
    }

    /**
     * Search products page
     * GET /products/search?q=...
     */
    @GetMapping("/products/search")
    public String searchProductsPage(
            @RequestParam("q") String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model) {

        log.info("Searching products: q={}", query);

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<ProductListDTO> products = productService.searchProducts(query, pageable);

        model.addAttribute("products", products);
        model.addAttribute("query", query);
        model.addAttribute("pageTitle", "Kết quả tìm kiếm: " + query);

        return "shop/search-results";
    }

    /**
     * Brands listing page
     * GET /brands
     */
    @GetMapping("/brands")
    public String brandsPage(Model model) {
        log.info("Loading brands page");
        model.addAttribute("brands", brandService.getAllActiveBrands());
        return "shop/brands";
    }

    /**
     * Categories page (tree structure)
     * GET /categories
     */
    @GetMapping("/categories")
    public String categoriesPage(Model model) {
        log.info("Loading categories page");
        model.addAttribute("categories", categoryService.getCategoryTree());
        return "shop/categories";
    }
}
