package com.cts.vis.service;

import com.cts.vis.exception.NotFoundException;
import com.cts.vis.model.Customer;
import com.cts.vis.model.User;
import com.cts.vis.repository.CustomerRepository;
import com.cts.vis.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private CustomerServiceImpl customerService;

    private final String testEmail = "customer@test.com";

    @BeforeEach
    void setupSecurityContext() {
        // Set the mock security context to the holder
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void testGetCurrentUserEmail_Success() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(testEmail);

        // Act
        String email = customerService.getCurrentUserEmail();

        // Assert
        assertEquals(testEmail, email);
    }

    @Test
    void testGetCurrentUserEmail_ReturnsNull_WhenAnonymous() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("anonymousUser");

        // Act
        String email = customerService.getCurrentUserEmail();

        // Assert
        assertNull(email);
    }

    @Test
    void testGetCurrentCustomer_Success() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(testEmail);

        User mockUser = new User();
        mockUser.setEmail(testEmail);
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(mockUser));

        Customer mockCustomer = new Customer();
        mockCustomer.setName("Alice");
        when(customerRepository.findByUser(mockUser)).thenReturn(Optional.of(mockCustomer));

        // Act
        Customer result = customerService.getCurrentCustomer();

        // Assert
        assertNotNull(result);
        assertEquals("Alice", result.getName());
    }

    @Test
    void testGetCurrentCustomer_ThrowsException_WhenNoAuth() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(null);

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> customerService.getCurrentCustomer());
    }

    @Test
    void testGetCurrentCustomer_ThrowsNotFound_WhenUserMissing() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(testEmail);
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> customerService.getCurrentCustomer());
    }

    @Test
    void testUpdateProfile_Success() {
        // Arrange
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(testEmail);

        User mockUser = new User();
        when(userRepository.findByEmail(testEmail)).thenReturn(Optional.of(mockUser));

        Customer mockCustomer = new Customer();
        mockCustomer.setName("Old Name");
        when(customerRepository.findByUser(mockUser)).thenReturn(Optional.of(mockCustomer));

        // Act
        customerService.updateProfile("New Name", "9999999999", "New Address");

        // Assert
        assertEquals("New Name", mockCustomer.getName());
        assertEquals("9999999999", mockCustomer.getPhone());
        verify(customerRepository, times(1)).save(mockCustomer);
    }
}