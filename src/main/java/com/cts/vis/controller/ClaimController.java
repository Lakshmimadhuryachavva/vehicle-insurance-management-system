package com.cts.vis.controller;

import com.cts.vis.dto.ClaimDTO;
import com.cts.vis.service.ClaimService;
import com.cts.vis.service.PolicyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
@RequiredArgsConstructor
public class ClaimController {

    private final ClaimService claimService;
    private final PolicyService policyService;

    @GetMapping("/customer/claims")
    public String claims(Model model) {
        model.addAttribute("policies", policyService.myPolicies());
        model.addAttribute("claims", claimService.myClaims());
        model.addAttribute("claim", new ClaimDTO.FileRequest());
        return "customer/claims";
    }

    @PostMapping("/customer/claims/file")
    public String file(
            @Valid @ModelAttribute("claim") ClaimDTO.FileRequest dto,
            BindingResult result,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("policies", policyService.myPolicies());
            model.addAttribute("claims", claimService.myClaims());
            return "customer/claims";
        }

        try {
            claimService.fileClaim(
                    dto.getPolicyId(),
                    dto.getClaimAmount(),
                    dto.getClaimReason()
            );
        } catch (Exception ex) {
            result.rejectValue("claimAmount", "error.claim", ex.getMessage());
            model.addAttribute("policies", policyService.myPolicies());
            model.addAttribute("claims", claimService.myClaims());
            return "customer/claims";
        }

        return "redirect:/customer/claims?submitted=true";
    }
}