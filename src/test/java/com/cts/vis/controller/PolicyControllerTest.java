package com.cts.vis.controller;

import com.cts.vis.dto.PolicyDTO;
import com.cts.vis.model.Policy;
import com.cts.vis.model.Vehicle;
import com.cts.vis.service.ClaimService;
import com.cts.vis.service.PolicyService;
import com.cts.vis.service.VehicleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class PolicyControllerTest {

    private MockMvc mockMvc;

    @Mock
    private PolicyService policyService;

    @Mock
    private VehicleService vehicleService;

    @Mock
    private ClaimService claimService;

    @InjectMocks
    private PolicyController policyController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(policyController).build();
    }

    @Test
    public void testPoliciesPage() throws Exception {
        // Prepare mock lists
        List<Policy> mockPolicies = new ArrayList<Policy>();
        List<Vehicle> mockVehicles = new ArrayList<Vehicle>();

        when(policyService.myPolicies()).thenReturn(mockPolicies);
        when(vehicleService.myVehicles()).thenReturn(mockVehicles);

        mockMvc.perform(get("/customer/policies"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer/policies"))
                .andExpect(model().attributeExists("policies", "vehicles", "editDisabled", "today"));
    }

    @Test
    public void testCreatePolicySuccess() throws Exception {
        mockMvc.perform(post("/customer/policies/create")
                        .param("vehicleId", "1")
                        .param("coverageAmount", "10000")
                        .param("startDate", LocalDate.now().toString())
                        .param("endDate", LocalDate.now().plusYears(1).toString()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customer/policies?created=true"));

        verify(policyService, times(1)).createPolicy(eq(1L), any(BigDecimal.class), any(LocalDate.class), any(LocalDate.class));
    }

    @Test
    public void testCreatePolicyDateError() throws Exception {
        // Start date after end date
        mockMvc.perform(post("/customer/policies/create")
                        .param("vehicleId", "1")
                        .param("coverageAmount", "10000")
                        .param("startDate", "2026-12-31")
                        .param("endDate", "2026-01-01"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer/policies"))
                .andExpect(model().hasErrors()); // result.rejectValue for date logic
    }

    @Test
    public void testRenewPolicy() throws Exception {
        mockMvc.perform(post("/customer/policies/1/renew"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customer/policies?renewed=true"));

        verify(policyService).renewPolicy(1L);
    }

    @Test
    public void testEditLockedByClaim() throws Exception {
        // Simulate an approved claim exists
        when(claimService.hasApprovedClaimForPolicy(1L)).thenReturn(true);

        mockMvc.perform(get("/customer/policies/1/edit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customer/policies?editLocked=true"));
    }

    @Test
    public void testUpdatePolicySuccess() throws Exception {
        when(claimService.hasApprovedClaimForPolicy(1L)).thenReturn(false);

        mockMvc.perform(post("/customer/policies/1/edit")
                        .param("coverageAmount", "15000"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customer/policies?updated=true"));

        verify(policyService).updatePolicy(eq(1L), any(BigDecimal.class));
    }
}