package com.ecommerce.service;

import com.ecommerce.dto.UserDto;
import com.ecommerce.entity.User;
import com.ecommerce.enums.Role;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.UserRepository;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ModelMapper modelMapper;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.modelMapper = modelMapper;
    }

    /**
     * Register new CUSTOMER
     */
    public UserDto registerCustomer(UserDto userDto) {

        logger.info("Attempting to register new user with email: {}", userDto.getEmail());

        if (userRepository.existsByEmail(userDto.getEmail())) {
            logger.error("Registration failed. Email already exists: {}", userDto.getEmail());
            throw new IllegalArgumentException("Email already registered");
        }

        // Convert DTO -> Entity
        User user = convertToEntity(userDto);

        // Encrypt password before saving
        user.setPassword(passwordEncoder.encode(userDto.getPassword()));
        user.setRole(Role.ROLE_CUSTOMER);

        User savedUser = userRepository.save(user);

        logger.info("User successfully registered with ID {}", savedUser.getId());

        return convertToDto(savedUser);
    }

    /**
     * Register new ADMIN (Only Admin can create another admin)
     */
    public UserDto registerAdmin(UserDto userDto, String adminEmail) {

        logger.info("Admin {} attempting to create new ADMIN account", adminEmail);

        User adminUser = userRepository.findByEmail(adminEmail)
                .orElseThrow(() -> {
                    logger.error("Admin creation failed. Admin not found: {}", adminEmail);
                    return new RuntimeException("Admin not found");
                });

        if (adminUser.getRole() != Role.ROLE_ADMIN) {
            logger.warn("Unauthorized attempt to create ADMIN by {}", adminEmail);
            throw new RuntimeException("Only ADMIN can create another ADMIN");
        }

        if (userRepository.existsByEmail(userDto.getEmail())) {
            logger.error("Admin registration failed. Email already exists: {}", userDto.getEmail());
            throw new RuntimeException("Email already exists");
        }

        User newAdmin = convertToEntity(userDto);
        newAdmin.setPassword(passwordEncoder.encode(userDto.getPassword()));
        newAdmin.setRole(Role.ROLE_ADMIN);

        User savedAdmin = userRepository.save(newAdmin);

        logger.info("New ADMIN created successfully with ID {}", savedAdmin.getId());

        return convertToDto(savedAdmin);
    }

    /**
     * Fetch single user by ID
     */
    public UserDto getUserById(Long id) {

        logger.debug("Fetching user with ID {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("User not found with ID {}", id);
                    return new ResourceNotFoundException("User not found with id " + id);
                });

        return convertToDto(user);
    }

    /**
     * Fetch all users (Admin operation)
     */
    public List<UserDto> getAllUsers() {

        logger.debug("Fetching all users from database");

        return userRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    /**
     * Update user profile information
     */
    public UserDto updateUser(Long id, UserDto userDto) {

        logger.info("Updating profile for user ID {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Update failed. User not found with ID {}", id);
                    return new ResourceNotFoundException("User not found with id " + id);
                });

        // Update only allowed fields
        user.setName(userDto.getName());
        user.setEmail(userDto.getEmail());

        User updatedUser = userRepository.save(user);

        logger.info("User profile updated successfully for ID {}", id);

        return convertToDto(updatedUser);
    }

    /**
     * Change user password
     */
    public void changePassword(Long id, String newPassword) {

        logger.info("Password change requested for user ID {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Password change failed. User not found with ID {}", id);
                    return new ResourceNotFoundException("User not found with id " + id);
                });

        user.setPassword(passwordEncoder.encode(newPassword));

        userRepository.save(user);

        logger.info("Password updated successfully for user ID {}", id);
    }

    /**
     * Delete user (Admin operation)
     */
    public void deleteUser(Long id) {

        logger.warn("Attempting to delete user with ID {}", id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    logger.error("Delete failed. User not found with ID {}", id);
                    return new ResourceNotFoundException("User not found with id " + id);
                });

        userRepository.delete(user);

        logger.warn("User with ID {} deleted successfully", id);
    }

    /**
     * Convert Entity -> DTO
     * Password is intentionally removed for security reasons
     */
    private UserDto convertToDto(User user) {

        UserDto dto = modelMapper.map(user, UserDto.class);

        // Never expose password in API responses
        dto.setPassword(null);

        return dto;
    }

    /**
     * Convert DTO -> Entity
     */
    private User convertToEntity(UserDto dto) {
        return modelMapper.map(dto, User.class);
    }
}