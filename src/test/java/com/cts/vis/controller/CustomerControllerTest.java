package com.cts.vis.controller;

import com.cts.vis.dto.CustomerDTO;
import com.cts.vis.model.Customer;
import com.cts.vis.service.CustomerService;
import com.cts.vis.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class CustomerControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CustomerService customerService;

    @Mock
    private ReportService reportService;

    @InjectMocks
    private CustomerController customerController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(customerController).build();
    }

    @Test
    public void testDashboard() throws Exception {
        // 1. Arrange
        Customer mockCustomer = new Customer();
        mockCustomer.setName("John Doe");

        Map<String, Object> stats = new HashMap<String, Object>();
        stats.put("activePolicies", 2);

        when(customerService.getCurrentCustomer()).thenReturn(mockCustomer);
        when(reportService.customerDashboardStats()).thenReturn(stats);

        // 2. Act & Assert
        mockMvc.perform(get("/customer/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer/dashboard"))
                .andExpect(model().attribute("customer", mockCustomer))
                .andExpect(model().attribute("activePolicies", 2));
    }

    @Test
    public void testProfilePage() throws Exception {
        Customer mockCustomer = new Customer();
        when(customerService.getCurrentCustomer()).thenReturn(mockCustomer);

        mockMvc.perform(get("/customer/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer/profile"))
                .andExpect(model().attributeExists("customer"))
                .andExpect(model().attributeExists("profile"));
    }

    @Test
    public void testUpdateProfile() throws Exception {
        // 1. Act
        mockMvc.perform(post("/customer/profile")
                        .param("name", "Jane Doe")
                        .param("phone", "9876543210")
                        .param("address", "456 Oak Ave"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customer/profile?saved=true"));

        // 2. Assert
        verify(customerService, times(1)).updateProfile("Jane Doe", "9876543210", "456 Oak Ave");
    }
}