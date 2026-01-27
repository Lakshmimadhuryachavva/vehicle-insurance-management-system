package com.cts.vis.controller;

import com.cts.vis.model.ReportType;
import com.cts.vis.service.AdminReportService;
import com.cts.vis.util.PdfExporter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/reports")
public class AdminReportController {

    private final AdminReportService adminReportService;

    @GetMapping
    public String reports(@RequestParam(defaultValue = "CUSTOMER") ReportType type,
                          @RequestParam(required = false) LocalDate startDate,
                          @RequestParam(required = false) LocalDate endDate,
                          Model model) {

        LocalDate start = (startDate == null) ? LocalDate.now().minusMonths(6) : startDate;
        LocalDate end = (endDate == null) ? LocalDate.now() : endDate;

        Map<String, Object> data = adminReportService.generate(type, start, end);

        model.addAttribute("types", ReportType.values());
        model.addAllAttributes(data);
        return "admin/reports";
    }

    @GetMapping("/download/pdf")
    public ResponseEntity<byte[]> downloadPdf(@RequestParam ReportType type,
                                              @RequestParam LocalDate startDate,
                                              @RequestParam LocalDate endDate) {

        Map<String, Object> data = adminReportService.generate(type, startDate, endDate);
        byte[] pdf = PdfExporter.exportAdminReport(type, data);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + type + "_REPORT.pdf")
                .body(pdf);
    }
}