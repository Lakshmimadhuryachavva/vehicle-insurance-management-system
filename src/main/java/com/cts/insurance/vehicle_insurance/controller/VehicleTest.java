package com.cts.insurance.vehicle_insurance.controller;
import com.cts.insurance.vehicle_insurance.model.Vehicle;
import com.cts.insurance.vehicle_insurance.repository.vehicleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
@RestController
@RequestMapping("/DB")
public class VehicleTest {
    @Autowired
    public vehicleRepository vr;
    @GetMapping("/vehicle")
    public List<Vehicle> getvehicle(){
        return vr.findAll();}
    @PostMapping("/addvehicle")
    public Vehicle insertvehicle(@RequestBody Vehicle vehicle){
        return vr.save(vehicle);
    }

}
