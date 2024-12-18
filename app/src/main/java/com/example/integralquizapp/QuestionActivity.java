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
    private IntegralRule currentRule;
    private String nextIntegralLatex;
    private String[] nextOptions;
    private int correctOptionIndex;
    private List<IntegralRule> selectedRules;
    private double a, b, n;

    // Yeni eklenen sayaçlar
    private int totalCorrect = 0;
    private int totalWrong = 0;

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

        integralWebView.loadUrl("file:///android_asset/mathjax/index.html");

        loadSelectedRules();
        prepareNextQuestion();
    }

    private void saveHistory() {
        // Mevcut tarih ve zamanı al (basit bir örnek)
        String date = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault()).format(new java.util.Date());

        // History verisini SharedPreferences'da JSON olarak tutabilirsiniz
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String historyJson = prefs.getString("history_data", "[]");

        try {
            JSONArray historyArray = new JSONArray(historyJson);

            // Bu örnekte kural ismini tek bir seferde sabit atıyoruz
            // İsterseniz currentRule.getName() gibi bir metod varsa onu kullanabilirsiniz
            String ruleName = "Karışık Kurallar"; // veya currentRule.getName()

            JSONObject newRecord = new JSONObject();
            newRecord.put("ruleName", ruleName);
            newRecord.put("date", date);
            newRecord.put("correct", totalCorrect);
            newRecord.put("wrong", totalWrong);

            historyArray.put(newRecord);

            prefs.edit().putString("history_data", historyArray.toString()).apply();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
        // Geçmişe kaydetme işlemi
        saveHistory();

        // Uygulamayı kapat
        finishAffinity();
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
                    selectedRules.add(allRules.get(Integer.parseInt(index.trim())));
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
        currentRule = selectedRules.get(rand.nextInt(selectedRules.size()));
        a = rand.nextInt(9) + 1;
        b = rand.nextInt(9) + 1;
        n = rand.nextInt(9) + 1;

        String correctAnswer = currentRule.getSolutionLatex(a,b,n);
        nextOptions = new String[4];
        nextOptions[0] = correctAnswer;

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

        for (int i = 1; i < 4; i++) {
            nextOptions[i] = otherOptions.remove(0);
        }

        Collections.shuffle(Arrays.asList(nextOptions));
        correctOptionIndex = Arrays.asList(nextOptions).indexOf(correctAnswer);

        nextIntegralLatex = currentRule.getIntegralLatex(a,b,n);

        if (isMathJaxReady) {
            displayCurrentQuestion();
        }
    }

    private void displayCurrentQuestion() {
        if (!isMathJaxReady) return;
        updateLatex("question_math", nextIntegralLatex);
        for (int i = 0; i < 4; i++) {
            updateLatex("option"+i+"_math", nextOptions[i]);
        }

        // Tüm butonların rengini varsayılana (mor) döndür
        for (int i = 0; i < 4; i++) {
            String resetJs = "document.getElementById('option" + i + "_math').style.backgroundColor = '#673ab7';";
            integralWebView.evaluateJavascript(resetJs, null);
        }
    }


    private void updateLatex(String elementId, String latex) {
        String escapedLatex = latex.replace("\\", "\\\\");
        String js = "setLatex('" + elementId + "', `" + escapedLatex + "`);";
        integralWebView.post(() -> integralWebView.evaluateJavascript(js, null));
    }

    private void checkAnswer(int selectedIndex) {
        boolean isCorrect = (selectedIndex == correctOptionIndex);

        // Doğru/Yanlış sayacı
        if (isCorrect) {
            totalCorrect++;
        } else {
            totalWrong++;
        }

        // Seçilen butonu renklendir
        String color = isCorrect ? "green" : "red";
        String js = "document.getElementById('option" + selectedIndex + "_math').style.backgroundColor = '" + color + "';";
        integralWebView.evaluateJavascript(js, null);

        // 1 saniye sonra yeni soruya geç
        new Handler().postDelayed(this::prepareNextQuestion, 1000);
    }

    public class AndroidInterface {
        @JavascriptInterface
        public void selectOption(int index) {
            runOnUiThread(() -> checkAnswer(index));
        }
    }
}
