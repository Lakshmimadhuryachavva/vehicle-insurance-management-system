package com.cts.vis.controller;

import com.cts.vis.model.Claim;
import com.cts.vis.service.ClaimService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.ArrayList;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ApprovalControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ClaimService claimService;

    @InjectMocks
    private ApprovalController approvalController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(approvalController).build();
    }

    @Test
    public void testApprovalsPage() throws Exception {
        // Prepare mock data
        List<Claim> mockClaims = new ArrayList<Claim>();
        mockClaims.add(new Claim());

        // Mock service call
        when(claimService.submittedClaims()).thenReturn(mockClaims);

        // Execute and verify
        mockMvc.perform(get("/admin/approvals"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/approvals"))
                .andExpect(model().attribute("claims", mockClaims));

        verify(claimService, times(1)).submittedClaims();
    }

    @Test
    public void testApproveClaim() throws Exception {
        Long claimId = 1L;

        // Execute POST request
        mockMvc.perform(post("/admin/approvals/" + claimId + "/approve"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/approvals?approved=true"));

        // Verify the service method was actually called
        verify(claimService, times(1)).approve(claimId);
    }

    @Test
    public void testRejectClaim() throws Exception {
        Long claimId = 2L;

        // Execute POST request
        mockMvc.perform(post("/admin/approvals/" + claimId + "/reject"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/approvals?rejected=true"));

        // Verify the service method was actually called
        verify(claimService, times(1)).reject(claimId);
    }
}