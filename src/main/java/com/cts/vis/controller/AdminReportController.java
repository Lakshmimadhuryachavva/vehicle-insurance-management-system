package com.cts.vis.controller;

import com.cts.vis.dto.ReportDTO;
import com.cts.vis.model.ReportType;
import com.cts.vis.service.AdminReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@Controller
@RequestMapping("/admin/reports")  // ✅ IMPORTANT: matches your HTML links
@RequiredArgsConstructor
public class AdminReportController {

    private final AdminReportService adminReportService;

    // ✅ PAGE: /admin/reports
    @GetMapping
    public String reportsPage(
            @RequestParam(required = false) ReportType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            Model model
    ) {
        ReportType t = (type == null) ? ReportType.CUSTOMER : type;
        LocalDate start = (startDate == null) ? LocalDate.now().minusMonths(6) : startDate;
        LocalDate end = (endDate == null) ? LocalDate.now() : endDate;

        Map<String, Object> data = adminReportService.generate(t, start, end);

        model.addAttribute("types", ReportType.values());
        model.addAttribute("type", t);
        model.addAttribute("start", start);
        model.addAttribute("end", end);

        // rows + summary keys (count, activeCount, totalPremium, approvedCount, totalClaimed)
        model.addAllAttributes(data);

        return "admin/reports";
    }

    // ✅ DOWNLOAD PDF: /admin/reports/download/pdf
    @GetMapping("/download/pdf")
    public ResponseEntity<byte[]> downloadPdf(@ModelAttribute ReportDTO.AdminFilterRequest filter) {

        ReportType type = (filter.getType() == null) ? ReportType.CUSTOMER : filter.getType();
        LocalDate start = (filter.getStartDate() == null) ? LocalDate.now().minusMonths(6) : filter.getStartDate();
        LocalDate end = (filter.getEndDate() == null) ? LocalDate.now() : filter.getEndDate();

        byte[] pdf = adminReportService.exportPdf(type, start, end);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + type + "_REPORT.pdf")
                .body(pdf);
    }

    // ✅ DOWNLOAD EXCEL: /admin/reports/download/excel
    @GetMapping("/download/excel")
    public ResponseEntity<byte[]> downloadExcel(@ModelAttribute ReportDTO.AdminFilterRequest filter) {

        ReportType type = (filter.getType() == null) ? ReportType.CUSTOMER : filter.getType();
        LocalDate start = (filter.getStartDate() == null) ? LocalDate.now().minusMonths(6) : filter.getStartDate();
        LocalDate end = (filter.getEndDate() == null) ? LocalDate.now() : filter.getEndDate();

        byte[] excel = adminReportService.exportExcel(type, start, end);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(
                        "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + type + "_REPORT.xlsx")
                .body(excel);
    }
}