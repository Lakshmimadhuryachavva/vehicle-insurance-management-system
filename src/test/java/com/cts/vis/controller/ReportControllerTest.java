package com.cts.vis.controller;

import com.cts.vis.service.ReportService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class ReportControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ReportService reportService;

    @InjectMocks
    private ReportController reportController;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        this.mockMvc = MockMvcBuilders.standaloneSetup(reportController).build();
    }

    @Test
    public void testReportsHome() throws Exception {
        mockMvc.perform(get("/customer/reports"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer/reports"));
    }

    @Test
    public void testPolicyReportPage() throws Exception {
        Map<String, Object> mockData = new HashMap<String, Object>();
        mockData.put("totalPremium", 1500.00);

        when(reportService.customerPolicyReport()).thenReturn(mockData);

        mockMvc.perform(get("/customer/reports/policies"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer/report-policy"))
                .andExpect(model().attribute("totalPremium", 1500.00));
    }

    @Test
    public void testClaimReportPage() throws Exception {
        Map<String, Object> mockData = new HashMap<String, Object>();
        mockData.put("totalClaims", 2);

        when(reportService.customerClaimReport()).thenReturn(mockData);

        mockMvc.perform(get("/customer/reports/claims"))
                .andExpect(status().isOk())
                .andExpect(view().name("customer/report-claim"))
                .andExpect(model().attribute("totalClaims", 2));
    }

    @Test
    public void testPolicyPdfDownload() throws Exception {
        byte[] mockPdf = "Fake PDF".getBytes();
        when(reportService.customerPolicyPdf()).thenReturn(mockPdf);

        mockMvc.perform(get("/customer/reports/policies/pdf"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=POLICY_REPORT.pdf"))
                .andExpect(content().bytes(mockPdf));
    }

    @Test
    public void testClaimExcelDownload() throws Exception {
        byte[] mockExcel = "Fake Excel".getBytes();
        String excelMime = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

        when(reportService.customerClaimExcel()).thenReturn(mockExcel);

        mockMvc.perform(get("/customer/reports/claims/excel"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.parseMediaType(excelMime)))
                .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=CLAIM_REPORT.xlsx"))
                .andExpect(content().bytes(mockExcel));
    }
}