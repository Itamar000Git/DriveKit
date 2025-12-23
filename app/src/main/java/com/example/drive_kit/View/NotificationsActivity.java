package com.example.drive_kit.View;
import androidx.lifecycle.ViewModelProvider;


import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.drive_kit.R;
import com.example.drive_kit.ViewModel.NotificationsViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.ArrayList;

/**
 * Activity for displaying notifications.
 * It observes the LiveData in the ViewModel and updates the UI accordingly.
 * If the list is empty, it removes all views from the container.
 * Otherwise, it creates a new TextView for each notification and adds it to the container.
 */
public class NotificationsActivity extends AppCompatActivity {

    private LinearLayout notificationsContainer; // container for notifications

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notifications);

        notificationsContainer = findViewById(R.id.notificationsContainer);
        // Initialize the ViewModel
        NotificationsViewModel viewModel = new ViewModelProvider(this).get(NotificationsViewModel.class);

        // Observe the LiveData and update the UI when the data changes with showNotifications()
        viewModel.getNoty().observe(this, this::showNotifications);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser(); // get the current user
        if (user == null) return;

        viewModel.loadNoty(user.getUid()); // load the notifications for the current user

    }

    /**
     * Shows the notifications in the UI.
     * If the list is empty, it removes all views from the container.
     * Otherwise, it creates a new TextView for each notification and adds it to the container.
     * @param noty
     */
    private void showNotifications(ArrayList<String> noty) {
        notificationsContainer.removeAllViews();
        if (noty == null || noty.isEmpty()) {
            return;
        }
        for (String msg : noty) {
            TextView tv = new TextView(this);
            tv.setText(msg);
            tv.setTextSize(16f);
            tv.setTextColor(0xFF001F3F);
            tv.setPadding(8, 8, 8, 16);
            notificationsContainer.addView(tv);
        }
    }


}
