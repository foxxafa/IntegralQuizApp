package com.example.integralquizapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class RuleSelectionActivity extends AppCompatActivity {

    private LinearLayout rulesLayout;
    private Button saveButton;
    private List<CheckBox> ruleCheckBoxes = new ArrayList<>();
    private List<IntegralRule> allRules;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_rule_selection);

        rulesLayout = findViewById(R.id.rulesLayout);
        saveButton = findViewById(R.id.btnSave);

        allRules = IntegralRules.getAllRules();

        for (int i = 0; i < allRules.size(); i++) {
            CheckBox checkBox = new CheckBox(this);
            checkBox.setText(allRules.get(i).getName());
            rulesLayout.addView(checkBox);
            ruleCheckBoxes.add(checkBox);
        }

        saveButton.setOnClickListener(v -> saveSelectedRules());
    }

    private void saveSelectedRules() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        StringBuilder selectedRules = new StringBuilder();
        int ruleCount = 0;

        for (int i = 0; i < ruleCheckBoxes.size(); i++) {
            if (ruleCheckBoxes.get(i).isChecked()) {
                selectedRules.append(i).append(","); // İndeksleri düzgün ekle
                ruleCount++;
            }
        }

        if (ruleCount < 4) { // Minimum 4 kural kontrolü
            Toast.makeText(this, "Lütfen en az 4 kural seçiniz!", Toast.LENGTH_SHORT).show();
            return; // İşlemi iptal et
        }

        editor.putString("selected_rules", selectedRules.toString());
        editor.putBoolean("rule_selected", true);
        editor.apply();

        Intent intent = new Intent(this, QuestionActivity.class);
        startActivity(intent);
        finish();
    }


}
