package com.cts.vis.controller;

import com.cts.vis.model.VehicleType;
import com.cts.vis.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/customer/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    @GetMapping
    public String vehicles(Model model) {
        model.addAttribute("vehicles", vehicleService.myVehicles());
        model.addAttribute("types", VehicleType.values());
        return "customer/vehicles";
    }

    @PostMapping("/add")
    public String add(@RequestParam String registrationNumber,
                      @RequestParam String make,
                      @RequestParam String model,
                      @RequestParam int yearOfManufacture,
                      @RequestParam VehicleType vehicleType,
                      Model m) {
        try {
            vehicleService.addVehicle(registrationNumber, make, model, yearOfManufacture, vehicleType);
            return "redirect:/customer/vehicles?added=true";
        } catch (Exception ex) {
            m.addAttribute("error", ex.getMessage());
            m.addAttribute("vehicles", vehicleService.myVehicles());
            m.addAttribute("types", VehicleType.values());
            return "customer/vehicles";
        }
    }

    @GetMapping("/{id}/edit")
    public String editVehicle(@PathVariable Long id, Model model) {
        model.addAttribute("vehicle", vehicleService.getMyVehicle(id));
        model.addAttribute("types", VehicleType.values());
        return "customer/vehicle-edit";
    }

    @PostMapping("/{id}/edit")
    public String updateVehicle(@PathVariable Long id,
                                @RequestParam String make,
                                @RequestParam String model,
                                @RequestParam int yearOfManufacture,
                                @RequestParam VehicleType vehicleType) {
        vehicleService.updateVehicle(id, make, model, yearOfManufacture, vehicleType);
        return "redirect:/customer/vehicles?updated=true";
    }
}