//package com.cts.vis.repository;
//
//import com.cts.vis.model.Claim;
//import com.cts.vis.model.ClaimStatus;
//import com.cts.vis.model.Policy;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//import java.time.LocalDate;
//import java.util.List;
//
//public interface ClaimRepository extends JpaRepository<Claim, Long> {
//
//    List<Claim> findByPolicyIn(List<Policy> policies);
//
//    List<Claim> findByClaimStatus(ClaimStatus status);
//
//    // ✅ This fixes your AdminReportServiceImpl compile error
//    List<Claim> findByClaimDateBetween(LocalDate start, LocalDate end);
//
//    // ✅ Needed for "disable Edit if approved claim exists"
//    boolean existsByPolicy_Vehicle_VehicleIdAndClaimStatus(Long vehicleId, ClaimStatus status);
//
//    boolean existsByPolicy_PolicyIdAndClaimStatus(Long policyId, ClaimStatus status);
//}

package com.cts.vis.repository;

import com.cts.vis.model.Claim;
import com.cts.vis.model.ClaimStatus;
import com.cts.vis.model.Policy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ClaimRepository extends JpaRepository<Claim, Long> {

    List<Claim> findByPolicyIn(List<Policy> policies);

    List<Claim> findByClaimStatus(ClaimStatus status);

    List<Claim> findByClaimDateBetween(LocalDate start, LocalDate end);

    // keep if used elsewhere
    boolean existsByPolicy_Vehicle_VehicleIdAndClaimStatus(Long vehicleId, ClaimStatus status);

    // ✅ policy-specific lock (ONLY this policy)
    boolean existsByPolicy_PolicyIdAndClaimStatus(Long policyId, ClaimStatus status);
}
