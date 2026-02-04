package com.cts.vis.service;

import com.cts.vis.model.*;
import com.cts.vis.repository.ClaimRepository;
import com.cts.vis.repository.CustomerRepository;
import com.cts.vis.repository.PolicyRepository;
import com.cts.vis.repository.VehicleRepository;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminReportServiceImpl implements AdminReportService {

    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final PolicyRepository policyRepository;
    private final ClaimRepository claimRepository;

    @Override
    @Transactional
    public Map<String, Object> generate(ReportType type, LocalDate start, LocalDate end) {
        Map<String, Object> model = new HashMap<String, Object>();
        model.put("type", type);
        model.put("start", start);
        model.put("end", end);

        // Classic Switch-Case (No Switch Expressions)
        switch (type) {
            case CUSTOMER:
                List<Customer> customers = customerRepository.findByCreatedDateBetween(start, end);
                model.put("rows", customers);
                model.put("count", (long) customers.size());
                break;
            case VEHICLE:
                List<Vehicle> vehicles = vehicleRepository.findByCreatedDateBetween(start, end);
                model.put("rows", vehicles);
                model.put("count", (long) vehicles.size());
                break;
            case POLICY:
                List<Policy> policies = policyRepository.findByStartDateBetween(start, end);
                long active = 0;
                BigDecimal totalPremium = BigDecimal.ZERO;
                // Traditional Loop instead of Streams
                for (Policy p : policies) {
                    if (p.getPolicyStatus() == PolicyStatus.ACTIVE) {
                        active++;
                    }
                    if (p.getPremiumAmount() != null) {
                        totalPremium = totalPremium.add(p.getPremiumAmount());
                    }
                }
                model.put("rows", policies);
                model.put("count", (long) policies.size());
                model.put("activeCount", active);
                model.put("totalPremium", totalPremium);
                break;
            case CLAIM:
                List<Claim> claims = claimRepository.findByClaimDateBetween(start, end);
                long approved = 0;
                BigDecimal totalClaimed = BigDecimal.ZERO;
                // Traditional Loop instead of Streams
                for (Claim c : claims) {
                    if (c.getClaimStatus() == ClaimStatus.APPROVED) {
                        approved++;
                    }
                    if (c.getClaimAmount() != null) {
                        totalClaimed = totalClaimed.add(c.getClaimAmount());
                    }
                }
                model.put("rows", claims);
                model.put("count", (long) claims.size());
                model.put("approvedCount", approved);
                model.put("totalClaimed", totalClaimed);
                break;
        }
        return model;
    }

    @Override
    public byte[] exportPdf(ReportType type, LocalDate start, LocalDate end) {
        Map<String, Object> model = generate(type, start, end);
        String title = "Vehicle Insurance - " + type + " Report (" + start + " to " + end + ")";
        return buildPdf(title, headersFor(type), rowsFor(type, model.get( "rows" )), summaryFor(type, model));
    }

    @Override
    public byte[] exportExcel(ReportType type, LocalDate start, LocalDate end) {
        Map<String, Object> model = generate(type, start, end);
        return buildExcel(type + " Report", headersFor(type), rowsFor(type, model.get( "rows" )), summaryFor(type, model));
    }

    private List<String> headersFor(ReportType type) {
        // Arrays.asList instead of List.of
        switch (type) {
            case CUSTOMER: return Arrays.asList("ID", "Name", "Email", "Phone", "Created");
            case VEHICLE:  return Arrays.asList("ID", "Reg No", "Owner", "Make", "Model", "Type");
            case POLICY:   return Arrays.asList("Policy No", "Customer", "Premium", "Start", "End", "Status");
            case CLAIM:    return Arrays.asList("ID", "Policy", "Customer", "Amount", "Reason", "Status");
            default:       return new ArrayList<String>();
        }
    }

    @SuppressWarnings("unchecked")
    private List<List<String>> rowsFor(ReportType type, Object rowsObj) {
        List<List<String>> out = new ArrayList<List<String>>();
        if (rowsObj == null) return out;

        if (type == ReportType.CUSTOMER) {
            List<Customer> customers = (List<Customer>) rowsObj;
            for (Customer c : customers) {
                out.add(Arrays.asList(s(c.getCustomerId()), safe(c.getName()), safe(c.getEmail()), safe(c.getPhone()), s(c.getCreatedDate())));
            }
        } else if (type == ReportType.VEHICLE) {
            List<Vehicle> vehicles = (List<Vehicle>) rowsObj;
            for (Vehicle v : vehicles) {
                String owner = v.getCustomer() != null ? safe(v.getCustomer().getName()) : "N/A";
                out.add(Arrays.asList(s(v.getVehicleId()), safe(v.getRegistrationNumber()), owner, safe(v.getMake()), safe(v.getModel()), s(v.getVehicleType())));
            }
        } else if (type == ReportType.POLICY) {
            List<Policy> policies = (List<Policy>) rowsObj;
            for (Policy p : policies) {
                String cust = (p.getVehicle() != null && p.getVehicle().getCustomer() != null) ? p.getVehicle().getCustomer().getName() : "N/A";
                out.add(Arrays.asList(safe(p.getPolicyNumber()), cust, s(p.getPremiumAmount()), s(p.getStartDate()), s(p.getEndDate()), s(p.getPolicyStatus())));
            }
        } else if (type == ReportType.CLAIM) {
            List<Claim> claims = (List<Claim>) rowsObj;
            for (Claim c : claims) {
                String pol = c.getPolicy() != null ? c.getPolicy().getPolicyNumber() : "N/A";
                out.add(Arrays.asList(s(c.getClaimId()), pol, s(c.getClaimAmount()), safe(c.getClaimReason()), s(c.getClaimStatus())));
            }
        }
        return out;
    }

    private Map<String, String> summaryFor(ReportType type, Map<String, Object> model) {
        Map<String, String> summary = new LinkedHashMap<String, String>();
        summary.put("Total Records", s(model.get("count")));
        if (type == ReportType.POLICY) {
            summary.put("Active Policies", s(model.get("activeCount")));
            summary.put("Total Premium", "$" + s(model.get("totalPremium")));
        } else if (type == ReportType.CLAIM) {
            summary.put("Approved Claims", s(model.get("approvedCount")));
            summary.put("Total Payout", "$" + s(model.get("totalClaimed")));
        }
        return summary;
    }

    private byte[] buildPdf(String title, List<String> headers, List<List<String>> rows, Map<String, String> summary) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document doc = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(doc, out);
            doc.open();

            doc.add(new Paragraph(title, FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16)));
            doc.add(Chunk.NEWLINE);

            PdfPTable table = new PdfPTable(headers.size());
            table.setWidthPercentage(100);

            com.lowagie.text.Font headFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.WHITE);
            for (int i = 0; i < headers.size(); i++) {
                PdfPCell cell = new PdfPCell(new Phrase(headers.get(i), headFont));
                cell.setBackgroundColor(new Color(30, 41, 59));
                cell.setPadding(5);
                table.addCell(cell);
            }

            for (int i = 0; i < rows.size(); i++) {
                List<String> rowData = rows.get(i);
                for (int j = 0; j < rowData.size(); j++) {
                    table.addCell(new Phrase(rowData.get(j), FontFactory.getFont(FontFactory.HELVETICA, 10)));
                }
            }

            doc.add(table);
            doc.add(Chunk.NEWLINE);

            doc.add(new Paragraph("Summary Report", FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
            // Manual iteration for map entries (Classic Java)
            Iterator<Map.Entry<String, String>> it = summary.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<String, String> entry = it.next();
                doc.add(new Paragraph(entry.getKey() + ": " + entry.getValue()));
            }

            doc.close();
            return out.toByteArray();
        } catch (Exception e) { throw new RuntimeException("PDF Error", e); }
    }

    private byte[] buildExcel(String sheetName, List<String> headers, List<List<String>> rows, Map<String, String> summary) {
        try {
            Workbook wb = new XSSFWorkbook();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Sheet sheet = wb.createSheet(sheetName);

            Row headerRow = sheet.createRow(0);
            CellStyle headerStyle = wb.createCellStyle();
            org.apache.poi.ss.usermodel.Font font = wb.createFont();
            font.setBold(true);
            headerStyle.setFont(font);

            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
                cell.setCellStyle(headerStyle);
            }

            for (int i = 0; i < rows.size(); i++) {
                List<String> rowData = rows.get(i);
                Row row = sheet.createRow(i + 1);
                for (int j = 0; j < rowData.size(); j++) {
                    row.createCell(j).setCellValue(rowData.get(j));
                }
            }

            for (int i = 0; i < headers.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            wb.write(out);
            wb.close();
            return out.toByteArray();
        } catch (Exception e) { throw new RuntimeException("Excel Error", e); }
    }

    private String safe(String s) { return s == null ? "" : s; }
    private String s(Object o) { return o == null ? "" : String.valueOf(o); }
}