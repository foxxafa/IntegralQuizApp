package com.example.integralquizapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private WeightedSamplingLearningSystem learningSystem;
    private WebView integralWebView;
    private WebView option1WebView, option2WebView, option3WebView, option4WebView;

    private List<IntegralRule> allRules;
    private IntegralRule currentRule;
    private int correctOptionIndex;
    private double a, b, n; // parametreler
    private List<String> currentOptions; // Şıkları tutmak için

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        integralWebView = findViewById(R.id.integralWebView);
        option1WebView = findViewById(R.id.option1WebView);
        option2WebView = findViewById(R.id.option2WebView);
        option3WebView = findViewById(R.id.option3WebView);
        option4WebView = findViewById(R.id.option4WebView);

        setupWebView(integralWebView);
        setupWebView(option1WebView);
        setupWebView(option2WebView);
        setupWebView(option3WebView);
        setupWebView(option4WebView);

        learningSystem = new WeightedSamplingLearningSystem(4, 5, 0.1, 0.1);
        loadModelData(learningSystem);

        allRules = getAllRules();

        addOptionClickListener(option1WebView, 0);
        addOptionClickListener(option2WebView, 1);
        addOptionClickListener(option3WebView, 2);
        addOptionClickListener(option4WebView, 3);

        askQuestion();
    }

    private void askQuestion() {
        // Şıkların arka planını sıfırla
        resetOptionBackgrounds();

        int chosenIndex = learningSystem.selectRule();
        currentRule = allRules.get(chosenIndex);

        // Rastgele parametre atama (1 ile 9 arasında)
        Random rand = new Random();
        int ai = rand.nextInt(9) + 1; // 1-9
        int bi = rand.nextInt(9) + 1;
        int ni = rand.nextInt(9) + 1;

        a = ai;
        b = bi;
        n = ni;

        // Doğru cevap
        String correctAnswer = currentRule.getSolutionLatex(a, b, n);

        // Yanlış cevaplar: diğer kurallardan
        List<IntegralRule> otherRules = new ArrayList<>(allRules);
        otherRules.remove(currentRule);
        Collections.shuffle(otherRules);
        List<String> wrongAnswers = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            wrongAnswers.add(otherRules.get(i).getSolutionLatex(a, b, n));
        }

        // Şıkları karıştır
        currentOptions = new ArrayList<>();
        currentOptions.add(correctAnswer);
        currentOptions.addAll(wrongAnswers);
        Collections.shuffle(currentOptions);

        correctOptionIndex = currentOptions.indexOf(correctAnswer);

        // Integral sorusunu göster
        String integralLatex = currentRule.getIntegralLatex(a, b, n);
        loadMathJax(integralWebView, integralLatex);

        // Şıkları göster
        loadMathJaxForOption(option1WebView, currentOptions.get(0));
        loadMathJaxForOption(option2WebView, currentOptions.get(1));
        loadMathJaxForOption(option3WebView, currentOptions.get(2));
        loadMathJaxForOption(option4WebView, currentOptions.get(3));

        // Başarı oranlarını konsola yaz
        printStatsToConsole();
    }

    private void resetOptionBackgrounds() {
        WebView[] options = {option1WebView, option2WebView, option3WebView, option4WebView};
        for (WebView option : options) {
            option.setBackgroundColor(Color.TRANSPARENT); // Arka planı sıfırla
        }
    }


    private void loadMathJax(WebView webView, String latex) {
        String html = "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset=\"utf-8\"/>" +
                "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1.0\"/>" +
                "<script async src=\"https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-mml-chtml.js\"></script>" +
                "</head>" +
                "<body style='margin: 0; padding: 0; text-align: center; background-color: transparent;'>" +
                "<div id='math'>$$" + latex + "$$</div>" +
                "</body>" +
                "</html>";
        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
    }



    private void loadMathJaxForOption(WebView webView, String latex) {
        // Şıklar için benzer tasarım, hafif gri arka plan
        String html = "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<meta charset=\"utf-8\"/>" +
                "<meta name=\"viewport\" content=\"width=device-width,initial-scale=1.0\"/>" +
                "<script async src=\"https://cdn.jsdelivr.net/npm/mathjax@3/es5/tex-mml-chtml.js\"></script>" +
                "</head>" +
                "<body style='margin: 0; padding: 0; text-align: center; background-color: transparent;'>" +
                "<div id='math'>$$" + latex + "$$</div>" +
                "</body>" +
                "</html>";
        webView.loadDataWithBaseURL(null, html, "text/html", "UTF-8", null);
    }



    private void checkAnswer(int selectedOptionIndex) {
        int reward = (selectedOptionIndex == correctOptionIndex) ? 1 : 0;

        // Doğru veya yanlış duruma göre şıkların rengini değiştir
        if (selectedOptionIndex == correctOptionIndex) {
            highlightOption(selectedOptionIndex, true); // Doğru şık yeşil
        } else {
            highlightOption(selectedOptionIndex, false); // Yanlış şık kırmızı
            highlightOption(correctOptionIndex, true); // Doğru şık da gösterilsin
        }

        // Skor güncelle ve modeli kaydet
        learningSystem.update(allRules.indexOf(currentRule), reward);
        saveModelData(learningSystem);

        // 1 saniye sonra yeni soru yüklensin
        new android.os.Handler().postDelayed(this::askQuestion, 1000);
    }

    private void highlightOption(int optionIndex, boolean isCorrect) {
        // Şık WebView'lerini tutan bir liste
        WebView[] options = {option1WebView, option2WebView, option3WebView, option4WebView};

        // Hangi renk? Yeşil (doğru) veya kırmızı (yanlış)
        int color = isCorrect ? Color.parseColor("#A5D6A7") : Color.parseColor("#EF9A9A"); // Yeşil veya Kırmızı

        // İlgili WebView'i renklendir
        options[optionIndex].setBackgroundColor(color);
    }



    private void printStatsToConsole() {
        double[] successRates = learningSystem.getSuccessRates();
        int[] selectionCounts = learningSystem.getSelectionCounts();
        double[] weights = learningSystem.getWeights();

        int totalQuestions = 0;
        for (int count : selectionCounts) {
            totalQuestions += count;
        }

        for (int i = 0; i < allRules.size(); i++) {
            double successRate = successRates[i] * 100;
            double selectionRate = (totalQuestions > 0) ? (selectionCounts[i] / (double) totalQuestions) * 100 : 0;
            double weight = weights[i] * 100;

            Log.d("IntegralStats", allRules.get(i).getName() + " - Soruldu: " + selectionCounts[i]
                    + " - Başarı Oranı: " + String.format("%.2f", successRate) + "%"
                    + " - Oranı: " + String.format("%.2f", selectionRate) + "%"
                    + " - Ağırlık: " + String.format("%.2f", weight) + "%");
        }
    }

    private void saveModelData(WeightedSamplingLearningSystem system) {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        double[] A = system.getA();
        double[] B = system.getB();
        int[] selectionCounts = system.getSelectionCounts();

        for (int i = 0; i < A.length; i++) {
            editor.putFloat("a_" + i, (float) A[i]);
            editor.putFloat("b_" + i, (float) B[i]);
            editor.putInt("s_" + i, selectionCounts[i]);
        }

        editor.apply();
    }

    private void loadModelData(WeightedSamplingLearningSystem system) {
        SharedPreferences prefs = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        double[] A = new double[4];
        double[] B = new double[4];
        int[] selectionCounts = new int[4];

        for (int i = 0; i < 4; i++) {
            A[i] = prefs.getFloat("a_" + i, 0f);
            B[i] = prefs.getFloat("b_" + i, 0f);
            selectionCounts[i] = prefs.getInt("s_" + i, 0);
        }

        system.setA(A);
        system.setB(B);
        system.setSelectionCounts(selectionCounts);
    }

    private List<IntegralRule> getAllRules() {
        List<IntegralRule> rules = new ArrayList<>();
        rules.add(new IntegralRule(
                "Kural 1",
                "\\int ({a}x+{b})^{n} dx",
                "\\frac{({a}x+{b})^{n+1}}{({n+1}){a}} + C",
                true));

        rules.add(new IntegralRule(
                "Kural 2",
                "\\int \\frac{dx}{{a}x+{b}}",
                "\\frac{1}{{a}} \\ln|{a}x+{b}| + C",
                false));

        rules.add(new IntegralRule(
                "Kural 3",
                "\\int {a}^{({n}x+{b})} dx",
                "\\frac{{a}^{({n}x+{b})}}{{n}\\ln {a}} + C",
                true));

        rules.add(new IntegralRule(
                "Kural 4",
                "\\int \\sin({a}x+{b}) dx",
                "-\\frac{\\cos({a}x+{b})}{{a}} + C",
                false));

        return rules;
    }

    private void setupWebView(WebView webView) {
        webView.setBackgroundColor(0x00000000); // Fully transparent background
        webView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null);
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
    }



    private void addOptionClickListener(WebView webView, final int index) {
        webView.setOnTouchListener((View v, MotionEvent event) -> {
            if (event.getAction() == MotionEvent.ACTION_UP) {
                checkAnswer(index);
            }
            return false;
        });
    }
}
