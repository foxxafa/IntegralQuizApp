package com.example.integralquizapp;

import androidx.appcompat.app.AppCompatActivity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    private WebView integralWebView;
    private WebView[] optionWebViews = new WebView[4];
    private List<IntegralRule> allRules;
    private IntegralRule currentRule;
    private double a, b, n;
    private List<String> currentOptions;
    private int correctOptionIndex;

    private Handler handler = new Handler();
    private String nextIntegralLatex;
    private String[] nextOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        integralWebView = findViewById(R.id.integralWebView);
        optionWebViews[0] = findViewById(R.id.option1WebView);
        optionWebViews[1] = findViewById(R.id.option2WebView);
        optionWebViews[2] = findViewById(R.id.option3WebView);
        optionWebViews[3] = findViewById(R.id.option4WebView);

        for (WebView wv : optionWebViews) setupWebView(wv);
        setupWebView(integralWebView);

        allRules = IntegralRules.getAllRules();
        prepareNextQuestion();
        displayCurrentQuestion();

        for (int i = 0; i < optionWebViews.length; i++) {
            final int index = i;
            optionWebViews[i].setOnTouchListener((v, event) -> {
                checkAnswer(index);
                return false;
            });
        }
    }

    private void setupWebView(WebView webView) {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true); // DOM Storage'ı aktif et
        webView.setLayerType(WebView.LAYER_TYPE_SOFTWARE, null); // Donanım katmanı sorunlarını önler
        webView.setWebViewClient(new WebViewClient()); // URL yüklenmesini garanti eder
        webView.loadUrl("file:///android_asset/mathjax/index.html");
    }


    private void prepareNextQuestion() {
        Random rand = new Random();
        int ruleIndex = rand.nextInt(allRules.size());
        currentRule = allRules.get(ruleIndex);

        a = rand.nextInt(9) + 1;
        b = rand.nextInt(9) + 1;
        n = rand.nextInt(9) + 1;

        String correctAnswer = currentRule.getSolutionLatex(a, b, n);
        List<IntegralRule> otherRules = new ArrayList<>(allRules);
        otherRules.remove(currentRule);
        Collections.shuffle(otherRules);

        nextOptions = new String[4];
        nextOptions[0] = correctAnswer;
        for (int i = 1; i < 4; i++) {
            nextOptions[i] = otherRules.get(i - 1).getSolutionLatex(a, b, n);
        }

        // Modifiye edilebilir listeye çevirip shuffle yap
        List<String> modifiableOptions = new ArrayList<>(Arrays.asList(nextOptions));
        Collections.shuffle(modifiableOptions);
        nextOptions = modifiableOptions.toArray(new String[0]);

        correctOptionIndex = Arrays.asList(nextOptions).indexOf(correctAnswer);
        nextIntegralLatex = currentRule.getIntegralLatex(a, b, n);
    }


    private void displayCurrentQuestion() {
        updateLatex(integralWebView, nextIntegralLatex);
        for (int i = 0; i < optionWebViews.length; i++) {
            updateLatex(optionWebViews[i], nextOptions[i]);
        }
    }

    private void checkAnswer(int selectedIndex) {
        optionWebViews[selectedIndex].setBackgroundColor(selectedIndex == correctOptionIndex ? Color.GREEN : Color.RED);
        handler.postDelayed(() -> {
            for (WebView wv : optionWebViews) wv.setBackgroundColor(Color.TRANSPARENT);
            prepareNextQuestion();
            displayCurrentQuestion();
        }, 1000);
    }

    private void updateLatex(WebView webView, String latex) {
        String js = "javascript:setLatex(\"" + latex.replace("\\", "\\\\") + "\")";
        webView.post(() -> {
            webView.evaluateJavascript(js, value -> {
                Log.d("WebView", "Latex set response: " + value);
            });
        });
    }

}
