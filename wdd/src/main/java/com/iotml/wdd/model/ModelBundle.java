package com.iotml.wdd.model;

import smile.classification.RandomForest;

import java.io.Serializable;
import java.util.Map;

/**
 * What gets persisted after training: the model itself plus the exact
 * per-feature means used for mean imputation during training.
 *
 * Predict-time imputation must reuse these same means (not recompute fresh
 * from the DB) to avoid train/serve skew - the model learned patterns based
 * on this specific imputation, not whatever the DB's current averages
 * happen to be by the time a prediction is requested.
 */
public record ModelBundle(
        RandomForest model,
        Map<Integer, Double> featureMeans,
        int featureCount
) implements Serializable {
}