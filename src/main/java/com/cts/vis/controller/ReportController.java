package com.cts.vis.controller;

import com.cts.vis.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/customer/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    // PAGE: /customer/reports
    @GetMapping
    public String reportsHome() {
        return "customer/reports";
    }

    // PAGE: /customer/reports/policies
    @GetMapping("/policies")
    public String policyReportPage(Model model) {
        var data = reportService.customerPolicyReport();
        model.addAllAttributes(data);
        return "customer/report-policy";
    }

    // PAGE: /customer/reports/claims
    @GetMapping("/claims")
    public String claimReportPage(Model model) {
        var data = reportService.customerClaimReport();
        model.addAllAttributes(data);
        return "customer/report-claim";
    }

    // DOWNLOADS
    @GetMapping("/policies/pdf")
    public ResponseEntity<byte[]> policyPdf() {
        byte[] pdf = reportService.customerPolicyPdf();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=POLICY_REPORT.pdf")
                .body(pdf);
    }

    @GetMapping("/policies/excel")
    public ResponseEntity<byte[]> policyExcel() {
        byte[] excel = reportService.customerPolicyExcel();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=POLICY_REPORT.xlsx")
                .body(excel);
    }

    @GetMapping("/claims/pdf")
    public ResponseEntity<byte[]> claimPdf() {
        byte[] pdf = reportService.customerClaimPdf();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=CLAIM_REPORT.pdf")
                .body(pdf);
    }

    @GetMapping("/claims/excel")
    public ResponseEntity<byte[]> claimExcel() {
        byte[] excel = reportService.customerClaimExcel();
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=CLAIM_REPORT.xlsx")
                .body(excel);
    }
}