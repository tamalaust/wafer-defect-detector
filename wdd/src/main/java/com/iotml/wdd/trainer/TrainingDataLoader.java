package com.iotml.wdd.trainer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Reads directly from Postgres (sensor_reading / sensor_feature) rather than
 * the raw SECOM files, so training is guaranteed consistent with whatever is
 * actually in the DB. No Spring/JPA - plain JDBC, since this is a standalone,
 * read-only, one-shot batch job.
 *
 * N+1 query pattern (one feature query per reading) - acceptable for a
 * one-time training run over ~1567 rows; flagged as a known simplification,
 * not an oversight, if this ever needs to scale up.
 */
public class TrainingDataLoader {

    private final Connection connection;

    public TrainingDataLoader(Connection connection) {
        this.connection = connection;
    }

    public List<TrainingRow> load() throws SQLException {
        int featureCount = findMaxFeatureIndex();
        Map<Integer, Double> featureMeans = computeFeatureMeans();
        List<ReadingHeader> readings = findLabeledReadings();

        List<TrainingRow> rows = new ArrayList<>(readings.size());
        for (ReadingHeader header : readings) {
            double[] vector = buildFeatureVector(header.id(), featureCount, featureMeans);
            rows.add(new TrainingRow(header.waferId(), header.label(), vector));
        }

        return rows;
    }

    private int findMaxFeatureIndex() throws SQLException {
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT MAX(feature_index) FROM sensor_feature")) {
            rs.next();
            return rs.getInt(1);
        }
    }

    private Map<Integer, Double> computeFeatureMeans() throws SQLException {
        String sql = """
                SELECT feature_index, AVG(value) AS mean_value
                FROM sensor_feature
                WHERE is_missing = 'false'
                GROUP BY feature_index
                """;

        Map<Integer, Double> means = new HashMap<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                means.put(rs.getInt("feature_index"), rs.getDouble("mean_value"));
            }
        }
        return means;
    }

    private List<ReadingHeader> findLabeledReadings() throws SQLException {
        String sql = """
                SELECT id, wafer_id, pass_fail
                FROM sensor_reading
                WHERE pass_fail IS NOT NULL
                ORDER BY id
                """;

        List<ReadingHeader> readings = new ArrayList<>();
        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                long id = rs.getLong("id");
                String waferId = rs.getString("wafer_id");
                String passFail = rs.getString("pass_fail");
                int label = "FAIL".equals(passFail) ? 1 : 0;
                readings.add(new ReadingHeader(id, waferId, label));
            }
        }
        return readings;
    }

    private double[] buildFeatureVector(long readingId, int featureCount, Map<Integer, Double> means)
            throws SQLException {
        double[] vector = new double[featureCount];

        // Start every slot at its feature's mean; overwrite with the real
        // value below where one was actually present.
        for (int i = 0; i < featureCount; i++) {
            vector[i] = means.getOrDefault(i + 1, 0.0);
        }

        String sql = """
                SELECT feature_index, value, is_missing
                FROM sensor_feature
                WHERE reading_id = ?
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setLong(1, readingId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int featureIndex = rs.getInt("feature_index");
                    boolean missing = "true".equals(rs.getString("is_missing"));
                    if (!missing) {
                        vector[featureIndex - 1] = rs.getDouble("value");
                    }
                }
            }
        }

        return vector;
    }

    private record ReadingHeader(long id, String waferId, int label) {
    }
}