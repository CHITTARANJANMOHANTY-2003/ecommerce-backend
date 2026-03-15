package com.ecommerce.util;

import static org.junit.jupiter.api.Assertions.*;

import java.lang.reflect.Field;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import com.ecommerce.security.JwtUtil;

class JwtUtilTest {

    private JwtUtil jwtUtil;

    private UserDetails userDetails;

    private final String SECRET =
            "mysecretkeymysecretkeymysecretkeymysecretkey"; // >=32 bytes

    @BeforeEach
    void setup() throws Exception {

        jwtUtil = new JwtUtil();

        userDetails = User
                .withUsername("testuser")
                .password("password")
                .authorities("ROLE_USER")
                .build();

        // Inject jwtSecret
        Field secretField = JwtUtil.class.getDeclaredField("jwtSecret");
        secretField.setAccessible(true);
        secretField.set(jwtUtil, SECRET);

        // Inject expiration
        Field expField = JwtUtil.class.getDeclaredField("jwtExpirationMs");
        expField.setAccessible(true);
        expField.set(jwtUtil, 3600000L);
    }

    /**
     * Test token generation
     */
    @Test
    void generateToken_shouldCreateValidToken() {

        String token = jwtUtil.generateToken(userDetails);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    /**
     * Test username extraction
     */
    @Test
    void getUsernameFromToken_shouldReturnCorrectUsername() {

        String token = jwtUtil.generateToken(userDetails);

        String username = jwtUtil.getUsernameFromToken(token);

        assertEquals("testuser", username);
    }

    /**
     * Test valid token
     */
    @Test
    void validateToken_shouldReturnTrueForValidToken() {

        String token = jwtUtil.generateToken(userDetails);

        boolean isValid = jwtUtil.validateToken(token);

        assertTrue(isValid);
    }

    /**
     * Test invalid token
     */
    @Test
    void validateToken_shouldReturnFalseForInvalidToken() {

        String invalidToken = "invalid.jwt.token";

        boolean isValid = jwtUtil.validateToken(invalidToken);

        assertFalse(isValid);
    }

}