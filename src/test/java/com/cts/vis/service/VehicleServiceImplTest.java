package com.cts.vis.service;

import com.cts.vis.exception.NotFoundException;
import com.cts.vis.model.Customer;
import com.cts.vis.model.Vehicle;
import com.cts.vis.model.VehicleType;
import com.cts.vis.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class VehicleServiceImplTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private VehicleServiceImpl vehicleService;

    private Customer mockCustomer;
    private Vehicle mockVehicle;

    @BeforeEach
    void setUp() {
        mockCustomer = new Customer();
        mockCustomer.setCustomerId(1L);

        mockVehicle = new Vehicle();
        mockVehicle.setVehicleId(100L);
        mockVehicle.setRegistrationNumber("TN-01-AB-1234");
        mockVehicle.setCustomer(mockCustomer);
    }

    @Test
    void testAddVehicle_Success() {
        // Arrange
        when(vehicleRepository.existsByRegistrationNumber("TN-01-AB-1234")).thenReturn(false);
        when(customerService.getCurrentCustomer()).thenReturn(mockCustomer);
        when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Vehicle result = vehicleService.addVehicle("TN-01-AB-1234", "Honda", "Civic", 2022, VehicleType.CAR);

        // Assert
        assertNotNull(result);
        assertEquals("TN-01-AB-1234", result.getRegistrationNumber());
        assertEquals(mockCustomer, result.getCustomer());
        verify(vehicleRepository).save(any(Vehicle.class));
    }

    @Test
    void testAddVehicle_ThrowsException_WhenRegNoExists() {
        // Arrange
        when(vehicleRepository.existsByRegistrationNumber("TN-01-AB-1234")).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                vehicleService.addVehicle("TN-01-AB-1234", "Honda", "Civic", 2022, VehicleType.CAR)
        );
        verify(vehicleRepository, never()).save(any());
    }

    @Test
    void testMyVehicles_ReturnsList() {
        // Arrange
        when(customerService.getCurrentCustomer()).thenReturn(mockCustomer);
        when(vehicleRepository.findByCustomer(mockCustomer)).thenReturn(Arrays.asList(mockVehicle));

        // Act
        List<Vehicle> list = vehicleService.myVehicles();

        // Assert
        assertEquals(1, list.size());
        assertEquals("TN-01-AB-1234", list.get(0).getRegistrationNumber());
    }

    @Test
    void testGetMyVehicle_Success() {
        // Arrange
        when(customerService.getCurrentCustomer()).thenReturn(mockCustomer);
        when(vehicleRepository.findByVehicleIdAndCustomer(100L, mockCustomer)).thenReturn(Optional.of(mockVehicle));

        // Act
        Vehicle result = vehicleService.getMyVehicle(100L);

        // Assert
        assertNotNull(result);
        assertEquals(100L, result.getVehicleId());
    }

    @Test
    void testGetMyVehicle_ThrowsNotFound() {
        // Arrange
        when(customerService.getCurrentCustomer()).thenReturn(mockCustomer);
        when(vehicleRepository.findByVehicleIdAndCustomer(999L, mockCustomer)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(NotFoundException.class, () -> vehicleService.getMyVehicle(999L));
    }

    @Test
    void testUpdateVehicle_WithRegNoChange_ChecksUniqueness() {
        // Arrange
        String newReg = "KA-05-XY-9999";
        when(customerService.getCurrentCustomer()).thenReturn(mockCustomer);
        when(vehicleRepository.findByVehicleIdAndCustomer(100L, mockCustomer)).thenReturn(Optional.of(mockVehicle));
        when(vehicleRepository.existsByRegistrationNumber(newReg)).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () ->
                vehicleService.updateVehicle(100L, newReg, "Honda", "City", 2023, VehicleType.CAR)
        );
    }
}