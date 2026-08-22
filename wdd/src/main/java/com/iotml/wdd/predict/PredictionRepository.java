package com.iotml.wdd.predict;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface PredictionRepository extends JpaRepository<Prediction, Long> {

    /**
     * "Pending" = live-ingested (pass_fail IS NULL -- seeded historical rows
     * always have a label) AND not yet scored (no Prediction row exists yet
     * for it). Limited via Pageable so the batch job can pull small chunks
     * rather than everything pending at once.
     */
    @Query("""
            SELECT r.id FROM SensorReading r
            WHERE r.passFail IS NULL
              AND NOT EXISTS (SELECT 1 FROM Prediction p WHERE p.reading = r)
            ORDER BY r.id
            """)
    List<Long> findPendingReadingIds(Pageable pageable);
}