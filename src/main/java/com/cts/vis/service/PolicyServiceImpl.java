package com.cts.vis.service;

import com.cts.vis.model.Customer;
import com.cts.vis.model.Policy;
import com.cts.vis.model.PolicyStatus;
import com.cts.vis.model.Vehicle;
import com.cts.vis.model.VehicleType;
import com.cts.vis.repository.PolicyRepository;
import com.cts.vis.repository.VehicleRepository;
import com.cts.vis.util.PolicyNumberGenerator;
import com.cts.vis.exception.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Year;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PolicyServiceImpl implements PolicyService {

    private final PolicyRepository policyRepository;
    private final VehicleRepository vehicleRepository;
    private final CustomerService customerService;
    private final PolicyNumberGenerator policyNumberGenerator;

    @Override
    @Transactional
    public Policy createPolicy(Long vehicleId, BigDecimal coverageAmount, LocalDate startDate, LocalDate endDate) {
        Customer customer = customerService.getCurrentCustomer();

        // Manual Optional handling instead of .orElseThrow()
        Optional<Vehicle> vOpt = vehicleRepository.findByVehicleIdAndCustomer(vehicleId, customer);
        if (!vOpt.isPresent()) {
            throw new IllegalArgumentException("Vehicle not found.");
        }
        Vehicle vehicle = vOpt.get();

        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("Start date and End date are required.");
        }
        if (!endDate.isAfter(startDate)) {
            throw new IllegalArgumentException("End date must be after start date.");
        }

        String number;
        do {
            number = policyNumberGenerator.generate();
        } while (policyRepository.existsByPolicyNumber(number));

        BigDecimal premium = calculatePremium(vehicle.getVehicleType(), vehicle.getYearOfManufacture(), coverageAmount);

        // Using standard setters instead of Policy.builder()
        Policy p = new Policy();
        p.setVehicle(vehicle);
        p.setPolicyNumber(number);
        p.setCoverageAmount(coverageAmount);
        p.setPremiumAmount(premium);
        p.setStartDate(startDate);
        p.setEndDate(endDate);
        p.setPolicyStatus(getStatusByEndDate(endDate));

        return policyRepository.save(p);
    }

    @Override
    @Transactional
    public Policy renewPolicy(Long policyId) {
        Policy p = getMyPolicy(policyId);

        refreshStatus(p);

        if (p.getPolicyStatus() != PolicyStatus.EXPIRED) {
            throw new IllegalStateException("Policy is still ACTIVE. Renewal is allowed only after expiry.");
        }

        LocalDate today = LocalDate.now();
        p.setStartDate(today);
        p.setEndDate(today.plusYears(1));
        p.setPolicyStatus(PolicyStatus.ACTIVE);

        return policyRepository.save(p);
    }

    @Override
    @Transactional
    public Policy updatePolicy(Long policyId, BigDecimal coverageAmount) {
        Policy p = getMyPolicy(policyId);
        Vehicle v = p.getVehicle();

        BigDecimal premium = calculatePremium(v.getVehicleType(), v.getYearOfManufacture(), coverageAmount);

        p.setCoverageAmount(coverageAmount);
        p.setPremiumAmount(premium);

        return policyRepository.save(p);
    }

    @Override
    @Transactional
    public List<Policy> myPolicies() {
        Customer customer = customerService.getCurrentCustomer();
        List<Vehicle> vehicles = vehicleRepository.findByCustomer(customer);
        List<Policy> policies = policyRepository.findByVehicleIn(vehicles);

        boolean changed = false;
        // Simple for-each loop to refresh status
        for (int i = 0; i < policies.size(); i++) {
            Policy p = policies.get(i);
            if (refreshStatus(p)) {
                changed = true;
            }
        }

        if (changed) {
            policyRepository.saveAll(policies);
        }

        return policies;
    }

    @Override
    public Policy getMyPolicy(Long policyId) {
        Customer customer = customerService.getCurrentCustomer();
        List<Vehicle> vehicles = vehicleRepository.findByCustomer(customer);

        // Manual Optional handling
        Optional<Policy> pOpt = policyRepository.findByPolicyIdAndVehicleIn(policyId, vehicles);
        if (!pOpt.isPresent()) {
            throw new NotFoundException("Policy not found.");
        }
        return pOpt.get();
    }

    private PolicyStatus getStatusByEndDate(LocalDate endDate) {
        if (LocalDate.now().isAfter(endDate)) {
            return PolicyStatus.EXPIRED;
        } else {
            return PolicyStatus.ACTIVE;
        }
    }

    private boolean refreshStatus(Policy p) {
        if (p.getEndDate() == null) {
            return false;
        }

        PolicyStatus newStatus = getStatusByEndDate(p.getEndDate());
        if (p.getPolicyStatus() != newStatus) {
            p.setPolicyStatus(newStatus);
            return true;
        }
        return false;
    }

    private BigDecimal calculatePremium(VehicleType type, int yearOfManufacture, BigDecimal coverageAmount) {
        // Replaced switch expression with classic switch-case
        BigDecimal base;
        switch (type) {
            case CAR:
                base = BigDecimal.valueOf(1000);
                break;
            case BIKE:
                base = BigDecimal.valueOf(500);
                break;
            case TRUCK:
                base = BigDecimal.valueOf(1500);
                break;
            default:
                base = BigDecimal.valueOf(1000);
        }

        int currentYear = Year.now().getValue();
        int age = currentYear - yearOfManufacture;
        if (age < 0) {
            age = 0;
        }

        BigDecimal ageFactor;
        if (age <= 3) {
            ageFactor = BigDecimal.valueOf(1.00);
        } else if (age <= 7) {
            ageFactor = BigDecimal.valueOf(1.10);
        } else if (age <= 12) {
            ageFactor = BigDecimal.valueOf(1.25);
        } else {
            ageFactor = BigDecimal.valueOf(1.40);
        }

        BigDecimal coverageFactor = coverageAmount.multiply(BigDecimal.valueOf(0.008));

        BigDecimal result = base.multiply(ageFactor).add(coverageFactor);
        return result.setScale(2, RoundingMode.HALF_UP);
    }
}