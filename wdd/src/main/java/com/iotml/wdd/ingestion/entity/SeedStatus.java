package com.iotml.wdd.ingestion.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "seed_status")
public class SeedStatus {

    @Id
    @Column(name = "seed_name")
    private String seedName;

    @Column(name = "status", nullable = false)
    private String status; // "COMPLETED", "IN_PROGRESS", "FAILED"

    @Column(name = "row_count")
    private Integer rowCount;

    @Column(name = "ran_at")
    private LocalDateTime ranAt;

    protected SeedStatus() {
        // JPA
    }

    public SeedStatus(String seedName, String status, Integer rowCount, LocalDateTime ranAt) {
        this.seedName = seedName;
        this.status = status;
        this.rowCount = rowCount;
        this.ranAt = ranAt;
    }

    public String getSeedName() {
        return seedName;
    }

    public String getStatus() {
        return status;
    }

    public Integer getRowCount() {
        return rowCount;
    }

    public LocalDateTime getRanAt() {
        return ranAt;
    }
}