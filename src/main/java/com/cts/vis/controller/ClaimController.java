package com.cts.vis.controller;

import com.cts.vis.service.ClaimService;
import com.cts.vis.service.PolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@Controller
@RequiredArgsConstructor
public class ClaimController {

    private final ClaimService claimService;
    private final PolicyService policyService;

    @GetMapping("/customer/claims")
    public String claims(Model model) {
        model.addAttribute("policies", policyService.myPolicies());
        model.addAttribute("claims", claimService.myClaims());
        return "customer/claims";
    }

    @PostMapping("/customer/claims/file")
    public String file(@RequestParam Long policyId,
                       @RequestParam BigDecimal claimAmount,
                       @RequestParam String claimReason) {
        claimService.fileClaim(policyId, claimAmount, claimReason);
        return "redirect:/customer/claims?submitted=true";
    }
}