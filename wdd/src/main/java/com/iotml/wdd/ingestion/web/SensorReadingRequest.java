package com.iotml.wdd.ingestion.web;

import java.time.Instant;
import java.util.Map;

/**
 * features keys are "sensor_1".."sensor_590"; a null value for a key means
 * that sensor's reading was missing (matches the SECOM "NaN" convention).
 */
public record SensorReadingRequest(
        String waferId,
        Instant timestamp,
        Map<String, Double> features
) {
}
