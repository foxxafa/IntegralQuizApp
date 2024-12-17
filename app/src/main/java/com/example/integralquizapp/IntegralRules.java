package com.example.integralquizapp;

import java.util.ArrayList;
import java.util.List;

public class IntegralRules {

    public static List<IntegralRule> getAllRules() {
        List<IntegralRule> rules = new ArrayList<>();

        rules.add(new IntegralRule(
                "Kural 1",
                "\\int ({a}x+{b})^{n} dx",
                "\\frac{({a}x+{b})^{({n+1})}}{({n+1}\\cdot {a})} + C",
                true));

        rules.add(new IntegralRule(
                "Kural 2",
                "\\int \\frac{dx}{{a}x+{b}}",
                "\\frac{1}{{a}}\\ln|{a}x+{b}| + C",
                false));

        rules.add(new IntegralRule(
                "Kural 3",
                "\\int {a}^{({n}x+{b})} dx",
                "\\frac{{a}^{({n}x+{b})}}{{n}\\cdot \\ln({a})} + C",
                true));

        rules.add(new IntegralRule(
                "Kural 4",
                "\\int \\sin({a}x+{b}) dx",
                "-\\frac{\\cos({a}x+{b})}{{a}} + C",
                false));

        return rules;
    }
}
