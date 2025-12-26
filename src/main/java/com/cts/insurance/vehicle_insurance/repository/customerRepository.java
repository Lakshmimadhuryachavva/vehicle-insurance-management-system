package com.cts.insurance.vehicle_insurance.repository;
import com.cts.insurance.vehicle_insurance.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface customerRepository extends JpaRepository<Customer,Long> {
}
