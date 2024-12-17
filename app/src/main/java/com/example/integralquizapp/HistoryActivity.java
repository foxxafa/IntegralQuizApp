package com.example.integralquizapp;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    private ListView historyListView;
    private List<String> historyList;
    private List<HistoryDetail> historyDetails; // Detaylı verileri tutmak için bir liste

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        historyListView = findViewById(R.id.historyListView);
        historyList = new ArrayList<>();
        historyDetails = new ArrayList<>();

        // Örnek geçmiş verileri - Gerçek veriyi SharedPreferences veya veritabanından çekebilirsiniz
        addHistory("Kural 1", "2024-06-10 12:00", 5, 2);
        addHistory("Kural 2", "2024-06-10 15:00", 8, 1);
        addHistory("Kural 4", "2024-06-11 14:30", 6, 4);

        // ListView için Adapter oluştur
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, historyList);
        historyListView.setAdapter(adapter);

        // Liste elemanına tıklanınca detayları aç
        historyListView.setOnItemClickListener((AdapterView<?> parent, android.view.View view, int position, long id) -> {
            Intent intent = new Intent(HistoryActivity.this, HistoryDetailActivity.class);
            intent.putExtra("ruleName", historyDetails.get(position).getRuleName());
            intent.putExtra("correct", historyDetails.get(position).getCorrect());
            intent.putExtra("wrong", historyDetails.get(position).getWrong());
            intent.putExtra("date", historyDetails.get(position).getDate());
            startActivity(intent);
        });
    }

    private void addHistory(String ruleName, String date, int correct, int wrong) {
        String displayText = date + " - " + ruleName + " - Doğru: " + correct + ", Yanlış: " + wrong;
        historyList.add(displayText);
        historyDetails.add(new HistoryDetail(ruleName, date, correct, wrong));
    }

    private static class HistoryDetail {
        private final String ruleName;
        private final String date;
        private final int correct;
        private final int wrong;

        public HistoryDetail(String ruleName, String date, int correct, int wrong) {
            this.ruleName = ruleName;
            this.date = date;
            this.correct = correct;
            this.wrong = wrong;
        }

        public String getRuleName() { return ruleName; }
        public String getDate() { return date; }
        public int getCorrect() { return correct; }
        public int getWrong() { return wrong; }
    }
}
