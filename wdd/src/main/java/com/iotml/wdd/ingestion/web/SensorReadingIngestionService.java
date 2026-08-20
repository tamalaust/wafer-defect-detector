package com.iotml.wdd.ingestion.web;

import com.iotml.wdd.ingestion.entity.SensorReading;
import com.iotml.wdd.ingestion.repository.SensorReadingRepository;
import com.iotml.wdd.ingestion.validation.ValidationResult;
import com.iotml.wdd.ingestion.validation.ValidationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}