package com.example.integralquizapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Button btnResetPreferences = findViewById(R.id.btnResetPreferences);

        btnResetPreferences.setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear(); // Tüm SharedPreferences verilerini temizle
            editor.apply();

            Toast.makeText(this, "Ayarlar sıfırlandı!", Toast.LENGTH_SHORT).show();
        });
    }
}
