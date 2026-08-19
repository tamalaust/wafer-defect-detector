package com.iotml.wdd.ingestion.parser;

/**
 * featureIndex is 1-based position in the row (sensor_1..sensor_590),
 * matching the normalized schema's feature_index column.
 * value is null when missing = true — the "NaN" token in the source
 * file is never parsed as a number.
 */
public record ParsedFeature(
        int featureIndex,
        Double value,
        boolean missing
) {
}
