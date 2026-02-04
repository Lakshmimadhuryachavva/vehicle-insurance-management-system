//package com.cts.vis.service;//package com.cts.vis.service;
//
//import com.cts.vis.model.Claim;
//
//import java.math.BigDecimal;
//import java.util.List;
//
//public interface ClaimService {
//    Claim fileClaim(Long policyId, BigDecimal claimAmount, String reason);
//    List<Claim> myClaims();
//
//    // Admin
//    List<Claim> submittedClaims();
//
//    boolean hasApprovedClaimForVehicle(Long vehicleId);
//
//    Claim approve(Long claimId);
//    Claim reject(Long claimId);
//}

package com.cts.vis.service;
import com.cts.vis.model.Claim;

import java.math.BigDecimal;
import java.util.List;

public interface ClaimService {
    Claim fileClaim(Long policyId, BigDecimal claimAmount, String reason);
    List<Claim> myClaims();

    // Admin
    List<Claim> submittedClaims();

    boolean hasApprovedClaimForVehicle(Long vehicleId);

    // ✅ NEW
    boolean hasApprovedClaimForPolicy(Long policyId);

    Claim approve(Long claimId);
    Claim reject(Long claimId);
}