package com.cts.vis.controller;

import com.cts.vis.dto.CustomerDTO;
import com.cts.vis.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    public void testRootRedirect() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customer/login"));
    }

    @Test
    public void testCustomerLoginView() throws Exception {
        mockMvc.perform(get("/customer/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer/login"));
    }

    @Test
    public void testAdminLoginView() throws Exception {
        mockMvc.perform(get("/admin/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/login"));
    }

    @Test
    public void testRegisterFormView() throws Exception {
        mockMvc.perform(get("/customer/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer/register"))
                .andExpect(model().attributeExists("customer"));
    }

    @Test
    public void testRegisterSuccess() throws Exception {
        // Validating against your DTO:
        // Name: Letters only | Email: .com | Phone: 10 digits | Password: Strong (8+ chars, digit, special)
        mockMvc.perform(post("/customer/register")
                        .param("name", "John Doe")
                        .param("email", "john@example.com")
                        .param("phone", "9876543210")
                        .param("address", "123 Main Street")
                        .param("password", "Strong@123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customer/login?registered=true"));

        verify(authService, times(1)).registerCustomer(
                eq("John Doe"), eq("john@example.com"), eq("9876543210"), eq("123 Main Street"), eq("Strong@123")
        );
    }

    @Test
    public void testRegisterFailure_EmailExists() throws Exception {
        doThrow(new RuntimeException("Email already registered."))
                .when(authService).registerCustomer(anyString(), anyString(), anyString(), anyString(), anyString());

        mockMvc.perform(post("/customer/register")
                        .param("name", "John Doe")
                        .param("email", "john@example.com")
                        .param("phone", "9876543210")
                        .param("address", "123 Main Street")
                        .param("password", "Strong@123"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer/register"))
                .andExpect(model().hasErrors());
    }
}