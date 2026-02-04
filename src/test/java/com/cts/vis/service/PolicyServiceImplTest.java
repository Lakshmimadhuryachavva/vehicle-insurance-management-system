package com.cts.vis.service;

import com.cts.vis.exception.NotFoundException;
import com.cts.vis.model.*;
import com.cts.vis.repository.PolicyRepository;
import com.cts.vis.repository.VehicleRepository;
import com.cts.vis.util.PolicyNumberGenerator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class PolicyServiceImplTest {

    @Mock
    private PolicyRepository policyRepository;
    @Mock
    private VehicleRepository vehicleRepository;
    @Mock
    private CustomerService customerService;
    @Mock
    private PolicyNumberGenerator policyNumberGenerator;

    @InjectMocks
    private PolicyServiceImpl policyService;

    private Customer mockCustomer;
    private Vehicle mockVehicle;

    @BeforeEach
    void setUp() {
        mockCustomer = new Customer();

        mockVehicle = new Vehicle();
        mockVehicle.setVehicleId(1L);
        mockVehicle.setVehicleType(VehicleType.CAR);
        mockVehicle.setYearOfManufacture(2020);
        mockVehicle.setCustomer(mockCustomer);
    }

    @Test
    void testCreatePolicy_Success() {
        // Arrange
        LocalDate start = LocalDate.now();
        LocalDate end = start.plusYears(1);
        String generatedNumber = "POL-12345";

        when(customerService.getCurrentCustomer()).thenReturn(mockCustomer);
        when(vehicleRepository.findByVehicleIdAndCustomer(1L, mockCustomer)).thenReturn(Optional.of(mockVehicle));
        when(policyNumberGenerator.generate()).thenReturn(generatedNumber);
        when(policyRepository.existsByPolicyNumber(generatedNumber)).thenReturn(false);
        when(policyRepository.save(any(Policy.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Policy policy = policyService.createPolicy(1L, new BigDecimal("50000"), start, end);

        // Assert
        assertNotNull(policy);
        assertEquals(generatedNumber, policy.getPolicyNumber());
        assertEquals(PolicyStatus.ACTIVE, policy.getPolicyStatus());
        assertNotNull(policy.getPremiumAmount());
        verify(policyRepository).save(any(Policy.class));
    }

    @Test
    void testCreatePolicy_ThrowsException_InvalidDates() {
        // Arrange
        LocalDate start = LocalDate.now();
        LocalDate end = start.minusDays(1); // Invalid: end before start

        when(customerService.getCurrentCustomer()).thenReturn(mockCustomer);
        when(vehicleRepository.findByVehicleIdAndCustomer(1L, mockCustomer)).thenReturn(Optional.of(mockVehicle));

        // Act & Assert
        Exception ex = assertThrows(IllegalArgumentException.class, () ->
                policyService.createPolicy(1L, new BigDecimal("50000"), start, end)
        );
        assertEquals("End date must be after start date.", ex.getMessage());
    }

    @Test
    void testRenewPolicy_Success() {
        // Arrange
        Policy expiredPolicy = new Policy();
        expiredPolicy.setPolicyId(10L);
        expiredPolicy.setPolicyStatus(PolicyStatus.EXPIRED); // Must be expired to renew
        expiredPolicy.setEndDate(LocalDate.now().minusDays(1));
        expiredPolicy.setVehicle(mockVehicle);

        // getMyPolicy is called internally, so we need to mock its dependencies
        when(customerService.getCurrentCustomer()).thenReturn(mockCustomer);
        when(vehicleRepository.findByCustomer(mockCustomer)).thenReturn(Arrays.asList(mockVehicle));
        when(policyRepository.findByPolicyIdAndVehicleIn(eq(10L), anyList())).thenReturn(Optional.of(expiredPolicy));
        when(policyRepository.save(any(Policy.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Policy renewed = policyService.renewPolicy(10L);

        // Assert
        assertEquals(PolicyStatus.ACTIVE, renewed.getPolicyStatus());
        assertEquals(LocalDate.now(), renewed.getStartDate());
        verify(policyRepository).save(expiredPolicy);
    }

    @Test
    void testRenewPolicy_ThrowsException_IfStillActive() {
        // Arrange
        Policy activePolicy = new Policy();
        activePolicy.setPolicyStatus(PolicyStatus.ACTIVE);
        activePolicy.setEndDate(LocalDate.now().plusMonths(6));
        activePolicy.setVehicle(mockVehicle);

        when(customerService.getCurrentCustomer()).thenReturn(mockCustomer);
        when(vehicleRepository.findByCustomer(mockCustomer)).thenReturn(Arrays.asList(mockVehicle));
        when(policyRepository.findByPolicyIdAndVehicleIn(anyLong(), anyList())).thenReturn(Optional.of(activePolicy));

        // Act & Assert
        assertThrows(IllegalStateException.class, () -> policyService.renewPolicy(10L));
    }

    @Test
    void testCalculatePremium_Logic() {
        // This is a private method test via public createPolicy
        // CAR (1000) * Age 4 (1.10) + Coverage (50000 * 0.008 = 400) = 1500.00
        mockVehicle.setYearOfManufacture(LocalDate.now().getYear() - 4);

        when(customerService.getCurrentCustomer()).thenReturn(mockCustomer);
        when(vehicleRepository.findByVehicleIdAndCustomer(1L, mockCustomer)).thenReturn(Optional.of(mockVehicle));
        when(policyNumberGenerator.generate()).thenReturn("P1");
        when(policyRepository.save(any(Policy.class))).thenAnswer(i -> i.getArguments()[0]);

        Policy p = policyService.createPolicy(1L, new BigDecimal("50000"), LocalDate.now(), LocalDate.now().plusYears(1));

        assertEquals(new BigDecimal("1500.00"), p.getPremiumAmount());
    }
}