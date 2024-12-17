package com.example.integralquizapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class QuestionActivity extends AppCompatActivity {

    private WebView integralWebView;
    private WebView[] optionWebViews = new WebView[4];
    private List<IntegralRule> selectedRules;
    private IntegralRule currentRule;
    private double a, b, n;
    private int correctOptionIndex;

    private Handler handler = new Handler();
    private String nextIntegralLatex;
    private String[] nextOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);

        integralWebView = findViewById(R.id.integralWebView);
        optionWebViews[0] = findViewById(R.id.option1WebView);
        optionWebViews[1] = findViewById(R.id.option2WebView);
        optionWebViews[2] = findViewById(R.id.option3WebView);
        optionWebViews[3] = findViewById(R.id.option4WebView);

        for (WebView wv : optionWebViews) setupWebView(wv);
        setupWebView(integralWebView);

        loadSelectedRules();
        prepareNextQuestion();
        displayCurrentQuestion();

        // WebView'lere tıklama olayını ekle
        for (int i = 0; i < optionWebViews.length; i++) {
            final int index = i;
            optionWebViews[i].setOnClickListener(v -> checkAnswer(index));
        }
    }


    private void setupWebView(WebView webView) {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);

        webView.setBackgroundColor(Color.TRANSPARENT);
        webView.setWebViewClient(new WebViewClient());

        webView.addJavascriptInterface(new Object() {
            @android.webkit.JavascriptInterface
            public void onOptionClick(int index) {
                runOnUiThread(() -> checkAnswer(index));
            }
        }, "AndroidInterface");

        if (webView.getUrl() == null) {
            webView.loadUrl("file:///android_asset/mathjax/index.html");
        }
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

        if (selectedRules.size() < 2) {
            Toast.makeText(this, "En az 2 kural seçmeniz gerekiyor!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, RuleSelectionActivity.class));
            finish();
        }
    }


    private void checkAnswer(int selectedIndex) {
        Log.d("QuestionActivity", "Tıklanan Seçenek: " + selectedIndex);

        for (WebView wv : optionWebViews) {
            wv.setBackgroundColor(Color.TRANSPARENT);
        }

        if (selectedIndex == correctOptionIndex) {
            optionWebViews[selectedIndex].setBackgroundColor(Color.GREEN);
        } else {
            optionWebViews[selectedIndex].setBackgroundColor(Color.RED);
        }

        handler.postDelayed(() -> {
            for (WebView wv : optionWebViews) {
                wv.setBackgroundColor(Color.TRANSPARENT);
            }
            prepareNextQuestion();
            displayCurrentQuestion();
        }, 1000);
    }


    private void prepareNextQuestion() {
        if (selectedRules == null || selectedRules.isEmpty()) {
            Log.e("QuestionActivity", "selectedRules list is empty!");
            return;
        }

        Random rand = new Random();
        currentRule = selectedRules.get(rand.nextInt(selectedRules.size()));

        a = rand.nextInt(9) + 1;
        b = rand.nextInt(9) + 1;
        n = rand.nextInt(9) + 1;

        // Doğru cevabı oluştur
        String correctAnswer = currentRule.getSolutionLatex(a, b, n);
        nextOptions = new String[4];
        nextOptions[0] = correctAnswer;

        // Benzersiz seçenekler için döngü kontrolü
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

        // Seçenekleri karıştır ve yerleştir
        for (int i = 1; i < 4; i++) {
            nextOptions[i] = otherOptions.remove(0);
        }

        Collections.shuffle(Arrays.asList(nextOptions));
        correctOptionIndex = Arrays.asList(nextOptions).indexOf(correctAnswer);

        nextIntegralLatex = currentRule.getIntegralLatex(a, b, n);
    }




    private void displayCurrentQuestion() {
        updateLatex(integralWebView, nextIntegralLatex, -1); // Soru için index -1 kullanıldı.
        for (int i = 0; i < 4; i++) {
            updateLatex(optionWebViews[i], nextOptions[i], i);
        }
    }


    private void updateLatex(WebView webView, String latex, int index) {
        String js = "setLatex(`" + latex.replace("\\", "\\\\") + "`, " + index + ");";
        webView.post(() -> webView.evaluateJavascript(js, null));
    }






}
