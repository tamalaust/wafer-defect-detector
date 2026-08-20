package com.iotml.wdd.ingestion.web;

public record SensorReadingResponse(
        String readingId,
        String status,
        ValidationSummary validation
) {

    public record ValidationSummary(
            int missingFeatureCount,
            int outOfRangeFeatureCount
    ) {
    }
}