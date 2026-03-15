package com.ecommerce.controller;

import com.ecommerce.dto.ProductDto;
import com.ecommerce.service.ProductService;
import com.ecommerce.utils.PaginationUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private static final Logger logger =
            LoggerFactory.getLogger(ProductController.class);

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    /**
     * ADMIN: Create a new product
     * Client sends ProductDto which is converted to Entity inside Service layer.
     */
    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@RequestBody ProductDto productDto) {

        logger.info("Received request to create product: {}", productDto.getName());

        ProductDto createdProduct = productService.createProduct(productDto);

        logger.info("Product created successfully with ID {}", createdProduct.getId());

        return ResponseEntity.ok(createdProduct);
    }

    /**
     * CUSTOMER: Get products with pagination and optional filters
     * Filters supported:
     * - category
     * - price range
     * - sorting
     */
    @GetMapping
    public ResponseEntity<Page<ProductDto>> getProducts(
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {

        logger.debug("Fetching products with filters - category: {}, minPrice: {}, maxPrice: {}",
                category, minPrice, maxPrice);

        Pageable pageable = PaginationUtil.createPageable(page, size, sortBy, sortDir);

        Page<ProductDto> products =
                productService.filterProducts(category, minPrice, maxPrice, pageable);

        return ResponseEntity.ok(products);
    }

    /**
     * CUSTOMER: Search products by keyword
     */
    @GetMapping("/search")
    public ResponseEntity<Page<ProductDto>> searchProducts(
            @RequestParam String keyword,
            Pageable pageable) {

        logger.debug("Searching products using keyword: {}", keyword);

        Page<ProductDto> products = productService.searchProducts(keyword, pageable);

        return ResponseEntity.ok(products);
    }

    /**
     * Fetch single product by ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<ProductDto> getProduct(@PathVariable Long id) {

        logger.debug("Fetching product with ID {}", id);

        ProductDto product = productService.getProductById(id);

        return ResponseEntity.ok(product);
    }

    /**
     * ADMIN: Update product details
     */
    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductDto productDto) {

        logger.info("Update request received for product ID {}", id);

        ProductDto updatedProduct = productService.updateProduct(id, productDto);

        logger.info("Product updated successfully with ID {}", id);

        return ResponseEntity.ok(updatedProduct);
    }

    /**
     * ADMIN: Delete product
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteProduct(@PathVariable Long id) {

        logger.warn("Delete request received for product ID {}", id);

        productService.deleteProduct(id);

        logger.warn("Product deleted successfully with ID {}", id);

        return ResponseEntity.ok("Product deleted successfully");
    }
}