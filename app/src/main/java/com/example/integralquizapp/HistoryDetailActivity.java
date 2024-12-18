package com.example.integralquizapp;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

public class HistoryDetailActivity extends AppCompatActivity {

    private TextView ruleNameTextView, dateTextView, correctTextView, wrongTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history_detail);

        ruleNameTextView = findViewById(R.id.ruleNameTextView);
        dateTextView = findViewById(R.id.dateTextView);
        correctTextView = findViewById(R.id.correctTextView);
        wrongTextView = findViewById(R.id.wrongTextView);

        // Veriyi al
        String date = getIntent().getStringExtra("date");
        int correct = getIntent().getIntExtra("correct", 0);
        int wrong = getIntent().getIntExtra("wrong", 0);

        // ruleName kullanılmayacak, gizle
        ruleNameTextView.setVisibility(View.GONE);

        // Basit format
        // Örneğin ekranda:
        // 2024-12-18 19:21
        // Doğru: 2   Yanlış: 1
        dateTextView.setText(date);
        correctTextView.setText("Doğru: " + correct);
        wrongTextView.setText("Yanlış: " + wrong);
    }
}

