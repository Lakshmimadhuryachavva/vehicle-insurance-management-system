package com.cts.vis.controller;

import com.cts.vis.model.Customer;
import com.cts.vis.model.Policy;
import com.cts.vis.model.Vehicle;
import com.cts.vis.repository.CustomerRepository;
import com.cts.vis.repository.PolicyRepository;
import com.cts.vis.repository.VehicleRepository;
import com.cts.vis.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AdminControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReportService reportService;

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private PolicyRepository policyRepository;

    @InjectMocks
    private AdminController adminController;

    @BeforeEach
    public void setup() {
        // Initialize mocks
        MockitoAnnotations.openMocks(this);
        // Setup MockMvc in standalone mode
        this.mockMvc = MockMvcBuilders.standaloneSetup(adminController).build();
    }

    @Test
    public void testDashboard() throws Exception {
        // Prepare mock data
        Map<String, Object> stats = new HashMap<String, Object>();
        stats.put("totalCustomers", 10L);
        stats.put("totalPolicies", 5L);

        // Mock behavior
        when(reportService.adminDashboardStats()).thenReturn(stats);

        // Execute and Verify
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attribute("totalCustomers", 10L))
                .andExpect(model().attribute("totalPolicies", 5L));
    }

    @Test
    public void testCustomers() throws Exception {
        // Prepare list
        List<Customer> customerList = new ArrayList<Customer>();
        customerList.add(new Customer());

        when(customerRepository.findAll()).thenReturn(customerList);

        mockMvc.perform(get("/admin/customers"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/customers"))
                .andExpect(model().attributeExists("customers"));
    }

    @Test
    public void testVehicles() throws Exception {
        List<Vehicle> vehicleList = new ArrayList<Vehicle>();
        vehicleList.add(new Vehicle());

        when(vehicleRepository.findAll()).thenReturn(vehicleList);

        mockMvc.perform(get("/admin/vehicles"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/vehicles"))
                .andExpect(model().attributeExists("vehicles"));
    }

    @Test
    public void testPolicies() throws Exception {
        List<Policy> policyList = new ArrayList<Policy>();
        policyList.add(new Policy());

        when(policyRepository.findAll()).thenReturn(policyList);

        mockMvc.perform(get("/admin/policies"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/policies"))
                .andExpect(model().attributeExists("policies"));
    }
}