package com.cts.insurance.vehicle_insurance.repository;
import com.cts.insurance.vehicle_insurance.model.Vehicle;
import org.springframework.data.jpa.repository.JpaRepository;
public interface vehicleRepository extends JpaRepository<Vehicle,Long>{

}
