package com.iotml.wdd.ingestion.validation;

public record ValidationResult(
        int totalFeatureCount,
        int missingFeatureCount,
        int outOfRangeFeatureCount
) {
}