package com.cts.vis.controller;

import com.cts.vis.dto.VehicleDTO;
import com.cts.vis.model.VehicleType;
import com.cts.vis.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/customer/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;

    /* ================= VIEW VEHICLES ================= */

    @GetMapping
    public String vehicles(Model model) {
        model.addAttribute("vehicles", vehicleService.myVehicles());
        model.addAttribute("types", VehicleType.values());
        model.addAttribute("vehicle", new VehicleDTO.CreateRequest());
        return "customer/vehicles";
    }
    /* ================= ADD VEHICLE ================= */

    @PostMapping("/add")
    public String add(
            @Valid @ModelAttribute("vehicle") VehicleDTO.CreateRequest dto,
            BindingResult result,
            Model model) {

        // ✅ DTO validation errors
        if (result.hasErrors()) {
            model.addAttribute("vehicles", vehicleService.myVehicles());
            model.addAttribute("types", VehicleType.values());
            return "customer/vehicles";
        }

        // ✅ Business validation (duplicate registration number)
        try {
            vehicleService.addVehicle(
                    dto.getRegistrationNumber(),
                    dto.getMake(),
                    dto.getModel(),
                    dto.getYearOfManufacture(),
                    dto.getVehicleType()
            );
        } catch (Exception ex) {
            result.rejectValue("registrationNumber", "error.vehicle", ex.getMessage());
            model.addAttribute("vehicles", vehicleService.myVehicles());
            model.addAttribute("types", VehicleType.values());
            return "customer/vehicles";
        }

        return "redirect:/customer/vehicles?added=true";
    }

    /* ================= EDIT VEHICLE ================= */

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("vehicle", vehicleService.getMyVehicle(id));
        model.addAttribute("types", VehicleType.values());
        return "customer/vehicle-edit";
    }

    /* ================= UPDATE VEHICLE ================= */

    @PostMapping("/{id}/edit")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("vehicle") VehicleDTO.UpdateRequest dto,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("types", VehicleType.values());
            return "customer/vehicle-edit";
        }

        vehicleService.updateVehicle(
                id,
                dto.getMake(),
                dto.getModel(),
                dto.getYearOfManufacture(),
                dto.getVehicleType()
        );

        return "redirect:/customer/vehicles?updated=true";
    }
}