package com.iotml.wdd.ingestion.web;

import com.iotml.wdd.ingestion.entity.SensorFeature;
import com.iotml.wdd.ingestion.entity.SensorReading;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SensorReadingRequestMapperTest {

    private final SensorReadingRequestMapper mapper = new SensorReadingRequestMapper();

    @Test
    void mapsFlatRequestIntoNormalizedFeatures() {
        Map<String, Double> features = new LinkedHashMap<>();
        features.put("sensor_1", 100.5);
        features.put("sensor_2", null);

        SensorReadingRequest request = new SensorReadingRequest(
                "W-TEST", Instant.parse("2026-08-20T09:15:00Z"), features);

        SensorReading reading = mapper.toEntity(request);

        assertEquals("W-TEST", reading.getWaferId());
        assertEquals(2, reading.getFeatures().size());
        assertNull(reading.getPassFail()); // no label yet -- live ingestion, not seeded

        SensorFeature sensor1 = findByIndex(reading, 1);
        assertEquals(100.5, sensor1.getValue());
        assertEquals("false", sensor1.getIsMissing());

        SensorFeature sensor2 = findByIndex(reading, 2);
        assertNull(sensor2.getValue());
        assertEquals("true", sensor2.getIsMissing());
    }

    private SensorFeature findByIndex(SensorReading reading, int index) {
        return reading.getFeatures().stream()
                .filter(f -> f.getFeatureIndex() == index)
                .findFirst()
                .orElseThrow();
    }
}