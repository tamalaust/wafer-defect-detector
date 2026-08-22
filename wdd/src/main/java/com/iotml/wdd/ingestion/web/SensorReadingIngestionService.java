package com.iotml.wdd.ingestion.web;

import com.iotml.wdd.ingestion.entity.SensorReading;
import com.iotml.wdd.ingestion.repository.SensorReadingRepository;
import com.iotml.wdd.ingestion.validation.ValidationResult;
import com.iotml.wdd.ingestion.validation.ValidationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SensorReadingIngestionService {

    private final SensorReadingRepository sensorReadingRepository;
    private final ValidationService validationService;
    private final SensorReadingRequestMapper mapper = new SensorReadingRequestMapper();

    public SensorReadingIngestionService(SensorReadingRepository sensorReadingRepository,
                                         ValidationService validationService) {
        this.sensorReadingRepository = sensorReadingRepository;
        this.validationService = validationService;
    }

    @Transactional
    public SensorReadingResponse ingest(SensorReadingRequest request) {
        SensorReading reading = mapper.toEntity(request);
        SensorReading saved = sensorReadingRepository.save(reading);

        ValidationResult validation = validationService.validate(saved);

        return new SensorReadingResponse(
                String.valueOf(saved.getId()),
                "INGESTED",
                new SensorReadingResponse.ValidationSummary(
                        validation.missingFeatureCount(),
                        validation.outOfRangeFeatureCount()
                )
        );
    }

    /**
     * Bulk path -- built for loading demo/synthetic data efficiently (e.g.
     * from a UI-uploaded file's parsed contents), not for per-row validation
     * feedback. Maps and saves everything in one transaction (saveAll, same
     * pattern as the seed) rather than looping single saves. Skips returning
     * a per-row validation summary to keep the response small for a large
     * batch -- individual readings can still be checked later via
     * /api/predict or a DB query if needed.
     */
    @Transactional
    public BulkIngestResponse ingestBulk(List<SensorReadingRequest> requests) {
        List<SensorReading> entities = requests.stream()
                .map(mapper::toEntity)
                .toList();

        List<SensorReading> saved = sensorReadingRepository.saveAll(entities);

        return new BulkIngestResponse(requests.size(), saved.size());
    }
}