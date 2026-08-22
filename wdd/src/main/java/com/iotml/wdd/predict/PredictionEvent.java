package com.iotml.wdd.predict;

import java.time.LocalDateTime;

public record PredictionEvent(
        String readingId,
        String prediction,
        double confidence,
        String modelVersion,
        LocalDateTime scoredAt
) {
}
