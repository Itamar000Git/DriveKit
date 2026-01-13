package com.example.drive_kit.View;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.drive_kit.R;

public class CarDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_details);

        TextView title = findViewById(R.id.carTitle);
        TextView info  = findViewById(R.id.carInfo);

        String nickname = getIntent().getStringExtra("nickname");
        String plate = getIntent().getStringExtra("plate");
        String manufacturer = getIntent().getStringExtra("manufacturer");
        String model = getIntent().getStringExtra("model");
        int year = getIntent().getIntExtra("year", 0);
        long km = getIntent().getLongExtra("km", 0);

        String header = (nickname != null && !nickname.isEmpty()) ? nickname : "פרטי רכב";
        title.setText(header);

        String text =
                "לוחית: " + safe(plate) + "\n" +
                        "יצרן: " + safe(manufacturer) + "\n" +
                        "דגם: " + safe(model) + "\n" +
                        "שנה: " + (year == 0 ? "-" : year) + "\n" +
                        "ק״מ: " + (km == 0 ? "-" : km);

        info.setText(text);
    }

    private String safe(String s) {
        return (s == null || s.trim().isEmpty()) ? "-" : s;
    }
}
