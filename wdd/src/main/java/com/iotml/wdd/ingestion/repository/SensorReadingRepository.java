package com.iotml.wdd.ingestion.repository;

import com.iotml.wdd.ingestion.entity.SensorReading;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SensorReadingRepository extends JpaRepository<SensorReading, Long> {
}