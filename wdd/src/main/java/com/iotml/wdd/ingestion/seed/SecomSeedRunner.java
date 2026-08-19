package com.iotml.wdd.ingestion.seed;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

/**
 * Runs once at application startup (Spring Boot's CommandLineRunner contract).
 * Force re-seed: java -jar app.jar --secom.seed.force=true
 */
@Component
public class SecomSeedRunner implements CommandLineRunner {

    private final SecomSeedService seedService;

    @Value("${secom.seed.force:false}")
    private boolean forceReseed;

    @Value("${secom.seed.data-path:data/secom.data}")
    private String dataPath;

    @Value("${secom.seed.labels-path:data/secom_labels.data}")
    private String labelsPath;

    public SecomSeedRunner(SecomSeedService seedService) {
        this.seedService = seedService;
    }

    @Override
    public void run(String... args) throws Exception {
        if (seedService.isAlreadyCompleted() && !forceReseed) {
            System.out.println("SECOM seed already completed — skipping (use --secom.seed.force=true to re-run).");
            return;
        }

        if (forceReseed) {
            System.out.println("Force reseed requested — clearing existing data.");
            seedService.clearExistingData();
        }

        int count = seedService.seed(Path.of(dataPath), Path.of(labelsPath));
        System.out.println("Seeded " + count + " readings.");
    }
}