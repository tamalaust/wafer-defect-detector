package com.iotml.wdd.predict;

public record PredictResponse(
        String readingId,
        String prediction,
        double confidence,
        String modelVersion
) {
}