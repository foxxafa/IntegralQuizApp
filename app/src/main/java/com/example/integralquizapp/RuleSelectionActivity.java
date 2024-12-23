package com.example.integralquizapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class RuleSelectionActivity extends AppCompatActivity {

    private WebView rulesWebView;
    private List<IntegralRule> allRules;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rule_selection);

        // Tek bir WebView
        rulesWebView = findViewById(R.id.rulesWebView);
        WebSettings webSettings = rulesWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);

        // Android -> JS köprüsü
        rulesWebView.addJavascriptInterface(new AndroidInterface(), "AndroidInterface");

        // WebViewClient -> onPageFinished vs.
        rulesWebView.setWebViewClient(new WebViewClient());

        // Tüm kuralları al
        allRules = IntegralRules.getAllRules();

        // rule_selection.html yükle
        // NOT: "mathjax/rule_selection.html" dosyasını assets klasörünüzdeki konuma göre ayarlayın
        rulesWebView.loadUrl("file:///android_asset/mathjax/rule_selection.html");

        // Sayfa yüklendikten sonra kuralları JS tarafına aktaracağız
        // Bunu onPageFinished ile yapabiliriz, ya da kısa bir gecikme verebiliriz.
        // Burada setWebViewClient ile onPageFinished'i override edebiliriz:
        rulesWebView.setWebViewClient(new WebViewClient(){
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                Log.d("RuleSelectionActivity", "Page Finished: " + url);

                String rawJson = buildRulesJson();
                Log.d("RuleSelectionActivity", "buildRulesJson(): " + rawJson);

                // Burada rawJson'u JavaScript stringine dönüştürmek için çift tırnakları ve backslash'ları kaçıracağız.
                String escapedJson = rawJson
                        .replace("\\", "\\\\")
                        .replace("\"", "\\\"");

                // Çağrı:
                // showRules("escapedJsonString");
                String jsCode = "showRules(\"" + escapedJson + "\");";
                Log.d("RuleSelectionActivity", "jsCode: " + jsCode);

                rulesWebView.evaluateJavascript(jsCode, null);
            }
        });
    }

    // Kuralların JS tarafında gösterilmesi için JSON üretiyoruz.
    // Her kural: { ruleName: "Kural 1", formula: "\\int (ax+b)^n dx" }
    private String buildRulesJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < allRules.size(); i++) {
            IntegralRule r = allRules.get(i);
            sb.append("{");
            sb.append("\"ruleName\":\"").append(r.getName()).append("\",");
            // formül -> JSON güvenli hale getirmek için ters slash kaçışlarına dikkat
            String formula = r.getFormula().replace("\\", "\\\\");
            sb.append("\"formula\":\"").append(formula).append("\"");
            sb.append("}");
            if (i < allRules.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }

    // JS tarafındaki "onSaveRules" callbackine karşılık
    public class AndroidInterface {
        @JavascriptInterface
        public void onSaveRules(String selectedIndicesJson) {
            // selectedIndicesJson -> örn: "[0,1,3]"
            // Bunu parse edip SharedPreferences'a kaydedelim
            runOnUiThread(() -> {
                try {
                    // stringi array'e çevirelim
                    org.json.JSONArray arr = new org.json.JSONArray(selectedIndicesJson);
                    if (arr.length() < 4) {
                        Toast.makeText(RuleSelectionActivity.this,
                                "Lütfen en az 4 kural seçiniz!",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < arr.length(); i++) {
                        sb.append(arr.getInt(i)).append(",");
                    }
                    // SharedPreferences'a kaydet
                    SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putString("selected_rules", sb.toString());
                    editor.putBoolean("rule_selected", true);
                    editor.apply();

                    // Soru çözüm ekranına geç
                    Intent intent = new Intent(RuleSelectionActivity.this, QuestionActivity.class);
                    startActivity(intent);
                    finish();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }
    }
}
