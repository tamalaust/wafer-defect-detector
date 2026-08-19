package com.iotml.wdd.ingestion.validation;

import com.iotml.wdd.ingestion.entity.SensorFeature;
import com.iotml.wdd.ingestion.entity.SensorReading;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Computes a per-reading validation summary: how many features were missing,
 * and how many present values fell outside a configured range.
 *
 * Range is a single global [min, max] applied to every sensor uniformly —
 * a v1 simplification. Real SECOM sensors vary widely in scale from one
 * another, so this range is a coarse "does this look like a sane number"
 * check, not a per-sensor-calibrated one. Revisit if that distinction
 * turns out to matter.
 */
@Service
public class ValidationService {

    private static final String MISSING = "true";

    private final double rangeMin;
    private final double rangeMax;

    public ValidationService(
            @Value("${secom.validation.range.min}") double rangeMin,
            @Value("${secom.validation.range.max}") double rangeMax
    ) {
        this.rangeMin = rangeMin;
        this.rangeMax = rangeMax;
    }

    public ValidationResult validate(SensorReading reading) {
        int total = 0;
        int missing = 0;
        int outOfRange = 0;

        for (SensorFeature feature : reading.getFeatures()) {
            total++;

            if (MISSING.equals(feature.getIsMissing())) {
                missing++;
                continue;
            }

            double value = feature.getValue();
            if (value < rangeMin || value > rangeMax) {
                outOfRange++;
            }
        }

        return new ValidationResult(total, missing, outOfRange);
    }
}