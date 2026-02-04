package com.cts.vis.service;

import com.cts.vis.model.Customer;
import com.cts.vis.model.Vehicle;
import com.cts.vis.model.VehicleType;
import com.cts.vis.repository.VehicleRepository;
import com.cts.vis.exception.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final CustomerService customerService;

    @Override
    @Transactional
    public Vehicle addVehicle(String regNo, String make, String model, int year, VehicleType type) {
        // Simple IF check instead of functional validation
        boolean exists = vehicleRepository.existsByRegistrationNumber(regNo);
        if (exists) {
            throw new IllegalArgumentException("Registration number already exists: " + regNo);
        }

        Customer customer = customerService.getCurrentCustomer();

        // Using standard setters instead of the Builder pattern
        Vehicle vehicle = new Vehicle();
        vehicle.setCustomer(customer);
        vehicle.setRegistrationNumber(regNo);
        vehicle.setMake(make);
        vehicle.setModel(model);
        vehicle.setYearOfManufacture(year);
        vehicle.setVehicleType(type);
        vehicle.setCreatedDate(LocalDate.now());

        return vehicleRepository.save(vehicle);
    }

    @Override
    public List<Vehicle> myVehicles() {
        Customer customer = customerService.getCurrentCustomer();
        return vehicleRepository.findByCustomer(customer);
    }

    @Override
    public Vehicle getMyVehicle(Long id) {
        Customer customer = customerService.getCurrentCustomer();

        // Manual Optional handling (No .orElseThrow lambda)
        Optional<Vehicle> vOpt = vehicleRepository.findByVehicleIdAndCustomer(id, customer);
        if (!vOpt.isPresent()) {
            throw new NotFoundException("Vehicle not found for ID: " + id);
        }

        return vOpt.get();
    }

    @Override
    @Transactional
    public Vehicle updateVehicle(Long id, String regNo, String make, String model, int year, VehicleType type) {
        // Reuse our helper method
        Vehicle v = getMyVehicle(id);

        // Standard comparison and nested IF
        if (!v.getRegistrationNumber().equals(regNo)) {
            if (vehicleRepository.existsByRegistrationNumber(regNo)) {
                throw new IllegalArgumentException("Registration number already exists.");
            }
        }

        v.setRegistrationNumber(regNo);
        v.setMake(make);
        v.setModel(model);
        v.setYearOfManufacture(year);
        v.setVehicleType(type);

        return vehicleRepository.save(v);
    }

    @Override
    @Transactional
    public Vehicle updateVehicle(Long id, String make, String model, int year, VehicleType type) {
        Vehicle v = getMyVehicle(id);

        v.setMake(make);
        v.setModel(model);
        v.setYearOfManufacture(year);
        v.setVehicleType(type);

        return vehicleRepository.save(v);
    }
}