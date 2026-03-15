package com.ecommerce.service;

import com.ecommerce.dto.ProductDto;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.ProductRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.*;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private ProductDto productDto;
    private Pageable pageable;

    @BeforeEach
    void setup() {

        pageable = PageRequest.of(0, 5);

        product = new Product();
        product.setId(1L);
        product.setName("Laptop");
        product.setCategory("electronics");
        BigDecimal price = new BigDecimal(1000);
        product.setPrice(price);
        product.setStock(10);

        productDto = new ProductDto();
        productDto.setId(1L);
        productDto.setName("Laptop");
        productDto.setCategory("electronics");
        productDto.setPrice(price);
        productDto.setStock(10);
    }

    /**
     * createProduct SUCCESS
     */
    @Test
    void createProduct_success() {

        when(modelMapper.map(productDto, Product.class)).thenReturn(product);
        when(productRepository.save(product)).thenReturn(product);
        when(modelMapper.map(product, ProductDto.class)).thenReturn(productDto);

        ProductDto result = productService.createProduct(productDto);

        assertNotNull(result);
        assertEquals("Laptop", result.getName());

        verify(productRepository).save(product);
    }

    /**
     * getProductById SUCCESS
     */
    @Test
    void getProductById_success() {

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(modelMapper.map(product, ProductDto.class)).thenReturn(productDto);

        ProductDto result = productService.getProductById(1L);

        assertEquals("Laptop", result.getName());
    }

    /**
     * getProductById NOT FOUND
     */
    @Test
    void getProductById_notFound() {

        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> productService.getProductById(1L));
    }

    /**
     * filterProducts CATEGORY + PRICE
     */
    @Test
    void filterProducts_categoryAndPrice() {

        Page<Product> productPage = new PageImpl<>(java.util.List.of(product));

        when(productRepository.findByCategoryAndPriceBetween(
                "electronics", 500.0, 1500.0, pageable))
                .thenReturn(productPage);

        when(modelMapper.map(product, ProductDto.class)).thenReturn(productDto);

        Page<ProductDto> result =
                productService.filterProducts("electronics", 500.0, 1500.0, pageable);

        assertEquals(1, result.getTotalElements());
    }

    /**
     * filterProducts CATEGORY ONLY
     */
    @Test
    void filterProducts_categoryOnly() {

        Page<Product> productPage = new PageImpl<>(java.util.List.of(product));

        when(productRepository.findByCategory("electronics", pageable))
                .thenReturn(productPage);

        when(modelMapper.map(product, ProductDto.class)).thenReturn(productDto);

        Page<ProductDto> result =
                productService.filterProducts("electronics", null, null, pageable);

        assertEquals(1, result.getTotalElements());
    }

    /**
     * filterProducts PRICE ONLY
     */
    @Test
    void filterProducts_priceOnly() {

        Page<Product> productPage = new PageImpl<>(java.util.List.of(product));

        when(productRepository.findByPriceBetween(500.0, 1500.0, pageable))
                .thenReturn(productPage);

        when(modelMapper.map(product, ProductDto.class)).thenReturn(productDto);

        Page<ProductDto> result =
                productService.filterProducts(null, 500.0, 1500.0, pageable);

        assertEquals(1, result.getTotalElements());
    }

    /**
     * filterProducts NO FILTER
     */
    @Test
    void filterProducts_noFilter() {

        Page<Product> productPage = new PageImpl<>(java.util.List.of(product));

        when(productRepository.findAll(pageable)).thenReturn(productPage);
        when(modelMapper.map(product, ProductDto.class)).thenReturn(productDto);

        Page<ProductDto> result =
                productService.filterProducts(null, null, null, pageable);

        assertEquals(1, result.getTotalElements());
    }

    /**
     * searchProducts SUCCESS
     */
    @Test
    void searchProducts_success() {

        Page<Product> productPage = new PageImpl<>(java.util.List.of(product));

        when(productRepository.findByNameContainingIgnoreCase("lap", pageable))
                .thenReturn(productPage);

        when(modelMapper.map(product, ProductDto.class)).thenReturn(productDto);

        Page<ProductDto> result =
                productService.searchProducts("lap", pageable);

        assertEquals(1, result.getTotalElements());
    }

    /**
     * updateProduct SUCCESS
     */
    @Test
    void updateProduct_success() {

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(product)).thenReturn(product);
        when(modelMapper.map(product, ProductDto.class)).thenReturn(productDto);

        ProductDto result = productService.updateProduct(1L, productDto);

        assertEquals("Laptop", result.getName());
    }

    /**
     * updateProduct NOT FOUND
     */
    @Test
    void updateProduct_notFound() {

        when(productRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> productService.updateProduct(1L, productDto));
    }

    /**
     * deleteProduct SUCCESS
     */
    @Test
    void deleteProduct_success() {

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        productService.deleteProduct(1L);

        verify(productRepository).delete(product);
    }

}