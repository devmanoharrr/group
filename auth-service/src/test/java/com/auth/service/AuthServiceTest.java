package com.auth.service;

import com.auth.dto.*;
import com.auth.exception.BadRequestException;
import com.auth.exception.UnauthorizedException;
import com.auth.model.User;
import com.auth.repository.UserRepository;
import com.auth.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");
        testUser.setPassword("encodedPassword");
        testUser.setName("Test User");
    }

    @Test
    void testRegister_Success() {
        RegisterRequest request = new RegisterRequest("newuser@example.com", "password123", "New User");
        
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        when(jwtUtil.generateToken(anyString(), anyLong())).thenReturn("testToken");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("testToken", response.getToken());
        assertNotNull(response.getUser());
        assertEquals("test@example.com", response.getUser().getEmail());
        
        verify(userRepository).existsByEmail("newuser@example.com");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testRegister_EmailExists() {
        RegisterRequest request = new RegisterRequest("existing@example.com", "password123", "User");
        
        when(userRepository.existsByEmail(anyString())).thenReturn(true);

        assertThrows(BadRequestException.class, () -> authService.register(request));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testLogin_Success() {
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);
        when(jwtUtil.generateToken(anyString(), anyLong())).thenReturn("testToken");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("testToken", response.getToken());
        assertNotNull(response.getUser());
    }

    @Test
    void testLogin_InvalidEmail() {
        LoginRequest request = new LoginRequest("wrong@example.com", "password123");
        
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }

    @Test
    void testLogin_InvalidPassword() {
        LoginRequest request = new LoginRequest("test@example.com", "wrongPassword");
        
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }

    @Test
    void testChangePassword_Success() {
        ChangePasswordRequest request = new ChangePasswordRequest("oldPassword", "newPassword123");
        
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("oldPassword", "encodedPassword")).thenReturn(true);
        when(passwordEncoder.encode("newPassword123")).thenReturn("newEncodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        MessageResponse response = authService.changePassword(1L, request);

        assertNotNull(response);
        assertEquals("Password changed successfully", response.getMessage());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testChangePassword_WrongOldPassword() {
        ChangePasswordRequest request = new ChangePasswordRequest("wrongPassword", "newPassword123");
        
        when(userRepository.findById(anyLong())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrongPassword", "encodedPassword")).thenReturn(false);

        assertThrows(UnauthorizedException.class, () -> authService.changePassword(1L, request));
        verify(userRepository, never()).save(any(User.class));
    }
}

