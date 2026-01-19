package com.cts.vis.util;

import com.cts.vis.model.VehicleType;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Year;

@Component
public class PremiumCalculator {

    public BigDecimal calculate(VehicleType type, int yearOfManufacture, BigDecimal coverageAmount) {
        BigDecimal base = switch (type) {
            case CAR -> new BigDecimal("1000");
            case BIKE -> new BigDecimal("500");
            case TRUCK -> new BigDecimal("1500");
        };

        int age = Math.max(0, Year.now().getValue() - yearOfManufacture);

        BigDecimal ageFactor = (age <= 3) ? new BigDecimal("1.00")
                : (age <= 7) ? new BigDecimal("1.10")
                : (age <= 12) ? new BigDecimal("1.25")
                : new BigDecimal("1.40");

        BigDecimal coverageFactor = coverageAmount.multiply(new BigDecimal("0.008"));

        BigDecimal premium = base.multiply(ageFactor).add(coverageFactor);
        return premium.setScale(2, RoundingMode.HALF_UP);
    }
}