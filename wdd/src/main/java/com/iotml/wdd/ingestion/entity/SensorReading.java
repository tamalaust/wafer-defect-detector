package com.iotml.wdd.ingestion.entity;

import com.iotml.wdd.ingestion.parser.PassFail;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sensor_reading")
public class SensorReading {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "wafer_id", nullable = false, unique = true)
    private String waferId;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Enumerated(EnumType.STRING)
    @Column(name = "pass_fail")
    private PassFail passFail;

    @OneToMany(mappedBy = "reading", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SensorFeature> features = new ArrayList<>();

    protected SensorReading() {
        // JPA
    }

    /**
     * Historical/seeded reading — label already known.
     */
    public SensorReading(String waferId, LocalDateTime timestamp, PassFail passFail) {
        this.waferId = waferId;
        this.timestamp = timestamp;
        this.passFail = passFail;
    }

    /**
     * Live-ingested reading — no label yet; determined later via /api/predict.
     */
    public SensorReading(String waferId, LocalDateTime timestamp) {
        this(waferId, timestamp, null);
    }

    public void addFeature(SensorFeature feature) {
        features.add(feature);
        feature.setReading(this);
    }

    public Long getId() {
        return id;
    }

    public String getWaferId() {
        return waferId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public PassFail getPassFail() {
        return passFail;
    }

    public List<SensorFeature> getFeatures() {
        return features;
    }
}