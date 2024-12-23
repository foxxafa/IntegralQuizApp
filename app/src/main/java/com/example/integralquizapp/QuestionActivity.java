package com.example.integralquizapp;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class QuestionActivity extends AppCompatActivity {
    private WebView integralWebView;
    private boolean isMathJaxReady = false;
    private List<IntegralRule> selectedRules;
    private double a, b, n;

    private WeightedSamplingLearningSystem wss;
    private int chosenRuleIndex; // seçilen kuralın indeksi
    private IntegralRule currentRule;

    private String nextIntegralLatex;
    private String[] nextOptions;
    private int correctOptionIndex;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        integralWebView = findViewById(R.id.integralWebView);
        WebSettings settings = integralWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        integralWebView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        integralWebView.addJavascriptInterface(new AndroidInterface(), "AndroidInterface");

        integralWebView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                new Handler().postDelayed(() -> {
                    isMathJaxReady = true;
                    displayCurrentQuestion();
                }, 2000);
            }
        });

        // "index.html" MathJax sayfası
        integralWebView.loadUrl("file:///android_asset/mathjax/index.html");

        // Seçilmiş kuralları yükle
        loadSelectedRules();

        // WeightedSamplingLearningSystem başlat
        wss = new WeightedSamplingLearningSystem(selectedRules.size(), 2, 0.1, 0.01);
        prepareNextQuestion();
    }

    private void loadSelectedRules() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String selectedRuleIndices = prefs.getString("selected_rules", "");

        if (selectedRuleIndices.isEmpty()) {
            Toast.makeText(this, "Kural seçimi yapılmamış! Lütfen kural seçin.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, RuleSelectionActivity.class));
            finish();
            return;
        }

        selectedRules = new ArrayList<>();
        List<IntegralRule> allRules = IntegralRules.getAllRules();
        for (String index : selectedRuleIndices.split(",")) {
            if (!index.isEmpty()) {
                try {
                    selectedRules.add(allRules.get(Integer.parseInt(index.trim()) ));
                } catch (Exception e) {
                    Log.e("QuestionActivity", "Kural eklenirken hata oluştu: " + e.getMessage());
                }
            }
        }

        if (selectedRules.size() < 4) {
            Toast.makeText(this, "Lütfen en az 4 kural seçiniz!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, RuleSelectionActivity.class));
            finish();
        }
    }

    private void prepareNextQuestion() {
        Random rand = new Random();
        // WeightedSamplingLearningSystem ile kural seç
        chosenRuleIndex = wss.selectRule();
        currentRule = selectedRules.get(chosenRuleIndex);

        // Rastgele parametreler a, b, n
        a = rand.nextInt(9) + 1;  // 1..9
        b = rand.nextInt(9) + 1;  // 1..9
        n = rand.nextInt(9) + 1;  // 1..9

        // Doğru cevap
        String correctAnswer = currentRule.getSolutionLatex(a, b, n);

        // 4 şık
        nextOptions = new String[4];
        nextOptions[0] = correctAnswer;

        // Diğer 3 yanlış seçenek
        List<String> otherOptions = new ArrayList<>();
        int attemptCount = 0;
        while (otherOptions.size() < 3 && attemptCount < 50) {
            IntegralRule randomRule = selectedRules.get(rand.nextInt(selectedRules.size()));
            String option = randomRule.getSolutionLatex(a, b, n);
            if (!option.equals(correctAnswer) && !otherOptions.contains(option)) {
                otherOptions.add(option);
            }
            attemptCount++;
        }

        if (otherOptions.size() < 3) {
            Log.e("QuestionActivity", "Yeterli sayıda seçenek oluşturulamadı!");
            Toast.makeText(this, "Yeterli kural seçilmedi! Lütfen daha fazla kural seçin.", Toast.LENGTH_LONG).show();
            startActivity(new Intent(this, RuleSelectionActivity.class));
            finish();
            return;
        }

        // Diğer 3 şık ekle, karıştır
        for (int i = 1; i < 4; i++) {
            nextOptions[i] = otherOptions.remove(0);
        }
        Collections.shuffle(Arrays.asList(nextOptions));

        correctOptionIndex = Arrays.asList(nextOptions).indexOf(correctAnswer);
        nextIntegralLatex = currentRule.getIntegralLatex(a, b, n);

        if (isMathJaxReady) {
            displayCurrentQuestion();
        }
    }

    private void displayCurrentQuestion() {
        if (!isMathJaxReady) return;

        // Soru
        updateLatex("question_math", nextIntegralLatex);

        // Şıklar
        for (int i = 0; i < 4; i++) {
            updateLatex("option" + i + "_math", nextOptions[i]);
        }

        // Renkleri resetle
        for (int i = 0; i < 4; i++) {
            String resetJs = "document.getElementById('option" + i + "_math').style.backgroundColor = '#673ab7';";
            integralWebView.evaluateJavascript(resetJs, null);
        }
    }

    private void updateLatex(String elementId, String latex) {
        // Ters slash'ları kaçırma
        String escapedLatex = latex.replace("\\", "\\\\");
        String js = "setLatex('" + elementId + "', `" + escapedLatex + "`);";
        integralWebView.post(() -> integralWebView.evaluateJavascript(js, null));
    }

    private void checkAnswer(int selectedIndex) {
        boolean isCorrect = (selectedIndex == correctOptionIndex);
        wss.update(chosenRuleIndex, isCorrect ? 1 : 0);

        // Seçilen butonu doğru/yanlış rengine boyama
        String color = isCorrect ? "green" : "red";
        String js = "document.getElementById('option" + selectedIndex + "_math').style.backgroundColor = '" + color + "';";
        integralWebView.evaluateJavascript(js, null);

        // 1 saniye sonra yeni soruya geç
        new Handler().postDelayed(this::prepareNextQuestion, 1000);
    }

    private void saveHistory() {
        String date = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(new java.util.Date());
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String historyJson = prefs.getString("history_data", "[]");

        try {
            JSONArray historyArray = new JSONArray(historyJson);

            JSONObject newRecord = new JSONObject();
            newRecord.put("date", date);

            // WeightedSamplingLearningSystem'den a[], b[] (yanlış/doğru sayıları) al
            double[] aArray = wss.getA();
            double[] bArray = wss.getB();

            // Her kural için JSON objesi
            JSONArray rulesArray = new JSONArray();
            for (int i = 0; i < selectedRules.size(); i++) {
                IntegralRule rule = selectedRules.get(i);

                JSONObject rObj = new JSONObject();
                // Kural ismi (ör. "Kural 4")
                rObj.put("ruleName", rule.getName());
                // LaTeX formülü ("\\int (ax+b)^n dx")
                rObj.put("formula", rule.getFormula());

                rObj.put("correct", (int) bArray[i]);
                rObj.put("wrong", (int) aArray[i]);

                rulesArray.put(rObj);
            }

            newRecord.put("rules", rulesArray);
            historyArray.put(newRecord);

            prefs.edit().putString("history_data", historyArray.toString()).apply();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        // Geçmişe kaydet
        saveHistory();

        // Ana ekrana dön
        Intent intent = new Intent(QuestionActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    public class AndroidInterface {
        @JavascriptInterface
        public void selectOption(int index) {
            runOnUiThread(() -> checkAnswer(index));
        }
    }
}
