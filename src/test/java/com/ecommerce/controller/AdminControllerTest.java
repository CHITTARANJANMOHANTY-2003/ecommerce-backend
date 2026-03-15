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

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AdminController.class)
@AutoConfigureMockMvc(addFilters = false)
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    /* ---- SECURITY MOCKS (same as previous tests) ---- */

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

    /* ------------------------------------------------ */

    @Autowired
    private ObjectMapper objectMapper;

    private CustomUserDetails buildAdminPrincipal() {

        User admin = new User();
        admin.setId(1L);
        admin.setName("Admin");
        admin.setEmail("admin@gmail.com");
        admin.setPassword("encoded");
        admin.setRole(Role.ROLE_ADMIN);

        return new CustomUserDetails(admin);
    }

    private UserDto buildUserDto() {
        UserDto dto = new UserDto();
        dto.setId(2L);
        dto.setName("John");
        dto.setEmail("john@gmail.com");
        return dto;
    }

    @Test
    void testCreateAdmin_success() throws Exception {

        CustomUserDetails admin = buildAdminPrincipal();

        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(
                        admin, null, admin.getAuthorities());

        UserDto request = buildUserDto();
        UserDto response = buildUserDto();

        when(userService.registerAdmin(any(UserDto.class), eq(admin.getUsername())))
                .thenReturn(response);

        SecurityContextHolder.getContext().setAuthentication(authentication);

        try {

            mockMvc.perform(post("/api/admin/create-admin")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
                    .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.email").value("john@gmail.com"));

        } finally {
            SecurityContextHolder.clearContext();
        }
    }

    @Test
    void testGetAllUsers_success() throws Exception {

        when(userService.getAllUsers()).thenReturn(List.of(buildUserDto()));

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("john@gmail.com"));
    }

    @Test
    void testGetUser_success() throws Exception {

        when(userService.getUserById(1L)).thenReturn(buildUserDto());

        mockMvc.perform(get("/api/admin/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("john@gmail.com"));
    }

    @Test
    void testUpdateUser_success() throws Exception {

        UserDto updated = buildUserDto();
        updated.setName("Updated User");

        when(userService.updateUser(eq(1L), any(UserDto.class)))
                .thenReturn(updated);

        mockMvc.perform(put("/api/admin/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updated))
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated User"));
    }

    @Test
    void testDeleteUser_success() throws Exception {

        doNothing().when(userService).deleteUser(1L);

        mockMvc.perform(delete("/api/admin/users/1")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(content().string("User deleted successfully"));

        verify(userService).deleteUser(1L);
    }
}