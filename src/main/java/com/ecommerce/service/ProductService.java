package com.ecommerce.service;

import com.ecommerce.dto.ProductDto;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.ProductRepository;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class ProductService {

    private static final Logger logger =
            LoggerFactory.getLogger(ProductService.class);

    private final ProductRepository productRepository;
    private final ModelMapper modelMapper;

    public ProductService(ProductRepository productRepository,
                          ModelMapper modelMapper) {
        this.productRepository = productRepository;
        this.modelMapper = modelMapper;
    }

    /**
     * ADMIN: Create new product
     */
    public ProductDto createProduct(ProductDto productDto) {

        logger.info("Creating new product: {}", productDto.getName());

        Product product = convertToEntity(productDto);

        Product savedProduct = productRepository.save(product);

        logger.info("Product created successfully with ID {}", savedProduct.getId());

        return convertToDto(savedProduct);
    }

    /**
     * Fetch product by ID
     */
    public ProductDto getProductById(Long id) {

        logger.debug("Fetching product with ID {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Product not found with ID {}", id);
                    return new ResourceNotFoundException("Product not found with id " + id);
                });

        return convertToDto(product);
    }

    /**
     * Filter products by category and/or price
     */
    public Page<ProductDto> filterProducts(String category,
                                           Double minPrice,
                                           Double maxPrice,
                                           Pageable pageable) {

        logger.debug("Filtering products - category: {}, minPrice: {}, maxPrice: {}",
                category, minPrice, maxPrice);

        Page<Product> products;

        if (category != null && minPrice != null && maxPrice != null) {

            products = productRepository
                    .findByCategoryAndPriceBetween(category, minPrice, maxPrice, pageable);

        } else if (category != null) {

            products = productRepository
                    .findByCategory(category, pageable);

        } else if (minPrice != null && maxPrice != null) {

            products = productRepository
                    .findByPriceBetween(minPrice, maxPrice, pageable);

        } else {

            products = productRepository.findAll(pageable);
        }

        return products.map(this::convertToDto);
    }

    /**
     * Search products by keyword
     */
    public Page<ProductDto> searchProducts(String keyword, Pageable pageable) {

        logger.debug("Searching products with keyword: {}", keyword);

        return productRepository
                .findByNameContainingIgnoreCase(keyword, pageable)
                .map(this::convertToDto);
    }

    /**
     * ADMIN: Update product
     */
    public ProductDto updateProduct(Long id, ProductDto updatedProductDto) {

        logger.info("Updating product with ID {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Product not found with ID {}", id);
                    return new ResourceNotFoundException("Product not found with id " + id);
                });

        product.setName(updatedProductDto.getName());
        product.setDescription(updatedProductDto.getDescription());
        product.setPrice(updatedProductDto.getPrice());
        product.setStock(updatedProductDto.getStock());
        product.setCategory(updatedProductDto.getCategory());
        product.setImageUrl(updatedProductDto.getImageUrl());
        product.setRating(updatedProductDto.getRating());

        Product savedProduct = productRepository.save(product);

        logger.info("Product updated successfully with ID {}", id);

        return convertToDto(savedProduct);
    }

    /**
     * ADMIN: Delete product
     */
    public void deleteProduct(Long id) {

        logger.warn("Attempting to delete product with ID {}", id);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Product not found with ID {}", id);
                    return new ResourceNotFoundException("Product not found with id " + id);
                });

        productRepository.delete(product);

        logger.warn("Product deleted successfully with ID {}", id);
    }

    /**
     * Convert Entity → DTO
     */
    private ProductDto convertToDto(Product product) {
        return modelMapper.map(product, ProductDto.class);
    }

    /**
     * Convert DTO → Entity
     */
    private Product convertToEntity(ProductDto dto) {
        return modelMapper.map(dto, Product.class);
    }
}