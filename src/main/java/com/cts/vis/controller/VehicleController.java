package com.cts.vis.controller;

import com.cts.vis.dto.VehicleDTO;
import com.cts.vis.model.Vehicle;
import com.cts.vis.model.VehicleType;
import com.cts.vis.service.ClaimService;
import com.cts.vis.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/customer/vehicles")
public class VehicleController {

    private final VehicleService vehicleService;
    private final ClaimService claimService;

    @GetMapping
    public String vehicles(Model model) {
        List<Vehicle> vehicles = vehicleService.myVehicles();

        Map<Long, Boolean> editDisabled = new HashMap<>();
        for (Vehicle v : vehicles) {
            editDisabled.put(
                    v.getVehicleId(),
                    claimService.hasApprovedClaimForVehicle(v.getVehicleId())
            );
        }

        model.addAttribute("vehicles", vehicles);
        model.addAttribute("editDisabled", editDisabled);
        model.addAttribute("types", VehicleType.values());
        model.addAttribute("vehicle", new VehicleDTO.CreateRequest());
        return "customer/vehicles";
    }

    @PostMapping("/add")
    public String add(@Valid @ModelAttribute("vehicle") VehicleDTO.CreateRequest dto,
                      BindingResult result,
                      Model model) {

        if (result.hasErrors()) {
            model.addAttribute("vehicles", vehicleService.myVehicles());
            model.addAttribute("types", VehicleType.values());
            return "customer/vehicles";
        }

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

    @GetMapping("/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {

        if (claimService.hasApprovedClaimForVehicle(id)) {
            return "redirect:/customer/vehicles?editLocked=true";
        }

        Vehicle v = vehicleService.getMyVehicle(id);

        VehicleDTO.UpdateRequest dto = new VehicleDTO.UpdateRequest();
        dto.setRegistrationNumber(v.getRegistrationNumber());
        dto.setMake(v.getMake());
        dto.setModel(v.getModel());
        dto.setYearOfManufacture(v.getYearOfManufacture());
        dto.setVehicleType(v.getVehicleType());

        model.addAttribute("id", id);
        model.addAttribute("vehicle", dto);
        model.addAttribute("types", VehicleType.values());
        return "customer/vehicle-edit";
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id,
                         @Valid @ModelAttribute("vehicle") VehicleDTO.UpdateRequest dto,
                         BindingResult result,
                         Model model) {

        if (claimService.hasApprovedClaimForVehicle(id)) {
            return "redirect:/customer/vehicles?editLocked=true";
        }

        if (result.hasErrors()) {
            model.addAttribute("id", id);
            model.addAttribute("types", VehicleType.values());
            return "customer/vehicle-edit";
        }

        try {
            vehicleService.updateVehicle(
                    id,
                    dto.getRegistrationNumber(),
                    dto.getMake(),
                    dto.getModel(),
                    dto.getYearOfManufacture(),
                    dto.getVehicleType()
            );
        } catch (Exception ex) {
            result.rejectValue("registrationNumber", "error.vehicle", ex.getMessage());
            model.addAttribute("id", id);
            model.addAttribute("types", VehicleType.values());
            return "customer/vehicle-edit";
        }

        return "redirect:/customer/vehicles?updated=true";
    }
}