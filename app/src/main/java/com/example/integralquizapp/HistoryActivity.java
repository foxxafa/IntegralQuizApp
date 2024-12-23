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
    private List<JSONObject> historyDetailsJson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        historyListView = findViewById(R.id.historyListView);
        historyList = new ArrayList<>();
        historyDetailsJson = new ArrayList<>();

        loadHistoryFromPrefs();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, historyList);
        historyListView.setAdapter(adapter);

        historyListView.setOnItemClickListener((parent, view, position, id) -> {
            JSONObject detailObj = historyDetailsJson.get(position);
            Intent intent = new Intent(HistoryActivity.this, HistoryDetailActivity.class);
            intent.putExtra("detail_json", detailObj.toString());
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
                String date = obj.getString("date");
                historyList.add(date);
                historyDetailsJson.add(obj);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}



