package com.cts.vis.controller;

import com.cts.vis.dto.ReportDTO;
import com.cts.vis.model.Claim;
import com.cts.vis.model.Policy;
import com.cts.vis.service.ReportService;
import com.cts.vis.util.ExcelExporter;
import com.cts.vis.util.PdfExporter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @GetMapping("/customer/reports")
    public String reportsHome(Model model) {
        model.addAttribute("filter", new ReportDTO.CustomerFilterRequest());
        return "customer/reports";
    }

    @GetMapping("/customer/reports/policies")
    public String policyReport(@ModelAttribute("filter") ReportDTO.CustomerFilterRequest filter,
                               Model model) {

        model.addAllAttributes(reportService.customerPolicyReport());
        return "customer/report-policy";
    }

    @GetMapping("/customer/reports/claims")
    public String claimReport(@ModelAttribute("filter") ReportDTO.CustomerFilterRequest filter,
                              Model model) {

        model.addAllAttributes(reportService.customerClaimReport());
        return "customer/report-claim";
    }

    @GetMapping("/customer/reports/policies/pdf")
    public ResponseEntity<byte[]> policyPdf(
            @ModelAttribute ReportDTO.CustomerFilterRequest filter) {

        Map<String, Object> data = reportService.customerPolicyReport();
        List<Policy> policies = (List<Policy>) data.get("policies");

        byte[] pdf = PdfExporter.exportCustomerPolicyReport(policies, data);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=POLICY_REPORT.pdf")
                .body(pdf);
    }

    @GetMapping("/customer/reports/claims/pdf")
    public ResponseEntity<byte[]> claimPdf(
            @ModelAttribute ReportDTO.CustomerFilterRequest filter) {

        Map<String, Object> data = reportService.customerClaimReport();
        List<Claim> claims = (List<Claim>) data.get("claims");

        byte[] pdf = PdfExporter.exportCustomerClaimReport(claims, data);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=CLAIM_REPORT.pdf")
                .body(pdf);
    }

    @GetMapping("/customer/reports/policies/excel")
    public ResponseEntity<byte[]> policyExcel(
            @ModelAttribute ReportDTO.CustomerFilterRequest filter) {

        Map<String, Object> data = reportService.customerPolicyReport();
        List<Policy> policies = (List<Policy>) data.get("policies");

        byte[] excel = ExcelExporter.exportCustomerPolicyReport(policies, data);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=POLICY_REPORT.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excel);
    }

    @GetMapping("/customer/reports/claims/excel")
    public ResponseEntity<byte[]> claimExcel(
            @ModelAttribute ReportDTO.CustomerFilterRequest filter) {

        Map<String, Object> data = reportService.customerClaimReport();
        List<Claim> claims = (List<Claim>) data.get("claims");

        byte[] excel = ExcelExporter.exportCustomerClaimReport(claims, data);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=CLAIM_REPORT.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excel);
    }
}