package com.example.integralquizapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Button startButton, historyButton,btnSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startButton = findViewById(R.id.btnStart);
        historyButton = findViewById(R.id.btnHistory);
        btnSettings = findViewById(R.id.btnSettings);

        startButton.setOnClickListener(v -> {
            if (isRuleSelected()) {
                startActivity(new Intent(MainActivity.this, QuestionActivity.class));
            } else {
                startActivity(new Intent(MainActivity.this, RuleSelectionActivity.class));
            }
        });

        historyButton.setOnClickListener(v -> {
            startActivity(new Intent(MainActivity.this, HistoryActivity.class));
        });

        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
    }

    private boolean isRuleSelected() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        return prefs.getBoolean("rule_selected", false);
    }
}
