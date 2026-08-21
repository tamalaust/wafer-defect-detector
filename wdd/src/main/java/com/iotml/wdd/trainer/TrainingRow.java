package com.iotml.wdd.trainer;

/**
 * label: 0 = PASS, 1 = FAIL (matches the raw SECOM convention used elsewhere
 * in the project: -1 -> PASS -> 0, 1 -> FAIL -> 1).
 */
public record TrainingRow(String waferId, int label, double[] features) {
}