package com.ecommerce.controller;

import com.ecommerce.dto.UserDto;
import com.ecommerce.security.CustomUserDetails;
import com.ecommerce.service.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private static final Logger logger =
            LoggerFactory.getLogger(AdminController.class);

    private final UserService userService;

    public AdminController(UserService userService) {
        this.userService = userService;
    }

    /**
     * ADMIN: Create another admin account
     * Only authenticated ADMIN users can access this endpoint.
     */
    @PostMapping("/create-admin")
    public ResponseEntity<UserDto> createAdmin(
            @RequestBody UserDto dto,
            @AuthenticationPrincipal CustomUserDetails admin) {

        logger.info("Admin {} attempting to create new ADMIN account",
                admin.getUsername());

        UserDto createdAdmin =
                userService.registerAdmin(dto, admin.getUsername());

        logger.info("New ADMIN created successfully with email {}",
                createdAdmin.getEmail());

        return ResponseEntity.ok(createdAdmin);
    }

    /**
     * ADMIN: Fetch all registered users
     */
    @GetMapping("/users")
    public ResponseEntity<List<UserDto>> getAllUsers() {

        logger.debug("Admin requested list of all users");

        List<UserDto> users = userService.getAllUsers();

        return ResponseEntity.ok(users);
    }

    /**
     * ADMIN: Fetch user details by ID
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<UserDto> getUser(@PathVariable Long id) {

        logger.debug("Admin fetching user with ID {}", id);

        UserDto user = userService.getUserById(id);

        return ResponseEntity.ok(user);
    }

    /**
     * ADMIN: Update any user profile
     */
    @PutMapping("/users/{id}")
    public ResponseEntity<UserDto> updateUser(
            @PathVariable Long id,
            @RequestBody UserDto dto) {

        logger.info("Admin updating user with ID {}", id);

        UserDto updatedUser = userService.updateUser(id, dto);

        logger.info("User updated successfully with ID {}", id);

        return ResponseEntity.ok(updatedUser);
    }

    /**
     * ADMIN: Delete a user
     */
    @DeleteMapping("/users/{id}")
    public ResponseEntity<String> deleteUser(@PathVariable Long id) {

        logger.warn("Admin attempting to delete user with ID {}", id);

        userService.deleteUser(id);

        logger.warn("User deleted successfully with ID {}", id);

        return ResponseEntity.ok("User deleted successfully");
    }
}