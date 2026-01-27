package com.cts.vis.controller;

import com.cts.vis.dto.PolicyDTO;
import com.cts.vis.service.PolicyService;
import com.cts.vis.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequiredArgsConstructor
@RequestMapping("/customer/policies")
public class PolicyController {

    private final PolicyService policyService;
    private final VehicleService vehicleService;

    @GetMapping
    public String policies(Model model) {
        model.addAttribute("policies", policyService.myPolicies());
        model.addAttribute("vehicles", vehicleService.myVehicles());
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("policy", new PolicyDTO.CreateRequest());
        return "customer/policies";
    }

    @PostMapping("/create")
    public String create(
            @Valid @ModelAttribute("policy") PolicyDTO.CreateRequest dto,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("policies", policyService.myPolicies());
            model.addAttribute("vehicles", vehicleService.myVehicles());
            model.addAttribute("today", LocalDate.now());
            return "customer/policies";
        }

        policyService.createPolicy(
                dto.getVehicleId(),
                dto.getCoverageAmount(),
                dto.getStartDate()
        );

        return "redirect:/customer/policies?created=true";
    }

    @PostMapping("/{policyId}/renew")
    public String renew(@PathVariable Long policyId) {
        policyService.renewPolicy(policyId);
        return "redirect:/customer/policies?renewed=true";
    }

    @GetMapping("/{policyId}/edit")
    public String edit(@PathVariable Long policyId, Model model) {
        model.addAttribute("policy", policyService.getMyPolicy(policyId));
        return "customer/policy-edit";
    }

    @PostMapping("/{policyId}/edit")
    public String update(
            @PathVariable Long policyId,
            @Valid @ModelAttribute("policy") PolicyDTO.UpdateRequest dto,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            return "customer/policy-edit";
        }

        policyService.updatePolicy(policyId, dto.getCoverageAmount());
        return "redirect:/customer/policies?updated=true";
    }
}