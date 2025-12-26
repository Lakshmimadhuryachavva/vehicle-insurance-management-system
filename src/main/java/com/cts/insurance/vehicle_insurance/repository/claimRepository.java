package com.cts.insurance.vehicle_insurance.repository;
import com.cts.insurance.vehicle_insurance.model.Claim;
import org.springframework.data.jpa.repository.JpaRepository;
public interface claimRepository extends JpaRepository<Claim,Long>{
}
