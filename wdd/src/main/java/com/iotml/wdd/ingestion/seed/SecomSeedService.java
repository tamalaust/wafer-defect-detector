package com.iotml.wdd.ingestion.seed;

import com.iotml.wdd.ingestion.entity.SeedStatus;
import com.iotml.wdd.ingestion.entity.SensorReading;
import com.iotml.wdd.ingestion.parser.ParsedReading;
import com.iotml.wdd.ingestion.parser.SecomFileParser;
import com.iotml.wdd.ingestion.repository.SeedStatusRepository;
import com.iotml.wdd.ingestion.repository.SensorReadingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Owns the transactional boundary for seeding — kept as a distinct Spring bean
 * (rather than methods on the CommandLineRunner) so @Transactional actually
 * applies. Spring's proxy-based transactions don't intercept self-invocation
 * (a method calling another method on the same instance) — splitting this into
 * its own service means the runner calls it as a cross-bean call, which the
 * proxy does intercept correctly.
 */
@Service
public class SecomSeedService {

    private static final String SEED_NAME = "secom_initial";

    private final SensorReadingRepository sensorReadingRepository;
    private final SeedStatusRepository seedStatusRepository;
    private final SecomFileParser parser = new SecomFileParser();
    private final ParsedReadingMapper mapper = new ParsedReadingMapper();

    public SecomSeedService(SensorReadingRepository sensorReadingRepository,
                            SeedStatusRepository seedStatusRepository) {
        this.sensorReadingRepository = sensorReadingRepository;
        this.seedStatusRepository = seedStatusRepository;
    }

    public boolean isAlreadyCompleted() {
        return seedStatusRepository.findById(SEED_NAME)
                .map(s -> "COMPLETED".equals(s.getStatus()))
                .orElse(false);
    }

    @Transactional
    public void clearExistingData() {
        sensorReadingRepository.deleteAllInBatch();
        seedStatusRepository.deleteById(SEED_NAME);
    }

    @Transactional
    public int seed(Path dataPath, Path labelsPath) throws IOException {
        List<ParsedReading> parsedReadings = parser.parse(dataPath, labelsPath);

        List<SensorReading> entities = parsedReadings.stream()
                .map(mapper::toEntity)
                .toList();

        sensorReadingRepository.saveAll(entities);

        seedStatusRepository.save(
                new SeedStatus(SEED_NAME, "COMPLETED", entities.size(), LocalDateTime.now())
        );

        return entities.size();
    }
}
