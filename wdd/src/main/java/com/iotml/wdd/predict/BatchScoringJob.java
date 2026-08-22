package com.iotml.wdd.predict;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Ticks on a fixed delay, scores a small batch of pending readings each
 * time (not everything at once) -- keeps this looking like a steady trickle
 * rather than an instantaneous dump, since that pacing is what will make the
 * dashboard (SSE, next phase) actually feel live rather than just correct.
 *
 * Config: wdd.batch.size (default 10), wdd.batch.interval-ms (default 5000).
 */
@Component
public class BatchScoringJob {

    private final PredictionRepository predictionRepository;
    private final PredictionService predictionService;

    @Value("${wdd.batch.size:10}")
    private int batchSize;

    public BatchScoringJob(PredictionRepository predictionRepository, PredictionService predictionService) {
        this.predictionRepository = predictionRepository;
        this.predictionService = predictionService;
    }

    @Scheduled(fixedDelayString = "${wdd.batch.interval-ms:5000}")
    public void scorePendingBatch() {
        List<Long> pendingIds = predictionRepository.findPendingReadingIds(PageRequest.of(0, batchSize));

        if (pendingIds.isEmpty()) {
            return;
        }

        int scored = 0;
        for (Long readingId : pendingIds) {
            try {
                predictionService.predict(readingId);
                scored++;
            } catch (Exception e) {
                // Happy-path scope: log and continue rather than letting one
                // bad reading kill the whole scheduled tick.
                System.err.println("Failed to score reading " + readingId + ": " + e.getMessage());
            }
        }

        System.out.println("Batch scored " + scored + "/" + pendingIds.size() + " readings.");
    }
}