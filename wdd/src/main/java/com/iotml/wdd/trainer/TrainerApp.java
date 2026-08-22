package com.iotml.wdd.trainer;

import com.iotml.wdd.model.ModelBundle;
import com.iotml.wdd.model.ModelPersistence;
import smile.classification.RandomForest;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Run directly (e.g. via IDE "Run" on this class) - a plain main(), does not
 * boot the Spring context or web server.
 *
 * DB connection settings are read from the same application.properties the
 * Spring app uses (spring.datasource.url/username/password) via
 * DbConfigLoader - no separate config to maintain.
 *
 * Model artifact (model + training-time feature means, bundled together) is
 * written to models/wafer-defect-rf-v1.model, relative to the project root
 * (same location data/ lives).
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
            TrainingDataLoader.LoadResult loadResult = loader.load();
            System.out.println("Loaded " + loadResult.rows().size() + " labeled readings.");

            ModelTrainer trainer = new ModelTrainer();
            RandomForest model = trainer.train(loadResult.rows());
            System.out.println("Training complete.");
            System.out.println(model.metrics());

            ModelBundle bundle = new ModelBundle(
                    model, loadResult.featureMeans(), loadResult.featureCount());

            ModelPersistence persistence = new ModelPersistence();
            persistence.save(bundle, MODEL_OUTPUT_PATH);
            System.out.println("Model saved to " + MODEL_OUTPUT_PATH.toAbsolutePath());

        } catch (Exception e) {
            System.err.println("Training failed: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}