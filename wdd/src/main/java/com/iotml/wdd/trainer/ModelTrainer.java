package com.iotml.wdd.trainer;

import smile.classification.RandomForest;
import smile.data.DataFrame;
import smile.data.formula.Formula;

import java.util.List;

/**
 * Builds a Smile DataFrame from TrainingRows and fits a RandomForest.
 *
 * NOTE: Smile's exact API has shifted across versions (2.x vs 3.x vs later).
 * This uses the simplest, most stable overloads confirmed against Smile's
 * published source/docs: DataFrame.of(double[][], String...), merging in a
 * label column, Formula.lhs("label"), and the 2-arg RandomForest.fit(formula, df)
 * (uses Smile's default 500 trees). If your installed Smile version's method
 * signatures differ, this is the first place to check.
 */
public class ModelTrainer {

    public RandomForest train(List<TrainingRow> rows) {
        int n = rows.size();
        int featureCount = rows.get(0).features().length;

        double[][] x = new double[n][featureCount];
        int[][] label = new int[n][1];
        String[] columnNames = new String[featureCount];

        for (int i = 0; i < featureCount; i++) {
            columnNames[i] = "sensor_" + (i + 1);
        }

        for (int i = 0; i < n; i++) {
            x[i] = rows.get(i).features();
            label[i][0] = rows.get(i).label();
        }

        DataFrame featureFrame = DataFrame.of(x, columnNames);
        DataFrame labelFrame = DataFrame.of(label, "label");
        DataFrame trainingFrame = featureFrame.merge(labelFrame);

        Formula formula = Formula.lhs("label");

        return RandomForest.fit(formula, trainingFrame);
    }
}