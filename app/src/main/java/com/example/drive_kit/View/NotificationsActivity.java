package com.example.drive_kit.View;
import androidx.lifecycle.ViewModelProvider;


import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.drive_kit.Model.NotificationItem;
import com.example.drive_kit.R;
import com.example.drive_kit.ViewModel.NotificationsViewModel;
import com.google.android.material.datepicker.MaterialDatePicker;
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

    private LinearLayout notificationsContainer;
    private NotificationsViewModel viewModel;


    @SuppressLint("MissingInflatedId")/// ///
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.notifications);

        notificationsContainer = findViewById(R.id.notificationsContainer);
        //Initialize the ViewModel

        viewModel = new ViewModelProvider(this).get(NotificationsViewModel.class);

        // Observe the LiveData and update the UI when the data changes with showNotifications()
        viewModel.getNoty().observe(this, this::showNotifications);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser(); // get the current user
        if (user == null) return;


        viewModel.loadNoty(user.getUid()); // load the notifications for the current user

        viewModel.getToastMessage().observe(this, msg -> {
            if (msg != null) {
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
            }
        });

    }

    /**
     * Shows the notifications in the UI.
     * If the list is empty, it removes all views from the container.
     * @param noty
     */
    private void showNotifications(ArrayList<NotificationItem> noty) {
        notificationsContainer.removeAllViews();

        if (noty == null || noty.isEmpty()) {
            return;
        }

        LayoutInflater inflater = LayoutInflater.from(this);

        for (NotificationItem item : noty) {
            View itemView = inflater.inflate(
                    R.layout.item_notification,
                    notificationsContainer,
                    false
            );

            TextView tv = itemView.findViewById(R.id.notificationText);

            Button btnDefer = itemView.findViewById(R.id.btnDefer);
            Button btnDone = itemView.findViewById(R.id.btnDone);

            viewModel = new ViewModelProvider(this).get(NotificationsViewModel.class);

            tv.setText(item.getMessage());

            btnDefer.setOnClickListener(v -> {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null) return;
                Toast.makeText(this, "ההתראה נדחתה", Toast.LENGTH_SHORT).show();
                viewModel.deferNotification(user.getUid(), item);
            });


            btnDone.setOnClickListener(v -> {
                Toast.makeText(this, "המשימה בוצעה בהצלחה", Toast.LENGTH_SHORT).show();
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null) return;

                openDoneDatePicker(item, user.getUid());
            });

            notificationsContainer.addView(itemView);
        }
    }

    /**
     * Opens the date picker for the done button.
     * It uses the MaterialDatePicker class to show the date picker.
     * @param item
     * @param uid
     */
    private void openDoneDatePicker(NotificationItem item, String uid) {
        String title = (item.getType() == NotificationItem.Type.INSURANCE)
                ? "בחר תאריך ביטוח חדש"
                : "בחר תאריך טסט חדש";
        MaterialDatePicker<Long> picker =MaterialDatePicker.Builder.datePicker().setTitleText(title).build();

        picker.show(getSupportFragmentManager(), "DONE_DATE_PICKER");

        picker.addOnPositiveButtonClickListener(selection -> {
            viewModel.doneNotification(uid, item, selection);
        });
    }


}
