package com.iotml.wdd.predict;

public record PredictionResult(
        String readingId,
        String prediction,
        double confidence
) {
}