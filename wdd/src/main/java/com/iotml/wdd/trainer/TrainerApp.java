package com.iotml.wdd.trainer;

import smile.classification.RandomForest;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

/**
 * Run directly (e.g. via IDE "Run" on this class) - a plain main(), does not
 * boot the Spring context or web server.
 *
 * DB connection settings are read from the same application.properties the
 * Spring app uses (spring.datasource.url/username/password) via
 * DbConfigLoader - no separate config to maintain.
 *
 * Model artifact is written to models/wafer-defect-rf-v1.model, relative to
 * the project root (same location data/ lives).
 */
public class TrainerApp {

    private static final Path MODEL_OUTPUT_PATH =
            Path.of("models/wafer-defect-rf-v1.model");

    public static void main(String[] args) throws SQLException {
        DbConfigLoader.DbConfig dbConfig = new DbConfigLoader().load();

        System.out.println("Connecting to " + dbConfig.url() + " ...");

        try (Connection connection = DriverManager.getConnection(
                dbConfig.url(), dbConfig.user(), dbConfig.password())) {

            TrainingDataLoader loader = new TrainingDataLoader(connection);
            List<TrainingRow> rows = loader.load();
            System.out.println("Loaded " + rows.size() + " labeled readings.");

            ModelTrainer trainer = new ModelTrainer();
            RandomForest model = trainer.train(rows);
            System.out.println("Training complete.");
            System.out.println(model.metrics());

            ModelPersistence persistence = new ModelPersistence();
            persistence.save(model, MODEL_OUTPUT_PATH);
            System.out.println("Model saved to " + MODEL_OUTPUT_PATH.toAbsolutePath());

        } catch (Exception e) {
            System.err.println("Training failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}