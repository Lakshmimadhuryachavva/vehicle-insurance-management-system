
    package com.cts.insurance.vehicle_insurance.model;

    import jakarta.persistence.*;
    import java.math.BigDecimal;
    import java.time.LocalDate;

    @Entity
    @Table(name = "claim")
    public class Claim {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        public Long claimId;

        @ManyToOne(optional = false)
        @JoinColumn(name = "policy_id", nullable = false,
                foreignKey = @ForeignKey(name = "fk_claim_policy"))
        public Policy policy;

        @Column(precision = 10, scale = 2, nullable = false)
        public BigDecimal claimAmount;

        @Column(columnDefinition = "TEXT", nullable = false)
        public String claimReason;

        @Column(nullable = false)
        public LocalDate claimDate;

        @Enumerated(EnumType.STRING)
        @Column(length = 10, nullable = false)
        public ClaimStatus claimStatus;
    }

    /* Inline enum (same package/file).
       Keep it non-public to respect Java’s one-public-type-per-file rule.
    */
    enum ClaimStatus {
        SUBMITTED, APPROVED, REJECTED
    }
