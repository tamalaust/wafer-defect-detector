package com.iotml.wdd.ingestion.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "sensor_feature")
public class SensorFeature {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reading_id", nullable = false)
    private SensorReading reading;

    @Column(name = "feature_index", nullable = false)
    private int featureIndex;

    @Column(name = "value")
    private Double value;

    // Stored as "true"/"false" String per current design decision.
    // (Native Boolean would work identically if that decision changes later.)
    @Column(name = "is_missing", nullable = false)
    private String isMissing;

    protected SensorFeature() {
        // JPA
    }

    public SensorFeature(int featureIndex, Double value, boolean missing) {
        this.featureIndex = featureIndex;
        this.value = value;
        this.isMissing = String.valueOf(missing);
    }

    void setReading(SensorReading reading) {
        this.reading = reading;
    }

    public Long getId() {
        return id;
    }

    public SensorReading getReading() {
        return reading;
    }

    public int getFeatureIndex() {
        return featureIndex;
    }

    public Double getValue() {
        return value;
    }

    public String getIsMissing() {
        return isMissing;
    }
}