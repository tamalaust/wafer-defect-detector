package com.iotml.wdd.ingestion.parser;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Runs against the real dataset in /data (project root), not a checked-in
 * fixture. Requires data/secom.data and data/secom_labels.data to be present
 * locally -- will fail on a machine/CI run that doesn't have them (data/ is
 * gitignored).
 */
class SecomFileParserTest {

    private final SecomFileParser parser = new SecomFileParser();
    private static final Path DATA_FILE = Path.of("data/secom.data");
    private static final Path LABELS_FILE = Path.of("data/secom_labels.data");

    @Test
    void parsesAllRowsFromRealDataset() throws IOException {
        List<ParsedReading> readings = parser.parse(DATA_FILE, LABELS_FILE);

        assertEquals(1567, readings.size());
    }

    @Test
    void everyRowHasASyntheticWaferIdAndALabel() throws IOException {
        List<ParsedReading> readings = parser.parse(DATA_FILE, LABELS_FILE);

        for (int i = 0; i < readings.size(); i++) {
            ParsedReading reading = readings.get(i);
            assertEquals("W-" + i, reading.waferId());
            assertTrue(reading.passFail() == PassFail.PASS || reading.passFail() == PassFail.FAIL);
        }
    }

    @Test
    void everyRowHasTheSameFeatureCount() throws IOException {
        List<ParsedReading> readings = parser.parse(DATA_FILE, LABELS_FILE);

        int expectedFeatureCount = readings.get(0).features().size();
        for (ParsedReading reading : readings) {
            assertEquals(expectedFeatureCount, reading.features().size());
        }
    }

    @Test
    void atLeastSomeFeaturesAreMissing() throws IOException {
        // SECOM is known to be sparse -- sanity-checks NaN detection actually
        // fires somewhere across a real 1567-row run, not just in a fixture.
        List<ParsedReading> readings = parser.parse(DATA_FILE, LABELS_FILE);

        boolean anyMissing = readings.stream()
                .flatMap(r -> r.features().stream())
                .anyMatch(ParsedFeature::missing);

        assertTrue(anyMissing);
    }

    @Test
    void presentValuesAreNeverNull() throws IOException {
        List<ParsedReading> readings = parser.parse(DATA_FILE, LABELS_FILE);

        boolean anyPresentValueIsNull = readings.stream()
                .flatMap(r -> r.features().stream())
                .filter(f -> !f.missing())
                .anyMatch(f -> f.value() == null);

        assertFalse(anyPresentValueIsNull);
    }
}