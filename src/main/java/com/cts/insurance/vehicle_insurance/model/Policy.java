
package com.cts.insurance.vehicle_insurance.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "policy")
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long policyId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "vehicle_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_policy_vehicle"))
    public Vehicle vehicle;

    @Column(length = 20, nullable = false, unique = true)
    public String policyNumber;

    @Column(precision = 10, scale = 2, nullable = false)
    public BigDecimal coverageAmount;

    @Column(precision = 10, scale = 2, nullable = false)
    public BigDecimal premiumAmount;

    @Column(nullable = false)
    public LocalDate startDate;

    @Column(nullable = false)
    public LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(length = 10, nullable = false)
    public PolicyStatus policyStatus;
}

/* Inline enum must be in the same package/file or same package in its own file.
   Keep it non-public (Java allows one public top-level type per file).
*/
enum PolicyStatus {
    ACTIVE, EXPIRED
}
