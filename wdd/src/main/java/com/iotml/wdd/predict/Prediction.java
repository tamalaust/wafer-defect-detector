package com.iotml.wdd.predict;

import com.iotml.wdd.ingestion.entity.SensorReading;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * One row per scored reading. Separate table (not columns on sensor_reading)
 * -- naturally supports re-scoring history later (e.g. comparing model
 * versions) without needing a schema change, at the cost of a join for the
 * "what's pending" query.
 */
@Entity
@Table(name = "prediction")
public class Prediction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reading_id", nullable = false)
    private SensorReading reading;

    @Column(name = "prediction", nullable = false)
    private String prediction; // "PASS" / "FAIL"

    @Column(name = "confidence", nullable = false)
    private double confidence;

    @Column(name = "model_version", nullable = false)
    private String modelVersion;

    @Column(name = "scored_at", nullable = false)
    private LocalDateTime scoredAt;

    protected Prediction() {
        // JPA
    }

    public Prediction(SensorReading reading, String prediction, double confidence,
                      String modelVersion, LocalDateTime scoredAt) {
        this.reading = reading;
        this.prediction = prediction;
        this.confidence = confidence;
        this.modelVersion = modelVersion;
        this.scoredAt = scoredAt;
    }

    public Long getId() {
        return id;
    }

    public SensorReading getReading() {
        return reading;
    }

    public String getPrediction() {
        return prediction;
    }

    public double getConfidence() {
        return confidence;
    }

    public String getModelVersion() {
        return modelVersion;
    }

    public LocalDateTime getScoredAt() {
        return scoredAt;
    }
}