package com.cts.vis.service;

import com.cts.vis.model.Customer;
import com.cts.vis.model.Policy;
import com.cts.vis.model.PolicyStatus;
import com.cts.vis.model.Vehicle;
import com.cts.vis.repository.PolicyRepository;
import com.cts.vis.repository.VehicleRepository;
import com.cts.vis.util.PolicyNumberGenerator;
import com.cts.vis.util.PremiumCalculator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PolicyServiceImpl implements PolicyService {

    private final PolicyRepository policyRepository;
    private final VehicleRepository vehicleRepository;
    private final CustomerService customerService;
    private final PolicyNumberGenerator policyNumberGenerator;
    private final PremiumCalculator premiumCalculator;

    @Override
    @Transactional
    public Policy createPolicy(Long vehicleId, BigDecimal coverageAmount, LocalDate startDate) {
        Customer customer = customerService.getCurrentCustomer();
        Vehicle vehicle = vehicleRepository.findByVehicleIdAndCustomer(vehicleId, customer)
                .orElseThrow(() -> new IllegalArgumentException("Vehicle not found."));

        String number;
        do {
            number = policyNumberGenerator.generate();
        } while (policyRepository.existsByPolicyNumber(number));

        BigDecimal premium = premiumCalculator.calculate(vehicle.getVehicleType(), vehicle.getYearOfManufacture(), coverageAmount);

        Policy p = Policy.builder()
                .vehicle(vehicle)
                .policyNumber(number)
                .coverageAmount(coverageAmount)
                .premiumAmount(premium)
                .startDate(startDate)
                .endDate(startDate.plusYears(1))
                .policyStatus(PolicyStatus.ACTIVE)
                .build();

        return policyRepository.save(p);
    }

    @Override
    @Transactional
    public Policy renewPolicy(Long policyId) {
        Policy p = getMyPolicy(policyId);
        p.setStartDate(LocalDate.now());
        p.setEndDate(LocalDate.now().plusYears(1));
        p.setPolicyStatus(PolicyStatus.ACTIVE);
        return policyRepository.save(p);
    }

    @Override
    @Transactional
    public Policy updatePolicy(Long policyId, BigDecimal coverageAmount) {
        Policy p = getMyPolicy(policyId);
        Vehicle v = p.getVehicle();
        BigDecimal premium = premiumCalculator.calculate(v.getVehicleType(), v.getYearOfManufacture(), coverageAmount);
        p.setCoverageAmount(coverageAmount);
        p.setPremiumAmount(premium);
        return policyRepository.save(p);
    }

    @Override
    public List<Policy> myPolicies() {
        Customer customer = customerService.getCurrentCustomer();
        List<Vehicle> vehicles = vehicleRepository.findByCustomer(customer);
        return policyRepository.findByVehicleIn(vehicles);
    }

    @Override
    public Policy getMyPolicy(Long policyId) {
        Customer customer = customerService.getCurrentCustomer();
        List<Vehicle> vehicles = vehicleRepository.findByCustomer(customer);
        return policyRepository.findByPolicyIdAndVehicleIn(policyId, vehicles)
                .orElseThrow(() -> new IllegalArgumentException("Policy not found."));
    }
}