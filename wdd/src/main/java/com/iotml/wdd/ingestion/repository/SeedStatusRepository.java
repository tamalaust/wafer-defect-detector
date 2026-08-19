package com.iotml.wdd.ingestion.repository;

import com.iotml.wdd.ingestion.entity.SeedStatus;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeedStatusRepository extends JpaRepository<SeedStatus, String> {
}
