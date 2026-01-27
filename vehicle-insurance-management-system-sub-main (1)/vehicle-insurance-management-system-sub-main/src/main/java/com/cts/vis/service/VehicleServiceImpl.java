package com.cts.vis.service;

import com.cts.vis.model.Customer;
import com.cts.vis.model.Vehicle;
import com.cts.vis.model.VehicleType;
import com.cts.vis.repository.VehicleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class VehicleServiceImpl implements VehicleService {

    private final VehicleRepository vehicleRepository;
    private final CustomerService customerService;

    @Override
    @Transactional
    public Vehicle addVehicle(String regNo, String make, String model, int year, VehicleType type) {
        if (vehicleRepository.existsByRegistrationNumber(regNo)) {
            throw new IllegalArgumentException("Registration number already exists.");
        }
        Customer customer = customerService.getCurrentCustomer();
        return vehicleRepository.save(Vehicle.builder()
                .customer(customer)
                .registrationNumber(regNo)
                .make(make)
                .model(model)
                .yearOfManufacture(year)
                .vehicleType(type)
                .build());
    }

    @Override
    public List<Vehicle> myVehicles() {
        Customer customer = customerService.getCurrentCustomer();
        return vehicleRepository.findByCustomer(customer);
    }

    @Override
    public Vehicle getMyVehicle(Long id) {
        Customer customer = customerService.getCurrentCustomer();
        return vehicleRepository.findByVehicleIdAndCustomer(id, customer)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found."));
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