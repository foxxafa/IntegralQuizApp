package com.example.integralquizapp;

import java.util.Random;

public class WeightedSamplingLearningSystem {
    private int nRules;
    private double[] a;
    private double[] b;
    private int[] selectionCounts;
    private double minSelections;
    private double minWeight;
    private double epsilon;
    private Random random;

    public WeightedSamplingLearningSystem(int nRules, double minSelections, double minWeight, double epsilon) {
        this.nRules = nRules;
        this.a = new double[nRules];
        this.b = new double[nRules];
        this.selectionCounts = new int[nRules];
        this.minSelections = minSelections;
        this.minWeight = minWeight;
        this.epsilon = epsilon;
        this.random = new Random();
    }

    public double[] getWeights() {
        double[] successRates = getSuccessRates();
        double[] weights = new double[nRules];

        for (int i = 0; i < nRules; i++) {
            weights[i] = Math.max(minWeight, 1 - successRates[i]);
        }

        return weights;
    }

    public int selectRule() {
        int[] underSelected = new int[nRules];
        int count = 0;

        for (int i = 0; i < nRules; i++) {
            if (selectionCounts[i] < minSelections) {
                underSelected[count++] = i;
            }
        }

        if (count > 0) {
            return underSelected[random.nextInt(count)];
        } else {
            double[] weights = getWeights();
            return selectWithProbability(weights);
        }
    }

    private int selectWithProbability(double[] weights) {
        double totalWeight = 0;
        for (double weight : weights) {
            totalWeight += weight;
        }

        double rand = random.nextDouble() * totalWeight;
        for (int i = 0; i < weights.length; i++) {
            rand -= weights[i];
            if (rand <= 0) {
                selectionCounts[i]++;
                return i;
            }
        }
        return -1;
    }

    public void update(int chosenRule, int reward) {
        selectionCounts[chosenRule]++;
        if (reward == 1) {
            b[chosenRule]++;
        } else {
            a[chosenRule]++;
        }
    }

    public double[] getSuccessRates() {
        double[] rates = new double[nRules];
        for (int i = 0; i < nRules; i++) {
            if (a[i] + b[i] > 0) {
                rates[i] = b[i] / (a[i] + b[i]);
            } else {
                rates[i] = 0;
            }
        }
        return rates;
    }

    public int[] getSelectionCounts() {
        return selectionCounts;
    }

    public double[] getA() {
        return a;
    }

    public double[] getB() {
        return b;
    }

    public void setA(double[] a) {
        this.a = a;
    }

    public void setB(double[] b) {
        this.b = b;
    }

    public void setSelectionCounts(int[] selectionCounts) {
        this.selectionCounts = selectionCounts;
    }
}
