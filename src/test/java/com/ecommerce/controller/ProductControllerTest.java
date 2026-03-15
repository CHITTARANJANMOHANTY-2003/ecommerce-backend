package com.ecommerce.controller;

import com.ecommerce.config.JwtAuthenticationEntryPoint;
import com.ecommerce.dto.ProductDto;
import com.ecommerce.security.CustomUserDetailsService;
import com.ecommerce.security.JwtAuthenticationFilter;
import com.ecommerce.security.JwtUtil;
import com.ecommerce.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller tests for ProductController
 *
 * Same security mock strategy used in UserControllerTest
 */
@WebMvcTest(controllers = ProductController.class)
@AutoConfigureMockMvc(addFilters = false)
class ProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ProductService productService;

    /* ---------- SECURITY MOCKS (REQUIRED) ---------- */

    @MockBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockBean
    private JwtUtil jwtUtil;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @MockBean
    private AuthenticationManager authenticationManager;

    /* ----------------------------------------------- */

    @Autowired
    private ObjectMapper objectMapper;

    private ProductDto buildProduct() {
        ProductDto dto = new ProductDto();
        dto.setId(1L);
        dto.setName("iPhone 15");
        dto.setDescription("Apple smartphone");
        dto.setPrice(new BigDecimal("75000"));
        dto.setStock(10);
        dto.setCategory("smartphones");
        dto.setImageUrl("img.jpg");
        dto.setRating(4.5);
        return dto;
    }

    @Test
    void testCreateProduct_success() throws Exception {

        ProductDto product = buildProduct();

        when(productService.createProduct(any(ProductDto.class))).thenReturn(product);

        mockMvc.perform(post("/api/products")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(product))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("iPhone 15"))
                .andExpect(jsonPath("$.price").value(75000));

        verify(productService).createProduct(any(ProductDto.class));
    }

    @Test
    void testGetProduct_success() throws Exception {

        ProductDto product = buildProduct();

        when(productService.getProductById(1L)).thenReturn(product);

        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("iPhone 15"))
                .andExpect(jsonPath("$.category").value("smartphones"));
    }

    @Test
    void testGetProducts_withFilters() throws Exception {

        ProductDto product = buildProduct();

        Page<ProductDto> page =
                new PageImpl<>(List.of(product), PageRequest.of(0,10),1);

        when(productService.filterProducts(any(), any(), any(), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/products")
                .param("category","smartphones")
                .param("minPrice","70000")
                .param("maxPrice","80000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("iPhone 15"));
    }

    @Test
    void testSearchProducts_success() throws Exception {

        ProductDto product = buildProduct();

        Page<ProductDto> page =
                new PageImpl<>(List.of(product), PageRequest.of(0,10),1);

        when(productService.searchProducts(eq("iphone"), any()))
                .thenReturn(page);

        mockMvc.perform(get("/api/products/search")
                .param("keyword","iphone"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name").value("iPhone 15"));
    }

    @Test
    void testUpdateProduct_success() throws Exception {

        ProductDto updated = buildProduct();
        updated.setName("iPhone 15 Pro");

        when(productService.updateProduct(eq(1L), any(ProductDto.class)))
                .thenReturn(updated);

        mockMvc.perform(put("/api/products/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updated))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("iPhone 15 Pro"));
    }

    @Test
    void testDeleteProduct_success() throws Exception {

        doNothing().when(productService).deleteProduct(1L);

        mockMvc.perform(delete("/api/products/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("Product deleted successfully"));

        verify(productService).deleteProduct(1L);
    }
}