package com.cts.vis.service;

import com.cts.vis.model.Policy;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public interface PolicyService {
    Policy createPolicy(Long vehicleId, BigDecimal coverageAmount, LocalDate startDate);
    Policy renewPolicy(Long policyId);
    Policy updatePolicy(Long policyId, BigDecimal coverageAmount);

    List<Policy> myPolicies();
    Policy getMyPolicy(Long policyId);
}