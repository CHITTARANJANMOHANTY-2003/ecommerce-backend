package com.ecommerce.controller;

import com.ecommerce.dto.UserDto;
import com.ecommerce.security.CustomUserDetails;
import com.ecommerce.service.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Register a new user (Customer)
     * Client sends registration details as DTO.
     * Controller forwards DTO to Service layer.
     */
    @PostMapping("/register")
    public ResponseEntity<UserDto> register(@RequestBody UserDto dto) {

        logger.info("Received request to register new user with email: {}", dto.getEmail());

        UserDto registeredUser = userService.registerCustomer(dto);

        logger.info("User registration completed for email: {}", dto.getEmail());

        return ResponseEntity.ok(registeredUser);
    }

    /**
     * Get profile of currently logged-in user
     * AuthenticationPrincipal injects the authenticated user's details
     */
    @GetMapping("/me")
    public ResponseEntity<UserDto> getMyProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        Long userId = userDetails.getUser().getId();

        logger.debug("Fetching profile for authenticated user ID {}", userId);

        UserDto user = userService.getUserById(userId);

        return ResponseEntity.ok(user);
    }

    /**
     * Update profile of currently logged-in user
     * Only basic fields like name and email can be updated
     */
    @PutMapping("/me")
    public ResponseEntity<UserDto> updateProfile(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody UserDto dto) {

        Long userId = userDetails.getUser().getId();

        logger.info("Profile update requested for user ID {}", userId);

        UserDto updatedUser = userService.updateUser(userId, dto);

        logger.info("Profile updated successfully for user ID {}", userId);

        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Change password for currently logged-in user
     * Password is encrypted inside the Service layer
     */
    @PutMapping("/me/password")
    public ResponseEntity<String> changePassword(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam String newPassword) {

        Long userId = userDetails.getUser().getId();

        logger.info("Password change request received for user ID {}", userId);

        userService.changePassword(userId, newPassword);

        logger.info("Password updated successfully for user ID {}", userId);

        return ResponseEntity.ok("Password updated successfully");
    }
}