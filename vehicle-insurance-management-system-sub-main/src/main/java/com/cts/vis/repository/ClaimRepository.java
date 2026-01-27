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
}