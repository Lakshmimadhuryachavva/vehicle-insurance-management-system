package com.cts.vis.service;

import com.cts.vis.model.*;
import com.cts.vis.repository.ClaimRepository;
import com.cts.vis.repository.CustomerRepository;
import com.cts.vis.repository.PolicyRepository;
import com.cts.vis.repository.VehicleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final CustomerService customerService;
    private final VehicleRepository vehicleRepository;
    private final PolicyRepository policyRepository;
    private final ClaimRepository claimRepository;
    private final CustomerRepository customerRepository;

    @Override
    @Transactional
    public Map<String, Object> customerDashboardStats() {
        Customer customer = customerService.getCurrentCustomer();
        List<Vehicle> vehicles = vehicleRepository.findByCustomer(customer);
        List<Policy> policies = policyRepository.findByVehicleIn(vehicles);
        List<Claim> claims = claimRepository.findByPolicyIn(policies);

        long activePolicies = policies.stream().filter(p -> p.getPolicyStatus() == PolicyStatus.ACTIVE).count();
        BigDecimal totalPremium = policies.stream().map(Policy::getPremiumAmount).filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> m = new HashMap<>();
        m.put("totalVehicles", vehicles.size());
        m.put("activePolicies", activePolicies);
        m.put("totalClaims", claims.size());
        m.put("totalPremium", totalPremium);
        return m;
    }

    @Override
    @Transactional
    public Map<String, Object> customerPolicyReport() {
        Customer customer = customerService.getCurrentCustomer();
        List<Vehicle> vehicles = vehicleRepository.findByCustomer(customer);
        List<Policy> policies = policyRepository.findByVehicleIn(vehicles);

        BigDecimal totalPremium = policies.stream().map(Policy::getPremiumAmount).filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> m = new HashMap<>();
        m.put("policies", policies);
        m.put("totalPolicies", policies.size());
        m.put("activePolicies", policies.stream().filter(p -> p.getPolicyStatus() == PolicyStatus.ACTIVE).count());
        m.put("totalPremium", totalPremium);
        return m;
    }

    @Override
    @Transactional
    public Map<String, Object> customerClaimReport() {
        Customer customer = customerService.getCurrentCustomer();
        List<Vehicle> vehicles = vehicleRepository.findByCustomer(customer);
        List<Policy> policies = policyRepository.findByVehicleIn(vehicles);
        List<Claim> claims = claimRepository.findByPolicyIn(policies);

        BigDecimal totalClaimed = claims.stream().map(Claim::getClaimAmount).filter(Objects::nonNull)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        Map<String, Object> m = new HashMap<>();
        m.put("claims", claims);
        m.put("totalClaims", claims.size());
        m.put("approvedClaims", claims.stream().filter(c -> c.getClaimStatus() == ClaimStatus.APPROVED).count());
        m.put("totalClaimed", totalClaimed);
        return m;
    }

    @Override
    public Map<String, Object> adminDashboardStats() {
        long totalCustomers = customerRepository.count();
        long totalVehicles = vehicleRepository.count();
        long totalPolicies = policyRepository.count();
        long pendingClaims = claimRepository.findByClaimStatus(ClaimStatus.SUBMITTED).size();
        long approvedClaims = claimRepository.findByClaimStatus(ClaimStatus.APPROVED).size();

        Map<String, Object> m = new HashMap<>();
        m.put("totalCustomers", totalCustomers);
        m.put("totalVehicles", totalVehicles);
        m.put("totalPolicies", totalPolicies);
        m.put("pendingClaims", pendingClaims);
        m.put("approvedClaims", approvedClaims);
        return m;
    }
}