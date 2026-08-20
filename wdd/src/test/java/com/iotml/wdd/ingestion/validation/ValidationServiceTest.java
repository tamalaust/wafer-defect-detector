package com.iotml.wdd.ingestion.validation;

import com.iotml.wdd.ingestion.entity.SensorFeature;
import com.iotml.wdd.ingestion.entity.SensorReading;
import com.iotml.wdd.ingestion.parser.PassFail;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ValidationServiceTest {

    private final ValidationService validationService = new ValidationService(-1000.0, 1000.0);

    @Test
    void countsMissingAndOutOfRangeFeaturesSeparately() {
        SensorReading reading = new SensorReading("W-1", LocalDateTime.now(), PassFail.PASS);
        reading.addFeature(new SensorFeature(1, 500.0, false));   // in range
        reading.addFeature(new SensorFeature(2, null, true));     // missing
        reading.addFeature(new SensorFeature(3, 5000.0, false));  // out of range
        reading.addFeature(new SensorFeature(4, -50.0, false));   // in range

        ValidationResult result = validationService.validate(reading);

        assertEquals(4, result.totalFeatureCount());
        assertEquals(1, result.missingFeatureCount());
        assertEquals(1, result.outOfRangeFeatureCount());
    }

    @Test
    void allPresentAndInRange_reportsNoFlags() {
        SensorReading reading = new SensorReading("W-2", LocalDateTime.now(), PassFail.FAIL);
        reading.addFeature(new SensorFeature(1, 10.0, false));
        reading.addFeature(new SensorFeature(2, -10.0, false));

        ValidationResult result = validationService.validate(reading);

        assertEquals(0, result.missingFeatureCount());
        assertEquals(0, result.outOfRangeFeatureCount());
    }
}