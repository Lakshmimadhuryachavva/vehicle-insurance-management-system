
package com.cts.insurance.vehicle_insurance.model;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;

@Entity
@Table(name = "vehicle")
public class Vehicle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long vehicleId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "customer_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_vehicle_customer"))
    @JsonBackReference
    public Customer customer;

    @Column(length = 20, nullable = false, unique = true)
    public String registrationNumber;

    @Column(length = 50, nullable = false)
    public String make;

    @Column(length = 50, nullable = false)
    public String model;

    @Column(nullable = false)
    public Integer yearOfManufacture;

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    public VehicleType vehicleType;
}

/* Inline enum (same package & file)
   Leave it non-public to respect Java’s one-public-type-per-file rule.
*/
enum VehicleType {
    CAR, BIKE, TRUCK
}
