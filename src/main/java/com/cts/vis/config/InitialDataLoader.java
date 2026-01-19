package com.cts.vis.config;

import com.cts.vis.model.*;
import com.cts.vis.repository.*;
import com.cts.vis.util.PolicyNumberGenerator;
import com.cts.vis.util.PremiumCalculator;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
public class InitialDataLoader implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final PolicyRepository policyRepository;
    private final ClaimRepository claimRepository;

    private final PasswordEncoder passwordEncoder;
    private final PolicyNumberGenerator policyNumberGenerator;
    private final PremiumCalculator premiumCalculator;

    @Override
    public void run(String... args) {

        // Admin seed
        userRepository.findByEmail("admin@insurance.com").orElseGet(() ->
                userRepository.save(User.builder()
                        .email("admin@insurance.com")
                        .passwordHash(passwordEncoder.encode("admin123"))
                        .role(UserRole.ROLE_ADMIN)
                        .build())
        );

        // Demo customer seed
        if (userRepository.findByEmail("john@example.com").isEmpty()) {

            User u = userRepository.save(User.builder()
                    .email("john@example.com")
                    .passwordHash(passwordEncoder.encode("john123"))
                    .role(UserRole.ROLE_CUSTOMER)
                    .build());

            Customer c = customerRepository.save(Customer.builder()
                    .name("John Doe")
                    .email("john@example.com")
                    .phone("123-456-7890")
                    .address("123 Main St")
                    .user(u)
                    .build());

            Vehicle v1 = vehicleRepository.save(Vehicle.builder()
                    .customer(c)
                    .registrationNumber("ABC123")
                    .make("Toyota")
                    .model("Camry")
                    .yearOfManufacture(2020)
                    .vehicleType(VehicleType.CAR)
                    .build());

            Vehicle v2 = vehicleRepository.save(Vehicle.builder()
                    .customer(c)
                    .registrationNumber("XYZ789")
                    .make("Honda")
                    .model("Civic")
                    .yearOfManufacture(2021)
                    .vehicleType(VehicleType.CAR)
                    .build());

            BigDecimal cov1 = new BigDecimal("50000");
            BigDecimal prem1 = premiumCalculator.calculate(v1.getVehicleType(), v1.getYearOfManufacture(), cov1);

            Policy p1 = policyRepository.save(Policy.builder()
                    .vehicle(v1)
                    .policyNumber(policyNumberGenerator.generate())
                    .coverageAmount(cov1)
                    .premiumAmount(prem1)
                    .startDate(LocalDate.now().minusMonths(2))
                    .endDate(LocalDate.now().plusMonths(10))
                    .policyStatus(PolicyStatus.ACTIVE)
                    .build());

            BigDecimal cov2 = new BigDecimal("40000");
            BigDecimal prem2 = premiumCalculator.calculate(v2.getVehicleType(), v2.getYearOfManufacture(), cov2);

            Policy p2 = policyRepository.save(Policy.builder()
                    .vehicle(v2)
                    .policyNumber(policyNumberGenerator.generate())
                    .coverageAmount(cov2)
                    .premiumAmount(prem2)
                    .startDate(LocalDate.now().minusMonths(1))
                    .endDate(LocalDate.now().plusMonths(11))
                    .policyStatus(PolicyStatus.ACTIVE)
                    .build());

            claimRepository.save(Claim.builder()
                    .policy(p1)
                    .claimAmount(new BigDecimal("5000"))
                    .claimReason("Accident damage to front bumper")
                    .claimDate(LocalDate.now().minusDays(10))
                    .claimStatus(ClaimStatus.SUBMITTED)
                    .build());

            claimRepository.save(Claim.builder()
                    .policy(p2)
                    .claimAmount(new BigDecimal("3000"))
                    .claimReason("Windshield replacement")
                    .claimDate(LocalDate.now().minusDays(25))
                    .claimStatus(ClaimStatus.APPROVED)
                    .build());
        }
    }
}