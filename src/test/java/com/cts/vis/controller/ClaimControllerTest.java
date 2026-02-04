package com.cts.vis.controller;

import com.cts.vis.dto.ClaimDTO;
import com.cts.vis.model.Claim;
import com.cts.vis.model.Policy;
import com.cts.vis.service.ClaimService;
import com.cts.vis.service.PolicyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ClaimControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ClaimService claimService;

    @Mock
    private PolicyService policyService;

    @InjectMocks
    private ClaimController claimController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(claimController).build();
    }

    @Test
    public void testClaimsPageView() throws Exception {
        // Prepare mock data
        List<Policy> mockPolicies = new ArrayList<Policy>();
        List<Claim> mockClaims = new ArrayList<Claim>();

        when(policyService.myPolicies()).thenReturn(mockPolicies);
        when(claimService.myClaims()).thenReturn(mockClaims);

        // Execute and verify
        mockMvc.perform(get("/customer/claims"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer/claims"))
                .andExpect(model().attributeExists("policies"))
                .andExpect(model().attributeExists("claims"))
                .andExpect(model().attributeExists("claim"));
    }

    @Test
    public void testFileClaimSuccess() throws Exception {
        // Execute POST request with parameters matching ClaimDTO.FileRequest
        mockMvc.perform(post("/customer/claims/file")
                        .param("policyId", "101")
                        .param("claimAmount", "5000.00")
                        .param("claimReason", "Accident damage"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/customer/claims?submitted=true"));

        // Verify the service was called once
        verify(claimService, times(1)).fileClaim(eq(101L), any(BigDecimal.class), eq("Accident damage"));
    }

    @Test
    public void testFileClaimBusinessError() throws Exception {
        // Simulate a business exception (e.g., claim exceeds coverage)
        doThrow(new RuntimeException("Claim amount exceeds coverage"))
                .when(claimService).fileClaim(anyLong(), any(BigDecimal.class), anyString());

        // Prepare some mock data for the model reload
        when(policyService.myPolicies()).thenReturn(new ArrayList<Policy>());
        when(claimService.myClaims()).thenReturn(new ArrayList<Claim>());

        mockMvc.perform(post("/customer/claims/file")
                        .param("policyId", "101")
                        .param("claimAmount", "999999.00")
                        .param("claimReason", "Too expensive"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer/claims"))
                .andExpect(model().hasErrors()) // Error added in the catch block
                .andExpect(model().attributeExists("policies"))
                .andExpect(model().attributeExists("claims"));
    }
}