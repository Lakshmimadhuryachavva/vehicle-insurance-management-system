package com.cts.vis.service;

import com.cts.vis.exception.NotFoundException;
import com.cts.vis.model.*;
import com.cts.vis.repository.ClaimRepository;
import com.cts.vis.repository.PolicyRepository;
import com.cts.vis.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ClaimServiceImplTest {

    @Mock
    private ClaimRepository claimRepository;
    @Mock
    private PolicyRepository policyRepository;
    @Mock
    private VehicleRepository vehicleRepository;
    @Mock
    private CustomerService customerService;

    @InjectMocks
    private ClaimServiceImpl claimService;

    private Customer mockCustomer;
    private Policy mockPolicy;
    private List<Vehicle> mockVehicles;

    @BeforeEach
    void setUp() {
        mockCustomer = new Customer();
        mockCustomer.setCustomerId(1L);

        Vehicle v = new Vehicle();
        v.setVehicleId(10L);
        mockVehicles = Collections.singletonList(v);

        mockPolicy = new Policy();
        mockPolicy.setPolicyId(100L);
        mockPolicy.setPolicyStatus(PolicyStatus.ACTIVE);
        mockPolicy.setCoverageAmount(new BigDecimal("5000.00"));
    }

    @Test
    void testFileClaim_Success() {
        // Arrange
        when(customerService.getCurrentCustomer()).thenReturn(mockCustomer);
        when(vehicleRepository.findByCustomer(mockCustomer)).thenReturn(mockVehicles);
        when(policyRepository.findByPolicyIdAndVehicleIn(eq(100L), eq(mockVehicles)))
                .thenReturn(Optional.of(mockPolicy));

        when(claimRepository.save(any(Claim.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Claim result = claimService.fileClaim(100L, new BigDecimal("2000.00"), "Accident");

        // Assert
        assertNotNull(result);
        assertEquals(ClaimStatus.SUBMITTED, result.getClaimStatus());
        assertEquals(new BigDecimal("2000.00"), result.getClaimAmount());
        verify(claimRepository).save(any(Claim.class));
    }

    @Test
    void testFileClaim_ThrowsException_WhenAmountExceedsCoverage() {
        // Arrange
        when(customerService.getCurrentCustomer()).thenReturn(mockCustomer);
        when(vehicleRepository.findByCustomer(mockCustomer)).thenReturn(mockVehicles);
        when(policyRepository.findByPolicyIdAndVehicleIn(100L, mockVehicles))
                .thenReturn(Optional.of(mockPolicy));

        // Act & Assert
        BigDecimal excessiveAmount = new BigDecimal("6000.00");
        Exception exception = assertThrows(IllegalArgumentException.class, () ->
                claimService.fileClaim(100L, excessiveAmount, "Total Loss")
        );

        assertEquals("Claim amount cannot exceed policy coverage amount", exception.getMessage());
        verify(claimRepository, never()).save(any());
    }

    @Test
    void testFileClaim_ThrowsException_WhenPolicyInactive() {
        // Arrange
        mockPolicy.setPolicyStatus(PolicyStatus.EXPIRED);
        when(customerService.getCurrentCustomer()).thenReturn(mockCustomer);
        when(vehicleRepository.findByCustomer(mockCustomer)).thenReturn(mockVehicles);
        when(policyRepository.findByPolicyIdAndVehicleIn(100L, mockVehicles))
                .thenReturn(Optional.of(mockPolicy));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                claimService.fileClaim(100L, new BigDecimal("100.00"), "Scratch")
        );
    }

    @Test
    void testApproveClaim_Success() {
        // Arrange
        Claim mockClaim = new Claim();
        mockClaim.setClaimId(500L);
        mockClaim.setClaimStatus(ClaimStatus.SUBMITTED);

        when(claimRepository.findById(500L)).thenReturn(Optional.of(mockClaim));
        when(claimRepository.save(any(Claim.class))).thenReturn(mockClaim);

        // Act
        Claim approved = claimService.approve(500L);

        // Assert
        assertEquals(ClaimStatus.APPROVED, approved.getClaimStatus());
        verify(claimRepository).save(mockClaim);
    }

    @Test
    void testApproveClaim_ThrowsNotFound() {
        // Arrange
        when(claimRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> claimService.approve(999L));
    }

    @Test
    void testRejectClaim_Success() {
        // Arrange
        Claim mockClaim = new Claim();
        mockClaim.setClaimId(500L);
        when(claimRepository.findById(500L)).thenReturn(Optional.of(mockClaim));
        when(claimRepository.save(any(Claim.class))).thenReturn(mockClaim);

        // Act
        Claim rejected = claimService.reject(500L);

        // Assert
        assertEquals(ClaimStatus.REJECTED, rejected.getClaimStatus());
    }
}