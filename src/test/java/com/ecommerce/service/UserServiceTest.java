package com.ecommerce.service;

import com.ecommerce.dto.UserDto;
import com.ecommerce.entity.User;
import com.ecommerce.enums.Role;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.modelmapper.ModelMapper;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserDto userDto;

    @BeforeEach
    void setup() {

        user = new User();
        user.setId(1L);
        user.setName("John");
        user.setEmail("john@test.com");
        user.setPassword("encoded");
        user.setRole(Role.ROLE_CUSTOMER);

        userDto = new UserDto();
        userDto.setId(1L);
        userDto.setName("John");
        userDto.setEmail("john@test.com");
        userDto.setPassword("password");
    }

    /**
     * registerCustomer SUCCESS
     */
    @Test
    void registerCustomer_success() {

        when(userRepository.existsByEmail(userDto.getEmail())).thenReturn(false);
        when(modelMapper.map(userDto, User.class)).thenReturn(user);
        when(passwordEncoder.encode(userDto.getPassword())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(modelMapper.map(user, UserDto.class)).thenReturn(userDto);

        UserDto result = userService.registerCustomer(userDto);

        assertNotNull(result);
        assertEquals(userDto.getEmail(), result.getEmail());

        verify(userRepository).save(any(User.class));
    }

    /**
     * registerCustomer EMAIL EXISTS
     */
    @Test
    void registerCustomer_emailExists() {

        when(userRepository.existsByEmail(userDto.getEmail())).thenReturn(true);

        assertThrows(IllegalArgumentException.class,
                () -> userService.registerCustomer(userDto));
    }

    /**
     * registerAdmin SUCCESS
     */
    @Test
    void registerAdmin_success() {

        User admin = new User();
        admin.setEmail("admin@test.com");
        admin.setRole(Role.ROLE_ADMIN);

        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.of(admin));
        when(userRepository.existsByEmail(userDto.getEmail())).thenReturn(false);
        when(modelMapper.map(userDto, User.class)).thenReturn(user);
        when(passwordEncoder.encode(userDto.getPassword())).thenReturn("encoded");
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(modelMapper.map(user, UserDto.class)).thenReturn(userDto);

        UserDto result = userService.registerAdmin(userDto, "admin@test.com");

        assertNotNull(result);
        verify(userRepository).save(any(User.class));
    }

    /**
     * registerAdmin ADMIN NOT FOUND
     */
    @Test
    void registerAdmin_adminNotFound() {

        when(userRepository.findByEmail("admin@test.com")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> userService.registerAdmin(userDto, "admin@test.com"));
    }

    /**
     * getUserById SUCCESS
     */
    @Test
    void getUserById_success() {

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(modelMapper.map(user, UserDto.class)).thenReturn(userDto);

        UserDto result = userService.getUserById(1L);

        assertEquals(userDto.getEmail(), result.getEmail());
    }

    /**
     * getUserById NOT FOUND
     */
    @Test
    void getUserById_notFound() {

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> userService.getUserById(1L));
    }

    /**
     * getAllUsers SUCCESS
     */
    @Test
    void getAllUsers_success() {

        when(userRepository.findAll()).thenReturn(List.of(user));
        when(modelMapper.map(user, UserDto.class)).thenReturn(userDto);

        List<UserDto> users = userService.getAllUsers();

        assertEquals(1, users.size());
    }

    /**
     * updateUser SUCCESS
     */
    @Test
    void updateUser_success() {

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(user);
        when(modelMapper.map(user, UserDto.class)).thenReturn(userDto);

        UserDto result = userService.updateUser(1L, userDto);

        assertEquals(userDto.getEmail(), result.getEmail());
    }

    /**
     * changePassword SUCCESS
     */
    @Test
    void changePassword_success() {

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.encode("newpass")).thenReturn("encoded");

        userService.changePassword(1L, "newpass");

        verify(userRepository).save(user);
    }

    /**
     * deleteUser SUCCESS
     */
    @Test
    void deleteUser_success() {

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        userService.deleteUser(1L);

        verify(userRepository).delete(user);
    }

}