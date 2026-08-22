package com.iotml.wdd.predict;

import com.iotml.wdd.ingestion.entity.SensorFeature;
import com.iotml.wdd.ingestion.entity.SensorReading;

import java.util.Map;

/**
 * Same imputation logic as TrainingDataLoader's vector-building step, but
 * working off an already-loaded entity's feature list instead of a JDBC
 * ResultSet -- and critically, using the persisted training-time means
 * (passed in), not freshly recomputed ones. See ModelBundle's javadoc.
 */
public class FeatureVectorBuilder {

    public double[] build(SensorReading reading, Map<Integer, Double> featureMeans, int featureCount) {
        double[] vector = new double[featureCount];

        for (int i = 0; i < featureCount; i++) {
            vector[i] = featureMeans.getOrDefault(i + 1, 0.0);
        }

        for (SensorFeature feature : reading.getFeatures()) {
            boolean missing = "true".equals(feature.getIsMissing());
            int index = feature.getFeatureIndex();

            if (!missing && index >= 1 && index <= featureCount) {
                vector[index - 1] = feature.getValue();
            }
        }

        return vector;
    }
}