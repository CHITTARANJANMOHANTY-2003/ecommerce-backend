package com.ecommerce.service;

import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;

import com.ecommerce.entity.User;
import com.ecommerce.repository.UserRepository;
import com.ecommerce.security.CustomUserDetailsService;

@SpringBootTest
@ActiveProfiles("test")
class CustomUserDetailsServiceTest {

    @MockBean
    private UserRepository userRepository; // Mock injected

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void testLoadUserByUsername() {
        User mockUser = new User();
        mockUser.setEmail("test@example.com");

        // Wrap the user in Optional
        Mockito.when(userRepository.findByEmail("test@example.com"))
               .thenReturn(Optional.of(mockUser));

        var userDetails = customUserDetailsService.loadUserByUsername("test@example.com");
        Assertions.assertNotNull(userDetails);
    }
}