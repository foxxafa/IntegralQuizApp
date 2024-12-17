package com.example.integralquizapp;

public class IntegralRule {
    private String name;
    private String integralTemplate;
    private String solutionTemplate;
    private boolean usesN; // Bu kural n parametresi kullanır mı?

    public IntegralRule(String name, String integralTemplate, String solutionTemplate, boolean usesN) {
        this.name = name;
        this.integralTemplate = integralTemplate;
        this.solutionTemplate = solutionTemplate;
        this.usesN = usesN;
    }

    public String getName() {
        return name;
    }

    public String getIntegralLatex(double a, double b, double n) {
        int ai = (int) a;
        int bi = (int) b;
        int ni = (int) n;

        String result = integralTemplate;
        result = result.replace("{a}", String.valueOf(ai));
        result = result.replace("{b}", String.valueOf(bi));
        if (usesN) {
            result = result.replace("{n}", String.valueOf(ni));
            result = result.replace("{n+1}", String.valueOf(ni+1));
        }
        return result;
    }

    public String getSolutionLatex(double a, double b, double n) {
        int ai = (int) a;
        int bi = (int) b;
        int ni = (int) n;

        String result = solutionTemplate;
        result = result.replace("{a}", String.valueOf(ai));
        result = result.replace("{b}", String.valueOf(bi));
        if (usesN) {
            result = result.replace("{n}", String.valueOf(ni));
            result = result.replace("{n+1}", String.valueOf(ni+1));
        }
        return result;
    }
}
