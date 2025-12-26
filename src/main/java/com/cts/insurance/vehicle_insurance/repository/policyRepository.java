package com.cts.insurance.vehicle_insurance.repository;
import com.cts.insurance.vehicle_insurance.model.Policy;
import org.springframework.data.jpa.repository.JpaRepository;

public interface policyRepository extends JpaRepository<Policy,Long>{
}
