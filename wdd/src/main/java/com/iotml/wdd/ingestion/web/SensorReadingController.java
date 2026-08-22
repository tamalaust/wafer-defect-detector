package com.iotml.wdd.ingestion.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    /**
     * Bulk path -- accepts a JSON array in the request body (not a raw
     * multipart file). A UI can read a local file client-side and POST its
     * parsed contents here, which covers "upload a file of readings" from
     * the user's perspective without needing server-side file/multipart
     * parsing.
     */
    @PostMapping("/bulk")
    public ResponseEntity<BulkIngestResponse> ingestBulk(@RequestBody List<SensorReadingRequest> requests) {
        BulkIngestResponse response = ingestionService.ingestBulk(requests);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}