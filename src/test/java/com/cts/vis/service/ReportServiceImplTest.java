package com.cts.vis.service;

import com.cts.vis.model.*;
import com.cts.vis.repository.ClaimRepository;
import com.cts.vis.repository.CustomerRepository;
import com.cts.vis.repository.PolicyRepository;
import com.cts.vis.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ReportServiceImplTest {

    @Mock private CustomerService customerService;
    @Mock private VehicleRepository vehicleRepository;
    @Mock private PolicyRepository policyRepository;
    @Mock private ClaimRepository claimRepository;
    @Mock private CustomerRepository customerRepository;

    @InjectMocks
    private ReportServiceImpl reportService;

    private Customer mockCustomer;
    private Vehicle mockVehicle;
    private Policy activePolicy;
    private Policy expiredPolicy;

    @BeforeEach
    void setUp() {
        mockCustomer = new Customer();
        mockCustomer.setCustomerId(1L);

        mockVehicle = new Vehicle();
        mockVehicle.setMake("Toyota");
        mockVehicle.setModel("Camry");

        activePolicy = new Policy();
        activePolicy.setPolicyStatus(PolicyStatus.ACTIVE);
        activePolicy.setPremiumAmount(new BigDecimal("1200.00"));
        activePolicy.setVehicle(mockVehicle);

        expiredPolicy = new Policy();
        expiredPolicy.setPolicyStatus(PolicyStatus.EXPIRED);
        expiredPolicy.setPremiumAmount(new BigDecimal("800.00"));
        expiredPolicy.setVehicle(mockVehicle);
    }

    @Test
    void testCustomerDashboardStats_Calculation() {
        // Arrange
        when(customerService.getCurrentCustomer()).thenReturn(mockCustomer);
        when(vehicleRepository.findByCustomer(mockCustomer)).thenReturn(Collections.singletonList(mockVehicle));
        when(policyRepository.findByVehicleIn(anyList())).thenReturn(Arrays.asList(activePolicy, expiredPolicy));
        when(claimRepository.findByPolicyIn(anyList())).thenReturn(new ArrayList<>());

        // Act
        Map<String, Object> stats = reportService.customerDashboardStats();

        // Assert
        assertEquals(1, stats.get("totalVehicles"));
        assertEquals(1L, stats.get("activePolicies")); // Only activePolicy counted
        assertEquals(0, new BigDecimal("2000.00").compareTo((BigDecimal) stats.get("totalPremium")));
    }

    @Test
    void testAdminDashboardStats() {
        // Arrange
        when(customerRepository.count()).thenReturn(10L);
        when(policyRepository.count()).thenReturn(50L);
        when(claimRepository.findByClaimStatus(ClaimStatus.SUBMITTED)).thenReturn(Arrays.asList(new Claim()));
        when(claimRepository.findByClaimStatus(ClaimStatus.APPROVED)).thenReturn(new ArrayList<>());

        // Act
        Map<String, Object> stats = reportService.adminDashboardStats();

        // Assert
        assertEquals(10L, stats.get("totalCustomers"));
        assertEquals(1L, stats.get("pendingClaims"));
        assertEquals(0L, stats.get("approvedClaims"));
    }

    @Test
    void testCustomerPolicyPdf_Generation() {
        // Arrange
        when(customerService.getCurrentCustomer()).thenReturn(mockCustomer);
        when(vehicleRepository.findByCustomer(mockCustomer)).thenReturn(Collections.singletonList(mockVehicle));
        when(policyRepository.findByVehicleIn(anyList())).thenReturn(Collections.singletonList(activePolicy));

        // Act
        byte[] pdfBytes = reportService.customerPolicyPdf();

        // Assert
        assertNotNull(pdfBytes);
        assertTrue(pdfBytes.length > 0);
        // Verify PDF Magic Number (%PDF-)
        assertEquals((byte) 0x25, pdfBytes[0]);
    }

    @Test
    void testCustomerClaimExcel_Generation() {
        // Arrange
        Claim mockClaim = new Claim();
        mockClaim.setClaimStatus(ClaimStatus.APPROVED);
        mockClaim.setClaimAmount(new BigDecimal("500.00"));
        mockClaim.setPolicy(activePolicy);

        when(customerService.getCurrentCustomer()).thenReturn(mockCustomer);
        when(vehicleRepository.findByCustomer(mockCustomer)).thenReturn(Collections.singletonList(mockVehicle));
        when(policyRepository.findByVehicleIn(anyList())).thenReturn(Collections.singletonList(activePolicy));
        when(claimRepository.findByPolicyIn(anyList())).thenReturn(Collections.singletonList(mockClaim));

        // Act
        byte[] excelBytes = reportService.customerClaimExcel();

        // Assert
        assertNotNull(excelBytes);
        assertTrue(excelBytes.length > 0);
    }
}