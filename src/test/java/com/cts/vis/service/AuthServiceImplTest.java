package com.cts.vis.service;

import com.cts.vis.exception.BadRequestException;
import com.cts.vis.model.Customer;
import com.cts.vis.model.User;
import com.cts.vis.model.UserRole;
import com.cts.vis.repository.CustomerRepository;
import com.cts.vis.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthServiceImpl authService;

    private String name = "John Doe";
    private String email = "john@example.com";
    private String phone = "9876543210";
    private String address = "123 Main St";
    private String rawPassword = "StrongPassword@123";
    private String encodedPassword = "hashed_password";

    @Test
    void testRegisterCustomer_Success() {
        // Arrange
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);

        // Use standard setId() matching your User model
        User mockSavedUser = new User();
        mockSavedUser.setId(1L);
        mockSavedUser.setEmail(email);

        when(userRepository.save(any(User.class))).thenReturn(mockSavedUser);

        Customer mockSavedCustomer = new Customer();
        mockSavedCustomer.setName(name);
        mockSavedCustomer.setUser(mockSavedUser);

        when(customerRepository.save(any(Customer.class))).thenReturn(mockSavedCustomer);

        // Act
        Customer registeredCustomer = authService.registerCustomer(name, email, phone, address, rawPassword);

        // Assert
        assertNotNull(registeredCustomer);
        assertEquals(name, registeredCustomer.getName());
        assertEquals(1L, registeredCustomer.getUser().getId()); // Accessing via .getId()

        // Verify
        verify(userRepository).save(any(User.class));
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void testRegisterCustomer_ThrowsBadRequest_WhenEmailExists() {
        // Arrange
        when(userRepository.existsByEmail(email)).thenReturn(true);

        // Act & Assert
        BadRequestException exception = assertThrows(BadRequestException.class, () -> {
            authService.registerCustomer(name, email, phone, address, rawPassword);
        });

        assertTrue(exception.getMessage().contains("Email already registered"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testRegisterCustomer_VerifyUserFields() {
        // Arrange
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);

        // Return the object that was passed into the save method
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);
        when(customerRepository.save(any(Customer.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Customer result = authService.registerCustomer(name, email, phone, address, rawPassword);

        // Assert properties from your User model
        User savedUser = result.getUser();
        assertEquals(email, savedUser.getEmail());
        assertEquals(encodedPassword, savedUser.getPasswordHash());
        assertEquals(UserRole.ROLE_CUSTOMER, savedUser.getRole());
        assertTrue(savedUser.getIsActive());
    }
}