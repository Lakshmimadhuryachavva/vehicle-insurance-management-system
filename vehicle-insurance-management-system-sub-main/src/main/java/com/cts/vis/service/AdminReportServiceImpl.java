package com.cts.vis.service;

import com.cts.vis.model.*;
import com.cts.vis.repository.*;
import com.cts.vis.service.AdminReportService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

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
        Map<String, Object> model = new HashMap<>();
        model.put("type", type);
        model.put("start", start);
        model.put("end", end);

        switch (type) {
            case CUSTOMER -> {
                List<Customer> rows = customerRepository.findByCreatedDateBetween(start, end);
                model.put("rows", rows);
                model.put("count", rows.size());
            }
            case VEHICLE -> {
                List<Vehicle> rows = vehicleRepository.findByCreatedDateBetween(start, end);
                model.put("rows", rows);
                model.put("count", rows.size());
            }
            case POLICY -> {
                List<Policy> rows = policyRepository.findByStartDateBetween(start, end);
                long active = rows.stream().filter(p -> p.getPolicyStatus() == PolicyStatus.ACTIVE).count();
                BigDecimal totalPremium = rows.stream().map(Policy::getPremiumAmount).filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                model.put("rows", rows);
                model.put("count", rows.size());
                model.put("activeCount", active);
                model.put("totalPremium", totalPremium);
            }
            case CLAIM -> {
                List<Claim> rows = claimRepository.findByClaimDateBetween(start, end);
                long approved = rows.stream().filter(c -> c.getClaimStatus() == ClaimStatus.APPROVED).count();
                BigDecimal totalClaimed = rows.stream().map(Claim::getClaimAmount).filter(Objects::nonNull)
                        .reduce(BigDecimal.ZERO, BigDecimal::add);

                model.put("rows", rows);
                model.put("count", rows.size());
                model.put("approvedCount", approved);
                model.put("totalClaimed", totalClaimed);
            }
        }
        return model;
    }
}