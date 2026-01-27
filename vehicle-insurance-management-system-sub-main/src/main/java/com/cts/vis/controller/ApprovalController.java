package com.cts.vis.controller;

import com.cts.vis.service.ClaimService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/approvals")
public class ApprovalController {

    private final ClaimService claimService;

    @GetMapping
    public String approvals(Model model) {
        model.addAttribute("claims", claimService.submittedClaims());
        return "admin/approvals";
    }

    @PostMapping("/{claimId}/approve")
    public String approve(@PathVariable Long claimId) {
        claimService.approve(claimId);
        return "redirect:/admin/approvals?approved=true";
    }

    @PostMapping("/{claimId}/reject")
    public String reject(@PathVariable Long claimId) {
        claimService.reject(claimId);
        return "redirect:/admin/approvals?rejected=true";
    }
}