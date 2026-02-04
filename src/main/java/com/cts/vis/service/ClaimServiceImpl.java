package com.cts.vis.service;

import com.cts.vis.model.*;
import com.cts.vis.repository.ClaimRepository;
import com.cts.vis.repository.PolicyRepository;
import com.cts.vis.repository.VehicleRepository;
import com.cts.vis.exception.NotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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

        // Manual Optional check instead of .orElseThrow() lambda
        Optional<Policy> policyOpt = policyRepository.findByPolicyIdAndVehicleIn(policyId, vehicles);
        if (!policyOpt.isPresent()) {
            throw new IllegalArgumentException("Policy not found.");
        }
        Policy policy = policyOpt.get();

        if (policy.getPolicyStatus() != PolicyStatus.ACTIVE) {
            throw new IllegalArgumentException("Cannot file claim on inactive/expired policy.");
        }

        // Business Validation: Claim amount vs Coverage amount
        if (claimAmount.compareTo(policy.getCoverageAmount()) > 0) {
            throw new IllegalArgumentException("Claim amount cannot exceed policy coverage amount");
        }

        // Using standard setters instead of Claim.builder()
        Claim claim = new Claim();
        claim.setPolicy(policy);
        claim.setClaimAmount(claimAmount);
        claim.setClaimReason(reason);
        claim.setClaimDate(LocalDate.now());
        claim.setClaimStatus(ClaimStatus.SUBMITTED);

        return claimRepository.save(claim);
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
    public boolean hasApprovedClaimForVehicle(Long vehicleId) {
        return claimRepository.existsByPolicy_Vehicle_VehicleIdAndClaimStatus(
                vehicleId, ClaimStatus.APPROVED
        );
    }

    @Override
    @Transactional
    public Claim approve(Long claimId) {
        // Manual Optional check
        Optional<Claim> claimOpt = claimRepository.findById(claimId);
        if (!claimOpt.isPresent()) {
            throw new NotFoundException("Claim not found");
        }

        Claim c = claimOpt.get();
        c.setClaimStatus(ClaimStatus.APPROVED);
        return claimRepository.save(c);
    }

    @Override
    public boolean hasApprovedClaimForPolicy(Long policyId) {
        return claimRepository.existsByPolicy_PolicyIdAndClaimStatus(policyId, ClaimStatus.APPROVED);
    }

    @Override
    @Transactional
    public Claim reject(Long claimId) {
        // Manual Optional check
        Optional<Claim> claimOpt = claimRepository.findById(claimId);
        if (!claimOpt.isPresent()) {
            throw new IllegalArgumentException("Claim not found");
        }

        Claim c = claimOpt.get();
        c.setClaimStatus(ClaimStatus.REJECTED);
        return claimRepository.save(c);
    }
}