package com.iotml.wdd.ingestion.web;

import com.iotml.wdd.ingestion.entity.SensorFeature;
import com.iotml.wdd.ingestion.entity.SensorReading;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Map;

/**
 * Converts a client-facing SensorReadingRequest (flat JSON, "sensor_N" keys)
 * into the normalized entity graph. This is the reshape point mentioned in
 * the design doc — the API contract stays flat regardless of the underlying
 * schema.
 */
public class SensorReadingRequestMapper {

    private static final String SENSOR_KEY_PREFIX = "sensor_";

    public SensorReading toEntity(SensorReadingRequest request) {
        LocalDateTime timestamp = LocalDateTime.ofInstant(request.timestamp(), ZoneOffset.UTC);
        SensorReading reading = new SensorReading(request.waferId(), timestamp);

        for (Map.Entry<String, Double> entry : request.features().entrySet()) {
            int featureIndex = parseFeatureIndex(entry.getKey());
            Double value = entry.getValue();
            boolean missing = (value == null);

            reading.addFeature(new SensorFeature(featureIndex, value, missing));
        }

        return reading;
    }

    private int parseFeatureIndex(String key) {
        // "sensor_42" -> 42. Happy-path only: assumes the "sensor_" prefix is always present.
        return Integer.parseInt(key.substring(SENSOR_KEY_PREFIX.length()));
    }
}