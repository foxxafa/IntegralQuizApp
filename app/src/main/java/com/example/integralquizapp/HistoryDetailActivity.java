package com.example.integralquizapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import org.json.JSONArray;
import org.json.JSONObject;

public class HistoryDetailActivity extends AppCompatActivity {

    private WebView historyWebView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_detail);

        historyWebView = findViewById(R.id.historyDetailWebView);
        WebSettings ws = historyWebView.getSettings();
        ws.setJavaScriptEnabled(true);
        ws.setDomStorageEnabled(true);

        historyWebView.setWebViewClient(new WebViewClient());

        // history_detail.html'i yükleyin
        historyWebView.loadUrl("file:///android_asset/mathjax/history_detail.html");

        // Intent ile gelen JSON verisi
        final String detailJson = getIntent().getStringExtra("detail_json"); // FINAL olarak tanımlandı
        final String safeJson = (detailJson != null ? detailJson : "{}"); // Null kontrolü yapıldı;

        // Sayfa yüklendikten sonra JS fonksiyonunu çağıralım
        historyWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);

                // JSON'u güvenli hale getirin
                String escapedJson = safeJson
                        .replace("\\", "\\\\")
                        .replace("\"", "\\\"");

                // JS fonksiyonunu çağır
                String jsCode = "showHistoryDetail(\"" + escapedJson + "\");";
                historyWebView.evaluateJavascript(jsCode, null);
            }
        });
    }

}
