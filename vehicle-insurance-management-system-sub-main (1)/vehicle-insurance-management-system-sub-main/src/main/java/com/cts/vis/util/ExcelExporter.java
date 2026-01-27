package com.cts.vis.util;

import com.cts.vis.model.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class ExcelExporter {

    /* ================= ADMIN REPORT ================= */

    public static byte[] exportAdminReport(ReportType type, Map<String, Object> data) {
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet(type + " Report");

            int rowIdx = 0;
            Row title = sheet.createRow(rowIdx++);
            title.createCell(0).setCellValue("Vehicle Insurance - " + type + " Report");

            rowIdx++;

            @SuppressWarnings("unchecked")
            List<?> rows = (List<?>) data.get("rows");

            rowIdx = switch (type) {
                case CUSTOMER -> customerSheet(sheet, rowIdx, (List<Customer>) rows);
                case VEHICLE -> vehicleSheet(sheet, rowIdx, (List<Vehicle>) rows);
                case POLICY -> policySheet(sheet, rowIdx, (List<Policy>) rows);
                case CLAIM -> claimSheet(sheet, rowIdx, (List<Claim>) rows);
            };

            rowIdx++;
            summary(sheet, rowIdx, type, data);

            autosize(sheet);

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            wb.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Excel generation failed", e);
        }
    }

    /* ================= CUSTOMER REPORT ================= */

    public static byte[] exportCustomerPolicyReport(List<Policy> policies, Map<String, Object> summary) {
        return exportAdminReport(ReportType.POLICY, Map.of(
                "start", "",
                "end", "",
                "rows", policies,
                "count", summary.get("totalPolicies"),
                "activeCount", summary.get("activePolicies"),
                "totalPremium", summary.get("totalPremium")
        ));
    }

    public static byte[] exportCustomerClaimReport(List<Claim> claims, Map<String, Object> summary) {
        return exportAdminReport(ReportType.CLAIM, Map.of(
                "start", "",
                "end", "",
                "rows", claims,
                "count", summary.get("totalClaims"),
                "approvedCount", summary.get("approvedClaims"),
                "totalClaimed", summary.get("totalClaimed")
        ));
    }

    /* ================= SHEETS ================= */

    private static int customerSheet(Sheet s, int r, List<Customer> list) {
        Row h = s.createRow(r++);
        header(h, "ID", "Name", "Email", "Phone", "Created");

        for (Customer c : list) {
            Row row = s.createRow(r++);
            row.createCell(0).setCellValue(c.getCustomerId());
            row.createCell(1).setCellValue(c.getName());
            row.createCell(2).setCellValue(c.getEmail());
            row.createCell(3).setCellValue(c.getPhone());
            row.createCell(4).setCellValue(String.valueOf(c.getCreatedDate()));
        }
        return r;
    }

    private static int vehicleSheet(Sheet s, int r, List<Vehicle> list) {
        Row h = s.createRow(r++);
        header(h, "ID", "Reg No", "Owner", "Make", "Model", "Type");

        for (Vehicle v : list) {
            Row row = s.createRow(r++);
            row.createCell(0).setCellValue(v.getVehicleId());
            row.createCell(1).setCellValue(v.getRegistrationNumber());
            row.createCell(2).setCellValue(v.getCustomer().getName());
            row.createCell(3).setCellValue(v.getMake());
            row.createCell(4).setCellValue(v.getModel());
            row.createCell(5).setCellValue(v.getVehicleType().name());
        }
        return r;
    }

    private static int policySheet(Sheet s, int r, List<Policy> list) {
        Row h = s.createRow(r++);
        header(h, "Policy No", "Customer", "Vehicle", "Coverage", "Premium", "Status");

        for (Policy p : list) {
            Row row = s.createRow(r++);
            row.createCell(0).setCellValue(p.getPolicyNumber());
            row.createCell(1).setCellValue(p.getVehicle().getCustomer().getName());
            row.createCell(2).setCellValue(p.getVehicle().getMake());
            row.createCell(3).setCellValue(val(p.getCoverageAmount()));
            row.createCell(4).setCellValue(val(p.getPremiumAmount()));
            row.createCell(5).setCellValue(p.getPolicyStatus().name());
        }
        return r;
    }

    private static int claimSheet(Sheet s, int r, List<Claim> list) {
        Row h = s.createRow(r++);
        header(h, "Claim ID", "Policy", "Customer", "Amount", "Status");

        for (Claim c : list) {
            Row row = s.createRow(r++);
            row.createCell(0).setCellValue(c.getClaimId());
            row.createCell(1).setCellValue(c.getPolicy().getPolicyNumber());
            row.createCell(2).setCellValue(c.getPolicy().getVehicle().getCustomer().getName());
            row.createCell(3).setCellValue(val(c.getClaimAmount()));
            row.createCell(4).setCellValue(c.getClaimStatus().name());
        }
        return r;
    }

    /* ================= COMMON ================= */

    private static void header(Row r, String... cols) {
        int i = 0;
        for (String c : cols) r.createCell(i++).setCellValue(c);
    }

    private static void summary(Sheet s, int r, ReportType type, Map<String, Object> d) {
        Row row = s.createRow(r);
        row.createCell(0).setCellValue("Total Rows");
        row.createCell(1).setCellValue(String.valueOf(d.get("count")));

        if (type == ReportType.POLICY) {
            Row a = s.createRow(r + 1);
            a.createCell(0).setCellValue("Active Policies");
            a.createCell(1).setCellValue(String.valueOf(d.get("activeCount")));
        }
        if (type == ReportType.CLAIM) {
            Row a = s.createRow(r + 1);
            a.createCell(0).setCellValue("Approved Claims");
            a.createCell(1).setCellValue(String.valueOf(d.get("approvedCount")));
        }
    }

    private static void autosize(Sheet s) {
        for (int i = 0; i < 10; i++) s.autoSizeColumn(i);
    }

    private static double val(BigDecimal b) {
        return b == null ? 0 : b.doubleValue();
    }
}