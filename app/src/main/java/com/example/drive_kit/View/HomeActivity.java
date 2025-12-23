package com.example.drive_kit.View;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.drive_kit.R;
import com.example.drive_kit.ViewModel.HomeViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class HomeActivity extends AppCompatActivity {

    private TextView welcomeText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home_activity);

        welcomeText = findViewById(R.id.welcomeText);
        ImageView notyIcon = findViewById(R.id.noty_icon);

        notyIcon.setOnClickListener(v -> {
            Intent intent = new Intent(HomeActivity.this, NotificationsActivity.class);
            startActivity(intent);
        });


        HomeViewModel viewModel = new ViewModelProvider(this).get(HomeViewModel.class);

        viewModel.getWelcomeText().observe(this, welcomeText::setText);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String uid = (user == null) ? null : user.getUid();

        viewModel.loadWelcomeText(uid);
    }
}
