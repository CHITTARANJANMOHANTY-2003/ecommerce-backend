package com.ecommerce.controller;

import com.ecommerce.config.JwtAuthenticationEntryPoint;
import com.ecommerce.dto.UserDto;
import com.ecommerce.entity.User;
import com.ecommerce.enums.Role;
import com.ecommerce.security.CustomUserDetails;
import com.ecommerce.security.CustomUserDetailsService;
import com.ecommerce.security.JwtAuthenticationFilter;
import com.ecommerce.security.JwtUtil;
import com.ecommerce.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Controller slice tests for UserController.
 *
 * - Security filters are disabled for the MockMvc slice via addFilters = false.
 * - All security-related components that the app configuration might create are mocked using @MockBean,
 *   so the test context starts cleanly.
 * - For endpoints using @AuthenticationPrincipal we explicitly set the SecurityContextHolder so the controller
 *   receives the CustomUserDetails as expected.
 */
@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    // Mock security beans so Spring won't attempt to construct real security filter beans
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

    @Autowired
    private ObjectMapper objectMapper;

    // ---------- Helpers ----------

    private User buildUser() {
        User u = new User();
        u.setId(1L);
        u.setName("John");
        u.setEmail("john@gmail.com");
        u.setPassword("encoded-pass");
        u.setRole(Role.ROLE_CUSTOMER);
        return u;
    }

    private CustomUserDetails buildCustomUserDetails() {
        return new CustomUserDetails(buildUser());
    }

    // ---------- Tests ----------

    @Test
    void testRegisterUser_success() throws Exception {
        UserDto request = new UserDto();
        request.setName("John");
        request.setEmail("john@gmail.com");
        request.setPassword("plaintext");

        UserDto response = new UserDto();
        response.setId(1L);
        response.setName("John");
        response.setEmail("john@gmail.com");

        when(userService.registerCustomer(any(UserDto.class))).thenReturn(response);

        mockMvc.perform(post("/api/users/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@gmail.com"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void testGetMyProfile_success() throws Exception {

        // build principal
        CustomUserDetails principal = buildCustomUserDetails();

        // build authentication token containing our CustomUserDetails
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        // mock service response
        UserDto response = new UserDto();
        response.setId(1L);
        response.setName("John");
        response.setEmail("john@gmail.com");

        when(userService.getUserById(1L)).thenReturn(response);

        // explicitly set SecurityContext so @AuthenticationPrincipal is populated
        SecurityContextHolder.getContext().setAuthentication(authentication);
        try {
            mockMvc.perform(get("/api/users/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value("john@gmail.com"))
                    .andExpect(jsonPath("$.id").value(1));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void testUpdateProfile_success() throws Exception {

        CustomUserDetails principal = buildCustomUserDetails();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        UserDto request = new UserDto();
        request.setName("John Updated");
        request.setEmail("john.updated@gmail.com");

        UserDto updated = new UserDto();
        updated.setId(1L);
        updated.setName("John Updated");
        updated.setEmail("john.updated@gmail.com");

        when(userService.updateUser(eq(1L), any(UserDto.class))).thenReturn(updated);

        SecurityContextHolder.getContext().setAuthentication(authentication);
        try {
            mockMvc.perform(put("/api/users/me")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.name").value("John Updated"))
                    .andExpect(jsonPath("$.email").value("john.updated@gmail.com"));
        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void testChangePassword_success() throws Exception {

        CustomUserDetails principal = buildCustomUserDetails();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());

        doNothing().when(userService).changePassword(1L, "newPassword");

        SecurityContextHolder.getContext().setAuthentication(authentication);
        try {
            mockMvc.perform(put("/api/users/me/password")
                    .param("newPassword", "newPassword")
                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Password updated successfully"));

            verify(userService).changePassword(1L, "newPassword");
        } finally {
            SecurityContextHolder.clearContext();
        }
    }
}