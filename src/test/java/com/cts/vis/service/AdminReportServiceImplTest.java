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
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AdminReportServiceImplTest {

    @Mock
    private CustomerRepository customerRepository;
    @Mock
    private VehicleRepository vehicleRepository;
    @Mock
    private PolicyRepository policyRepository;
    @Mock
    private ClaimRepository claimRepository;

    @InjectMocks
    private AdminReportServiceImpl adminReportService;

    private LocalDate start;
    private LocalDate end;

    @BeforeEach
    void setUp() {
        start = LocalDate.now().minusMonths(1);
        end = LocalDate.now();
    }

    @Test
    void testGenerateCustomerReport() {
        Customer c = new Customer();
        c.setCustomerId(1L);
        when(customerRepository.findByCreatedDateBetween(start, end))
                .thenReturn(Arrays.asList(c));

        Map<String, Object> result = adminReportService.generate(ReportType.CUSTOMER, start, end);

        assertEquals(1L, result.get("count"));
        verify(customerRepository).findByCreatedDateBetween(start, end);
    }

    @Test
    void testGeneratePolicyReport_CalculatesActiveAndPremium() {
        Policy p1 = new Policy();
        p1.setPolicyStatus(PolicyStatus.ACTIVE);
        p1.setPremiumAmount(new BigDecimal("1000.00"));

        Policy p2 = new Policy();
        // Fixed: Using EXPIRED as it is defined in your PolicyStatus enum
        p2.setPolicyStatus(PolicyStatus.EXPIRED);
        p2.setPremiumAmount(new BigDecimal("500.00"));

        when(policyRepository.findByStartDateBetween(start, end))
                .thenReturn(Arrays.asList(p1, p2));

        Map<String, Object> result = adminReportService.generate(ReportType.POLICY, start, end);

        assertEquals(2L, result.get("count"));
        assertEquals(1L, result.get("activeCount")); // Only p1 is ACTIVE
        assertEquals(0, new BigDecimal("1500.00").compareTo((BigDecimal) result.get("totalPremium")));
    }

    @Test
    void testGenerateClaimReport_CalculatesApprovedAndTotal() {
        Claim c1 = new Claim();
        c1.setClaimStatus(ClaimStatus.APPROVED);
        c1.setClaimAmount(new BigDecimal("5000.00"));

        Claim c2 = new Claim();
        // Fixed: Using REJECTED as it is defined in your ClaimStatus enum
        c2.setClaimStatus(ClaimStatus.REJECTED);
        c2.setClaimAmount(new BigDecimal("2000.00"));

        when(claimRepository.findByClaimDateBetween(start, end))
                .thenReturn(Arrays.asList(c1, c2));

        Map<String, Object> result = adminReportService.generate(ReportType.CLAIM, start, end);

        assertEquals(2L, result.get("count"));
        assertEquals(1L, result.get("approvedCount")); // Only c1 is APPROVED
        assertEquals(0, new BigDecimal("7000.00").compareTo((BigDecimal) result.get("totalClaimed")));
    }

    @Test
    void testExportPdf() {
        // Arrange
        when(customerRepository.findByCreatedDateBetween(any(), any()))
                .thenReturn(Arrays.asList(new Customer()));

        // Act
        byte[] pdf = adminReportService.exportPdf(ReportType.CUSTOMER, start, end);

        // Assert
        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
        // PDF header check (%)
        assertEquals((byte) 0x25, pdf[0]);
    }

    @Test
    void testExportExcel() {
        // Arrange
        when(vehicleRepository.findByCreatedDateBetween(any(), any()))
                .thenReturn(Arrays.asList(new Vehicle()));

        // Act
        byte[] excel = adminReportService.exportExcel(ReportType.VEHICLE, start, end);

        // Assert
        assertNotNull(excel);
        assertTrue(excel.length > 0);
    }
}