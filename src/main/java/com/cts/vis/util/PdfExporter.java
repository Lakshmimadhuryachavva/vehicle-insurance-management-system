package com.cts.vis.util;

import com.cts.vis.model.*;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class PdfExporter {

    public static byte[] exportAdminReport(ReportType type, Map<String, Object> data) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Document doc = new Document(PageSize.A4.rotate(), 24, 24, 24, 24);

        try {
            PdfWriter.getInstance(doc, out);
            doc.open();

            Font title = new Font(Font.HELVETICA, 18, Font.BOLD);
            doc.add(new Paragraph("Vehicle Insurance - " + type + " Report", title));
            doc.add(new Paragraph("Date Range: " + data.get("start") + " to " + data.get("end")));
            doc.add(Chunk.NEWLINE);

            @SuppressWarnings("unchecked")
            List<?> rows = (List<?>) data.get("rows");

            PdfPTable table = switch (type) {
                case CUSTOMER -> customerTable((List<Customer>) rows);
                case VEHICLE -> vehicleTable((List<Vehicle>) rows);
                case POLICY -> policyTable((List<Policy>) rows);
                case CLAIM -> claimTable((List<Claim>) rows);
            };

            doc.add(table);
            doc.add(Chunk.NEWLINE);
            doc.add(summaryBlock(type, data));

            doc.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("PDF generation failed: " + e.getMessage(), e);
        }
    }

    public static byte[] exportCustomerPolicyReport(List<Policy> policies, Map<String, Object> summary) {
        // reuse admin table style
        return exportAdminReport(ReportType.POLICY, Map.of(
                "start", "",
                "end", "",
                "rows", policies,
                "count", summary.getOrDefault("totalPolicies", policies.size()),
                "activeCount", summary.getOrDefault("activePolicies", 0),
                "totalPremium", summary.getOrDefault("totalPremium", BigDecimal.ZERO)
        ));
    }

    public static byte[] exportCustomerClaimReport(List<Claim> claims, Map<String, Object> summary) {
        return exportAdminReport(ReportType.CLAIM, Map.of(
                "start", "",
                "end", "",
                "rows", claims,
                "count", summary.getOrDefault("totalClaims", claims.size()),
                "approvedCount", summary.getOrDefault("approvedClaims", 0),
                "totalClaimed", summary.getOrDefault("totalClaimed", BigDecimal.ZERO)
        ));
    }

    private static PdfPTable customerTable(List<Customer> rows) {
        PdfPTable t = baseTable(5);
        header(t, "Customer ID", "Name", "Email", "Phone", "Created Date");
        for (Customer c : rows) {
            row(t, String.valueOf(c.getCustomerId()), c.getName(), c.getEmail(),
                    safe(c.getPhone()), String.valueOf(c.getCreatedDate()));
        }
        return t;
    }

    private static PdfPTable vehicleTable(List<Vehicle> rows) {
        PdfPTable t = baseTable(7);
        header(t, "Vehicle ID", "Reg No", "Owner", "Make", "Model", "Type", "Created Date");
        for (Vehicle v : rows) {
            row(t, String.valueOf(v.getVehicleId()), v.getRegistrationNumber(),
                    v.getCustomer().getName(), v.getMake(), v.getModel(),
                    v.getVehicleType().name(), String.valueOf(v.getCreatedDate()));
        }
        return t;
    }

    private static PdfPTable policyTable(List<Policy> rows) {
        PdfPTable t = baseTable(8);
        header(t, "Policy No", "Customer", "Vehicle", "Coverage", "Premium", "Start", "End", "Status");
        for (Policy p : rows) {
            row(t, p.getPolicyNumber(),
                    p.getVehicle().getCustomer().getName(),
                    p.getVehicle().getMake() + " " + p.getVehicle().getModel(),
                    String.valueOf(p.getCoverageAmount()),
                    String.valueOf(p.getPremiumAmount()),
                    String.valueOf(p.getStartDate()),
                    String.valueOf(p.getEndDate()),
                    p.getPolicyStatus().name());
        }
        return t;
    }

    private static PdfPTable claimTable(List<Claim> rows) {
        PdfPTable t = baseTable(7);
        header(t, "Claim ID", "Policy No", "Customer", "Amount", "Reason", "Date", "Status");
        for (Claim c : rows) {
            row(t,
                    String.valueOf(c.getClaimId()),
                    c.getPolicy().getPolicyNumber(),
                    c.getPolicy().getVehicle().getCustomer().getName(),
                    String.valueOf(c.getClaimAmount()),
                    c.getClaimReason(),
                    String.valueOf(c.getClaimDate()),
                    c.getClaimStatus().name());
        }
        return t;
    }

    private static Paragraph summaryBlock(ReportType type, Map<String, Object> data) {
        Paragraph p = new Paragraph();
        p.add(new Chunk("Summary\n", new Font(Font.HELVETICA, 14, Font.BOLD)));
        p.add("Total Rows: " + data.getOrDefault("count", 0) + "\n");

        if (type == ReportType.POLICY) {
            p.add("Active Policies: " + data.getOrDefault("activeCount", 0) + "\n");
            p.add("Total Premium: " + data.getOrDefault("totalPremium", BigDecimal.ZERO) + "\n");
        }
        if (type == ReportType.CLAIM) {
            p.add("Approved Claims: " + data.getOrDefault("approvedCount", 0) + "\n");
            p.add("Total Claimed: " + data.getOrDefault("totalClaimed", BigDecimal.ZERO) + "\n");
        }
        return p;
    }

    private static PdfPTable baseTable(int cols) {
        PdfPTable t = new PdfPTable(cols);
        t.setWidthPercentage(100);
        t.setSpacingBefore(10);
        return t;
    }

    private static void header(PdfPTable t, String... cols) {
        Font f = new Font(Font.HELVETICA, 11, Font.BOLD, Color.WHITE);
        for (String c : cols) {
            PdfPCell cell = new PdfPCell(new Phrase(c, f));
            cell.setBackgroundColor(new Color(15, 23, 42));
            cell.setPadding(8);
            t.addCell(cell);
        }
    }

    private static void row(PdfPTable t, String... cols) {
        for (String c : cols) {
            PdfPCell cell = new PdfPCell(new Phrase(safe(c)));
            cell.setPadding(6);
            t.addCell(cell);
        }
    }

    private static String safe(String s) {
        return (s == null) ? "" : s;
    }
}