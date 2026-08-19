package com.iotml.wdd.ingestion.seed;

import com.iotml.wdd.ingestion.entity.SensorFeature;
import com.iotml.wdd.ingestion.entity.SensorReading;
import com.iotml.wdd.ingestion.parser.ParsedFeature;
import com.iotml.wdd.ingestion.parser.ParsedReading;

public class ParsedReadingMapper {

    public SensorReading toEntity(ParsedReading parsed) {
        SensorReading reading = new SensorReading(
                parsed.waferId(),
                parsed.timestamp(),
                parsed.passFail()
        );

        for (ParsedFeature feature : parsed.features()) {
            reading.addFeature(new SensorFeature(
                    feature.featureIndex(),
                    feature.value(),
                    feature.missing()
            ));
        }

        return reading;
    }
}