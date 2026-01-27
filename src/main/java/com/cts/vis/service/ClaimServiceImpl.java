package com.cts.vis.service;

import com.cts.vis.model.*;
import com.cts.vis.repository.ClaimRepository;
import com.cts.vis.repository.PolicyRepository;
import com.cts.vis.repository.VehicleRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ClaimServiceImpl implements ClaimService {

    private final ClaimRepository claimRepository;
    private final PolicyRepository policyRepository;
    private final VehicleRepository vehicleRepository;
    private final CustomerService customerService;

    @Override
    @Transactional
    public Claim fileClaim(Long policyId, BigDecimal claimAmount, String reason) {
        Customer customer = customerService.getCurrentCustomer();
        List<Vehicle> vehicles = vehicleRepository.findByCustomer(customer);
        Policy policy = policyRepository.findByPolicyIdAndVehicleIn(policyId, vehicles)
                .orElseThrow(() -> new IllegalArgumentException("Policy not found."));

        if (policy.getPolicyStatus() != PolicyStatus.ACTIVE) {
            throw new IllegalArgumentException("Cannot file claim on inactive/expired policy.");
        }

        // ✅ IMPORTANT BUSINESS VALIDATION
        if (claimAmount.compareTo(policy.getCoverageAmount()) > 0) {
            throw new IllegalArgumentException(
                    "Claim amount cannot exceed policy coverage amount"
            );
        }

        return claimRepository.save(Claim.builder()
                .policy(policy)
                .claimAmount(claimAmount)
                .claimReason(reason)
                .claimDate(LocalDate.now())
                .claimStatus(ClaimStatus.SUBMITTED)
                .build());
    }

    @Override
    public List<Claim> myClaims() {
        Customer customer = customerService.getCurrentCustomer();
        List<Vehicle> vehicles = vehicleRepository.findByCustomer(customer);
        List<Policy> policies = policyRepository.findByVehicleIn(vehicles);
        return claimRepository.findByPolicyIn(policies);
    }

    @Override
    public List<Claim> submittedClaims() {
        return claimRepository.findByClaimStatus(ClaimStatus.SUBMITTED);
    }

    @Override
    @Transactional
    public Claim approve(Long claimId) {
        Claim c = claimRepository.findById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("Claim not found"));
        c.setClaimStatus(ClaimStatus.APPROVED);
        return claimRepository.save(c);
    }

    @Override
    @Transactional
    public Claim reject(Long claimId) {
        Claim c = claimRepository.findById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("Claim not found"));
        c.setClaimStatus(ClaimStatus.REJECTED);
        return claimRepository.save(c);
    }
}