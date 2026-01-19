package com.cts.vis.controller;

import com.cts.vis.service.PolicyService;
import com.cts.vis.service.VehicleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
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
        return "customer/policies";
    }

    @PostMapping("/create")
    public String create(@RequestParam Long vehicleId,
                         @RequestParam BigDecimal coverageAmount,
                         @RequestParam LocalDate startDate) {
        policyService.createPolicy(vehicleId, coverageAmount, startDate);
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
    public String update(@PathVariable Long policyId,
                         @RequestParam BigDecimal coverageAmount) {
        policyService.updatePolicy(policyId, coverageAmount);
        return "redirect:/customer/policies?updated=true";
    }
}