package com.course_scheduling.assets;

import java.util.List;

public class Statistics {

    public double getMean(List data) {
        double mean = 0.0;
        for (int i = 0; i < data.size(); i++) {
            mean += Double.parseDouble(data.get(i).toString());
        }
        mean /= data.size();
        return mean;
    }

    public double getVariance(List data) {
        double variance = 0;
        double mean = getMean(data);
        for (int i = 0; i < data.size(); i++) {
            variance += Math.pow(Double.parseDouble(data.get(i).toString()) - mean, 2);
        }
        variance /= data.size();

        return variance;
    }

    public double getStandardDeviation(List data) {
        double variance = getVariance(data);
        double std = Math.sqrt(variance);
        return std;
    }
}
