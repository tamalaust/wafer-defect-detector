package com.iotml.wdd.ingestion.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sensor-readings")
public class SensorReadingController {

    private final SensorReadingIngestionService ingestionService;

    public SensorReadingController(SensorReadingIngestionService ingestionService) {
        this.ingestionService = ingestionService;
    }

    @PostMapping
    public ResponseEntity<SensorReadingResponse> ingest(@RequestBody SensorReadingRequest request) {
        SensorReadingResponse response = ingestionService.ingest(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}