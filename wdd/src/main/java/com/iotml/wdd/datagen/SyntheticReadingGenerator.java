package com.iotml.wdd.datagen;

import com.iotml.wdd.trainer.DbConfigLoader;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.StringJoiner;

/**
 * Generates synthetic-but-realistic wafer readings (Gaussian noise around
 * each real sensor's actual mean/stddev, computed from your seeded data) and
 * POSTs them to /api/sensor-readings/bulk in chunks -- purely for creating
 * demo load for BatchScoringJob to work through.
 *
 * Not through Spring -- plain main(), same style as TrainerApp. Reuses
 * DbConfigLoader (already handles the ${VAR_NAME} placeholder resolution in
 * application.properties) for DB stats, and java.net.http.HttpClient (built
 * into the JDK, no extra dependency) to call the running app's own API.
 *
 * Your Spring Boot app must be RUNNING before you execute this -- it POSTs
 * over real HTTP to whatever WDD_API_BASE_URL points at (default
 * http://localhost:8080).
 */
public class SyntheticReadingGenerator {

    private static final int TOTAL_READINGS = 1000;
    private static final int CHUNK_SIZE = 50; // readings per HTTP call
    private static final Random RANDOM = new Random();

    public static void main(String[] args) throws Exception {
        String apiBaseUrl = envOrDefault("WDD_API_BASE_URL", "http://localhost:8080");
        DbConfigLoader.DbConfig dbConfig = new DbConfigLoader().load();

        Map<Integer, double[]> featureStats; // featureIndex -> [mean, stddev]
        int featureCount;

        try (Connection connection = DriverManager.getConnection(
                dbConfig.url(), dbConfig.user(), dbConfig.password())) {

            featureCount = findMaxFeatureIndex(connection);
            featureStats = computeFeatureStats(connection);
            System.out.println("Computed stats for " + featureStats.size() + " features.");
        }

        HttpClient httpClient = HttpClient.newHttpClient();
        int sent = 0;

        while (sent < TOTAL_READINGS) {
            int chunkCount = Math.min(CHUNK_SIZE, TOTAL_READINGS - sent);
            String jsonBody = buildChunkJson(chunkCount, featureCount, featureStats);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiBaseUrl + "/api/sensor-readings/bulk"))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 201) {
                throw new IllegalStateException(
                        "Bulk ingest failed: HTTP " + response.statusCode() + " - " + response.body());
            }

            sent += chunkCount;
            System.out.println("Sent " + sent + "/" + TOTAL_READINGS);
        }

        System.out.println("Done. " + TOTAL_READINGS + " synthetic readings sent.");
    }

    private static int findMaxFeatureIndex(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT MAX(feature_index) FROM sensor_feature")) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private static Map<Integer, double[]> computeFeatureStats(Connection connection) throws SQLException {
        String sql = """
                SELECT feature_index, AVG(value) AS mean_value, STDDEV_SAMP(value) AS stddev_value
                FROM sensor_feature
                WHERE is_missing = 'false'
                GROUP BY feature_index
                """;

        Map<Integer, double[]> stats = new HashMap<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int featureIndex = rs.getInt("feature_index");
                double mean = rs.getDouble("mean_value");
                double stddev = rs.getDouble("stddev_value");
                // A feature with only one distinct present value has a null
                // (0.0 after getDouble) stddev -- fall back to a small
                // nonzero spread so generation doesn't collapse to a constant.
                if (stddev <= 0.0) {
                    stddev = Math.max(Math.abs(mean) * 0.01, 0.01);
                }
                stats.put(featureIndex, new double[]{mean, stddev});
            }
        }
        return stats;
    }

    private static String buildChunkJson(int count, int featureCount, Map<Integer, double[]> stats) {
        StringJoiner readings = new StringJoiner(",", "[", "]");

        for (int i = 0; i < count; i++) {
            readings.add(buildReadingJson(featureCount, stats));
        }

        return readings.toString();
    }

    private static String buildReadingJson(int featureCount, Map<Integer, double[]> stats) {
        String waferId = "SYN-" + RANDOM.nextLong(1_000_000_000L);
        String timestamp = Instant.now().toString();

        StringJoiner features = new StringJoiner(",", "{", "}");
        for (int i = 1; i <= featureCount; i++) {
            double[] meanStddev = stats.getOrDefault(i, new double[]{0.0, 1.0});
            double value = meanStddev[0] + RANDOM.nextGaussian() * meanStddev[1];
            features.add("\"sensor_" + i + "\":" + value);
        }

        return "{\"waferId\":\"" + waferId + "\","
                + "\"timestamp\":\"" + timestamp + "\","
                + "\"features\":" + features + "}";
    }

    private static String envOrDefault(String key, String defaultValue) {
        String value = System.getenv(key);
        return (value != null) ? value : defaultValue;
    }
}