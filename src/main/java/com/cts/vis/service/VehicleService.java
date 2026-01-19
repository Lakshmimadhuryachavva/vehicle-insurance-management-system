package com.cts.vis.service;

import com.cts.vis.model.Vehicle;
import com.cts.vis.model.VehicleType;

import java.util.List;

public interface VehicleService {
    Vehicle addVehicle(String regNo, String make, String model, int year, VehicleType type);
    List<Vehicle> myVehicles();
    Vehicle getMyVehicle(Long id);
    Vehicle updateVehicle(Long id, String make, String model, int year, VehicleType type);
}