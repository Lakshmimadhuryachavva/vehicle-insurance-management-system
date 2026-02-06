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
@RequestMapping("/admin/reports")
@RequiredArgsConstructor
public class AdminReportController {
    private final AdminReportService adminReportService;
    @GetMapping
    public String reportsPage(@ModelAttribute ReportDTO.AdminFilterRequest filter, Model model) {
        // Simply pass whatever the UI sent (or didn't send) to the service
        Map<String, Object> data = adminReportService.generate(filter.getType(), filter.getStartDate(), filter.getEndDate());

        model.addAttribute("types", ReportType.values());
        model.addAllAttributes(data);
        return "admin/reports";
    }

    @GetMapping("/download/pdf")
    public ResponseEntity<byte[]> downloadPdf(@ModelAttribute ReportDTO.AdminFilterRequest filter) {
        byte[] pdf = adminReportService.exportPdf(filter.getType(), filter.getStartDate(), filter.getEndDate());
        String filename = (filter.getType() != null ? filter.getType() : "GENERAL") + "_REPORT.pdf";

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .body(pdf);
    }

    @GetMapping("/download/excel")
    public ResponseEntity<byte[]> downloadExcel(@ModelAttribute ReportDTO.AdminFilterRequest filter) {
        byte[] excel = adminReportService.exportExcel(filter.getType(), filter.getStartDate(), filter.getEndDate());
        String filename = (filter.getType() != null ? filter.getType() : "GENERAL") + "_REPORT.xlsx";

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + filename)
                .body(excel);
    }
}