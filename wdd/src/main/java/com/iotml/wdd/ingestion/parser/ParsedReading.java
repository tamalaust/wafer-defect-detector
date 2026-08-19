package com.iotml.wdd.ingestion.parser;

import java.time.LocalDateTime;
import java.util.List;

/**
 * One fully-parsed SECOM row, built from a matched pair of lines
 * (one from secom.data, one from secom_labels.data).
 * waferId is synthetic — derived from row position, since the source
 * files carry no natural identifier.
 */
public record ParsedReading(
        String waferId,
        LocalDateTime timestamp,
        PassFail passFail,
        List<ParsedFeature> features
) {
}
