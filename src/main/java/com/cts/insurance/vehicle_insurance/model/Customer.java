package com.cts.insurance.vehicle_insurance.model;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.util.List;
import com.cts.insurance.vehicle_insurance.model.Vehicle;
@Entity
@Table(name = "customer")
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long customerId;

    @Column(length = 100, nullable = false)
    public String name;

    @Column(length = 100, nullable = false, unique = true)
    public String email;

    @Column(length = 15, nullable = false)
    public String phone;

    @Column(columnDefinition = "TEXT")
    public String address;

    @OneToMany(mappedBy = "customer", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonManagedReference
    public List<Vehicle> vehicles;
}
