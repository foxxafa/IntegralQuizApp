package com.example.integralquizapp;

import java.util.ArrayList;
import java.util.List;

public class IntegralRules {

    public static List<IntegralRule> getAllRules() {
        List<IntegralRule> rules = new ArrayList<>();

        rules.add(new IntegralRule("Kural 1", "\\int ({a}x+{b})^{n} dx", "\\frac{({a}x+{b})^{({n+1})}}{({n+1}\\cdot {a})} + C", true));
        rules.add(new IntegralRule("Kural 2", "\\int \\frac{dx}{{a}x+{b}}", "\\frac{1}{{a}}\\ln|{a}x+{b}| + C", false));
        rules.add(new IntegralRule("Kural 3", "\\int {a}^{({n}x+{b})} dx", "\\frac{{a}^{({n}x+{b})}}{{n}\\cdot \\ln({a})} + C", true));
        rules.add(new IntegralRule("Kural 4", "\\int \\sin({a}x+{b}) dx", "-\\frac{\\cos({a}x+{b})}{{a}} + C", false));
        rules.add(new IntegralRule("Kural 5", "\\int \\cos({a}x+{b}) dx", "\\frac{\\sin({a}x+{b})}{{a}} + C", false));
        rules.add(new IntegralRule("Kural 6", "\\int \\sec^2({a}x+{b}) dx", "\\frac{\\tan({a}x+{b})}{{a}} + C", false));
        rules.add(new IntegralRule("Kural 7", "\\int \\csc^2({a}x+{b}) dx", "-\\frac{\\cot({a}x+{b})}{{a}} + C", false));
        rules.add(new IntegralRule("Kural 8", "\\int \\tan({a}x+{b}) dx", "-\\frac{\\ln|\\cos({a}x+{b})|}{{a}} + C", false));
        rules.add(new IntegralRule("Kural 9", "\\int \\frac{dx}{1+({a}x+{b})^2}", "\\frac{1}{{a}}\\arctan({a}x+{b}) + C", false));
        rules.add(new IntegralRule("Kural 10", "\\int \\cot({a}x+{b}) dx", "\\frac{\\ln|\\sin({a}x+{b})|}{{a}} + C", false));
        rules.add(new IntegralRule("Kural 11", "\\int \\sec({a}x+{b}) dx", "\\frac{\\ln|\\sec({a}x+{b})+\\tan({a}x+{b})|}{{a}} + C", false));
        rules.add(new IntegralRule("Kural 12", "\\int \\frac{dx}{\\sqrt{1-({a}x+{b})^2}}", "\\frac{1}{{a}}\\arcsin({a}x+{b}) + C", false));
        rules.add(new IntegralRule("Kural 13", "\\int \\csc({a}x+{b}) dx", "-\\frac{\\ln|\\csc({a}x+{b})+\\cot({a}x+{b})|}{{a}} + C", false));
        rules.add(new IntegralRule("Kural 14", "\\int \\frac{dx}{\\sqrt{1+({a}x+{b})^2}}", "\\ln(x+\\sqrt{1+({a}x+{b})^2}) + C", false));
        rules.add(new IntegralRule("Kural 15", "\\int \\frac{dx}{\\sqrt{({a}x+{b})^2-1}}", "\\ln(x+\\sqrt{({a}x+{b})^2-1}) + C", false));

        return rules;
    }
}
