package com.example.integralquizapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private ListView historyListView;
    private List<String> historyList;
    private List<HistoryDetail> historyDetails;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        historyListView = findViewById(R.id.historyListView);
        historyList = new ArrayList<>();
        historyDetails = new ArrayList<>();

        loadHistoryFromPrefs();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, historyList);
        historyListView.setAdapter(adapter);

        historyListView.setOnItemClickListener((parent, view, position, id) -> {
            Intent intent = new Intent(HistoryActivity.this, HistoryDetailActivity.class);
            // Sadece tarih, doğru, yanlış bilgilerini gönderiyoruz.
            intent.putExtra("date", historyDetails.get(position).getDate());
            intent.putExtra("correct", historyDetails.get(position).getCorrect());
            intent.putExtra("wrong", historyDetails.get(position).getWrong());
            startActivity(intent);
        });
    }

    private void loadHistoryFromPrefs() {
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String historyJson = prefs.getString("history_data", "[]");

        try {
            JSONArray historyArray = new JSONArray(historyJson);
            for (int i = 0; i < historyArray.length(); i++) {
                JSONObject obj = historyArray.getJSONObject(i);

                // Artık ruleName kullanmıyoruz, direkt olarak date, correct, wrong
                String date = obj.getString("date");
                int correct = obj.getInt("correct");
                int wrong = obj.getInt("wrong");

                // Liste için basit format: Tarih alt satırda Doğru/Yanlış
                String displayText = date + "\nDoğru: " + correct + "   Yanlış: " + wrong;

                historyList.add(displayText);
                historyDetails.add(new HistoryDetail(date, correct, wrong));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static class HistoryDetail {
        private final String date;
        private final int correct;
        private final int wrong;

        public HistoryDetail(String date, int correct, int wrong) {
            this.date = date;
            this.correct = correct;
            this.wrong = wrong;
        }

        public String getDate() { return date; }
        public int getCorrect() { return correct; }
        public int getWrong() { return wrong; }
    }
}


