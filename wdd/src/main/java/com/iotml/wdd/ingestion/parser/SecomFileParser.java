package com.iotml.wdd.ingestion.parser;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Parses SECOM's two-file format into a list of ParsedReading.
 *
 * secom.data:        1567 lines, ~590 whitespace-separated values per line.
 *                     Missing values are the literal token "NaN".
 * secom_labels.data: 1567 lines, "<label> <timestamp>" per line, e.g.
 *                     "-1  19/07/2008 11:55:00"
 *
 * The two files carry no shared key — correlation is by line position only,
 * so both readers are walked in lockstep (zip-iteration). No wafer ID exists
 * in the source data, so one is synthesized from row index.
 *
 * Happy-path only: assumes both files have matching line counts and
 * well-formed rows, per current DoD scope.
 */
public class SecomFileParser {

    private static final String MISSING_TOKEN = "NaN";
    private static final DateTimeFormatter LABEL_TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public List<ParsedReading> parse(Path dataFile, Path labelsFile) throws IOException {
        List<ParsedReading> readings = new ArrayList<>();

        try (BufferedReader dataReader = Files.newBufferedReader(dataFile);
             BufferedReader labelReader = Files.newBufferedReader(labelsFile)) {

            String dataLine;
            String labelLine;
            int rowIndex = 0;

            while ((dataLine = dataReader.readLine()) != null
                    && (labelLine = labelReader.readLine()) != null) {

                readings.add(parseRow(rowIndex, dataLine, labelLine));
                rowIndex++;
            }
        }

        return readings;
    }

    private ParsedReading parseRow(int rowIndex, String dataLine, String labelLine) {
        String waferId = "W-" + rowIndex;
        List<ParsedFeature> features = parseFeatures(dataLine);
        LabelInfo labelInfo = parseLabel(labelLine);

        return new ParsedReading(waferId, labelInfo.timestamp(), labelInfo.passFail(), features);
    }

    private List<ParsedFeature> parseFeatures(String dataLine) {
        String[] tokens = dataLine.trim().split("\\s+");
        List<ParsedFeature> features = new ArrayList<>(tokens.length);

        for (int i = 0; i < tokens.length; i++) {
            int featureIndex = i + 1; // 1-based: sensor_1..sensor_590
            String token = tokens[i];

            if (MISSING_TOKEN.equalsIgnoreCase(token)) {
                features.add(new ParsedFeature(featureIndex, null, true));
            } else {
                features.add(new ParsedFeature(featureIndex, Double.parseDouble(token), false));
            }
        }

        return features;
    }

    private LabelInfo parseLabel(String labelLine) {
        String[] parts = labelLine.trim().split("\\s+", 2);
        int rawLabel = Integer.parseInt(stripQuotes(parts[0]));
        PassFail passFail = (rawLabel == 1) ? PassFail.FAIL : PassFail.PASS;
        String timestampToken = stripQuotes(parts[1].trim());
        LocalDateTime timestamp = LocalDateTime.parse(timestampToken, LABEL_TIMESTAMP_FORMAT);

        return new LabelInfo(passFail, timestamp);
    }

    private String stripQuotes(String token) {
        String trimmed = token.trim();
        if (trimmed.length() >= 2 && trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
            return trimmed.substring(1, trimmed.length() - 1);
        }
        return trimmed;
    }

    private record LabelInfo(PassFail passFail, LocalDateTime timestamp) {
    }
}