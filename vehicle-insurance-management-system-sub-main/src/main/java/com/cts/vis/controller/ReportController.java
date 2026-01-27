package com.cts.vis.controller;

import com.cts.vis.model.Claim;
import com.cts.vis.model.Policy;
import com.cts.vis.service.ReportService;
import com.cts.vis.util.PdfExporter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/customer/reports")
    public String reportsHome() {
        return "customer/reports";
    }

    @GetMapping("/customer/reports/policies")
    public String policyReport(Model model) {
        model.addAllAttributes(reportService.customerPolicyReport());
        return "customer/report-policy";
    }

    @GetMapping("/customer/reports/claims")
    public String claimReport(Model model) {
        model.addAllAttributes(reportService.customerClaimReport());
        return "customer/report-claim";
    }

    @GetMapping("/customer/reports/policies/pdf")
    public ResponseEntity<byte[]> policyPdf() {
        Map<String, Object> data = reportService.customerPolicyReport();
        @SuppressWarnings("unchecked")
        List<Policy> policies = (List<Policy>) data.get("policies");
        byte[] pdf = PdfExporter.exportCustomerPolicyReport(policies, data);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=POLICY_REPORT.pdf")
                .body(pdf);
    }

    @GetMapping("/customer/reports/claims/pdf")
    public ResponseEntity<byte[]> claimPdf() {
        Map<String, Object> data = reportService.customerClaimReport();
        @SuppressWarnings("unchecked")
        List<Claim> claims = (List<Claim>) data.get("claims");
        byte[] pdf = PdfExporter.exportCustomerClaimReport(claims, data);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=CLAIM_REPORT.pdf")
                .body(pdf);
    }
}