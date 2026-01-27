package com.cts.vis.controller;

import com.cts.vis.dto.ReportDTO;
import com.cts.vis.model.ReportType;
import com.cts.vis.service.AdminReportService;
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
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDate;
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin/reports")
public class AdminReportController {

    private final AdminReportService adminReportService;

    @GetMapping
    public String reports(@ModelAttribute("filter") ReportDTO.AdminFilterRequest filter,
                          Model model) {

        ReportType type = (filter.getType() == null)
                ? ReportType.CUSTOMER
                : filter.getType();

        LocalDate start = (filter.getStartDate() == null)
                ? LocalDate.now().minusMonths(6)
                : filter.getStartDate();

        LocalDate end = (filter.getEndDate() == null)
                ? LocalDate.now()
                : filter.getEndDate();

        Map<String, Object> data = adminReportService.generate(type, start, end);

        model.addAttribute("types", ReportType.values());
        model.addAttribute("type", type);
        model.addAttribute("start", start);
        model.addAttribute("end", end);
        model.addAllAttributes(data);

        return "admin/reports";
    }

    @GetMapping("/download/pdf")
    public ResponseEntity<byte[]> downloadPdf(@ModelAttribute ReportDTO.AdminFilterRequest filter) {

        ReportType type = (filter.getType() == null)
                ? ReportType.CUSTOMER
                : filter.getType();

        LocalDate start = (filter.getStartDate() == null)
                ? LocalDate.now().minusMonths(6)
                : filter.getStartDate();

        LocalDate end = (filter.getEndDate() == null)
                ? LocalDate.now()
                : filter.getEndDate();

        Map<String, Object> data = adminReportService.generate(type, start, end);
        byte[] pdf = PdfExporter.exportAdminReport(type, data);

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + type + "_REPORT.pdf")
                .body(pdf);
    }

    @GetMapping("/download/excel")
    public ResponseEntity<byte[]> downloadExcel(@ModelAttribute ReportDTO.AdminFilterRequest filter) {

        ReportType type = (filter.getType() == null)
                ? ReportType.CUSTOMER
                : filter.getType();

        LocalDate start = (filter.getStartDate() == null)
                ? LocalDate.now().minusMonths(6)
                : filter.getStartDate();

        LocalDate end = (filter.getEndDate() == null)
                ? LocalDate.now()
                : filter.getEndDate();

        Map<String, Object> data = adminReportService.generate(type, start, end);
        byte[] excel = ExcelExporter.exportAdminReport(type, data);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=" + type + "_REPORT.xlsx")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(excel);
    }
}