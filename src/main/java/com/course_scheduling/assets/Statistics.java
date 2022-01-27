package com.course_scheduling.assets;

import java.util.List;

/**
 *
 * @author @artikandri
 */
public class Statistics {

    /**
     * Calculate the mean of a list of numbers
     *
     * @return the mean as decimal number
     */
    public double getMean(List data) {
        double mean = 0.0;
        for (int i = 0; i < data.size(); i++) {
            mean += Double.parseDouble(data.get(i).toString());
        }
        mean /= data.size();
        return mean;
    }

    /**
     * Calculate the variance of a list of given numbers
     *
     * @return the variance as decimal number
     */
    public double getVariance(List data) {
        double variance = 0;
        double mean = getMean(data);
        for (int i = 0; i < data.size(); i++) {
            variance += Math.pow(Double.parseDouble(data.get(i).toString()) - mean, 2);
        }
        variance /= data.size();

        return variance;
    }

    /**
     * Calculate the standard deviation of a list of given numbers
     *
     * @return the standard deviation as decimal number
     */
    public double getStandardDeviation(List data) {
        double variance = getVariance(data);
        double std = Math.sqrt(variance);
        return std;
    }
}
