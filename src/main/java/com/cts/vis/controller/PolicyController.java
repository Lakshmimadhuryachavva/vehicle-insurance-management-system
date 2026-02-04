package com.cts.vis.controller;

import com.cts.vis.dto.PolicyDTO;
import com.cts.vis.model.Policy;
import com.cts.vis.service.ClaimService;
import com.cts.vis.service.PolicyService;
import com.cts.vis.service.VehicleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/customer/policies")
public class PolicyController {

    private final PolicyService policyService;
    private final VehicleService vehicleService;
    private final ClaimService claimService;

    @GetMapping
    public String policies(Model model) {
        loadModel(model, new PolicyDTO.CreateRequest());
        return "customer/policies"; // ✅ matches templates/customer/policies.html
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("policy") PolicyDTO.CreateRequest dto,
                         BindingResult result,
                         Model model) {

        if (dto.getStartDate() != null && dto.getEndDate() != null
                && !dto.getEndDate().isAfter(dto.getStartDate())) {
            result.rejectValue("endDate", "endDate.invalid", "End date must be after start date");
        }

        if (result.hasErrors()) {
            loadModel(model, dto);
            return "customer/policies"; // ✅
        }

        try {
            policyService.createPolicy(dto.getVehicleId(), dto.getCoverageAmount(), dto.getStartDate(), dto.getEndDate());
        } catch (IllegalArgumentException ex) {
            result.reject("create.failed", ex.getMessage());
            loadModel(model, dto);
            return "customer/policies"; // ✅
        }

        return "redirect:/customer/policies?created=true";
    }

    @PostMapping("/{policyId}/renew")
    public String renew(@PathVariable Long policyId) {
        try {
            policyService.renewPolicy(policyId);
            return "redirect:/customer/policies?renewed=true";
        } catch (IllegalStateException ex) {
            return "redirect:/customer/policies?renewError=true";
        }
    }

    @GetMapping("/{policyId}/edit")
    public String edit(@PathVariable Long policyId, Model model) {

        if (claimService.hasApprovedClaimForPolicy(policyId)) {
            return "redirect:/customer/policies?editLocked=true";
        }

        model.addAttribute("policy", policyService.getMyPolicy(policyId));
        return "customer/policy-edit"; // ✅ matches templates/customer/policy-edit.html
    }

    @PostMapping("/{policyId}/edit")
    public String update(@PathVariable Long policyId,
                         @Valid @ModelAttribute("policy") PolicyDTO.UpdateRequest dto,
                         BindingResult result,
                         Model model) {

        if (claimService.hasApprovedClaimForPolicy(policyId)) {
            return "redirect:/customer/policies?editLocked=true";
        }

        if (result.hasErrors()) {
            model.addAttribute("policy", policyService.getMyPolicy(policyId));
            return "customer/policy-edit"; // ✅
        }

        policyService.updatePolicy(policyId, dto.getCoverageAmount());
        return "redirect:/customer/policies?updated=true";
    }

    private void loadModel(Model model, PolicyDTO.CreateRequest form) {

        List<Policy> policies = policyService.myPolicies();

        Map<Long, Boolean> editDisabled = new HashMap<>();
        for (Policy p : policies) {
            editDisabled.put(p.getPolicyId(),
                    claimService.hasApprovedClaimForPolicy(p.getPolicyId()));
        }

        model.addAttribute("policies", policies);
        model.addAttribute("editDisabled", editDisabled);

        model.addAttribute("vehicles", vehicleService.myVehicles());
        model.addAttribute("today", LocalDate.now());
        model.addAttribute("policy", form);
    }
}