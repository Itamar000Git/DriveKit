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

public class NotificationsActivity extends AppCompatActivity {
//    private FirebaseAuth auth;
//    private FirebaseFirestore db;
    //private ArrayList<String> noty;

//    private long currentInsuranceDate;
//    private long currentTestDate;

    private LinearLayout notificationsContainer;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.notifications);

        notificationsContainer = findViewById(R.id.notificationsContainer);
        NotificationsViewModel viewModel =
                new ViewModelProvider(this).get(NotificationsViewModel.class);

        viewModel.getNoty().observe(this, this::showNotifications);

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        viewModel.loadNoty(user.getUid());
//        FirebaseUser user = auth.getCurrentUser();
//        String uid = user.getUid();


//        db.collection("drivers")
//                .document(uid)
//                .get()
//                .addOnSuccessListener(documentSnapshot -> {
//                    if (documentSnapshot.exists()) {
//                        //converts the document to a driver object
//                        Driver driver = documentSnapshot.toObject(Driver.class);
//
//                        if (driver != null) {
//                            currentInsuranceDate = driver.getInsuranceDateMillis();
//                            currentTestDate= driver.getTestDateMillis();
//
//                            Log.d("Notifications", "currentInsuranceDate = " + currentInsuranceDate);
//                            Log.d("Notifications", "currentTestDate = " + currentTestDate);
//
//
//                            if (currentInsuranceDate > 0) {
//                                long now = System.currentTimeMillis();
//
//                                long oneYearMillis = TimeUnit.DAYS.toMillis(366);
//
//                                long insuranceEndMillis = currentInsuranceDate + oneYearMillis;
//
//                                long diffMillis = insuranceEndMillis - now;
//                                long daysUntil = TimeUnit.MILLISECONDS.toDays(diffMillis);
//
//                                Log.d("Notifications", "daysUntil = " + daysUntil);
//                                // builds the notification list
//                                if (daysUntil <= 28 && daysUntil > 14) {
//                                    noty.add("בעוד פחות מ 28 ימים יפוג תוקף הביטוח שלך");
//                                }
//                                if (daysUntil <= 14 && daysUntil > 7) {
//                                    noty.add("בעוד פחות מ 14 ימים יפוג תוקף הביטוח שלך");
//                                }
//                                if (daysUntil <= 7 && daysUntil > 1) {
//                                    noty.add("בעוד פחות מ 7 ימים יפוג תוקף הביטוח שלך");
//                                }
//                                if (daysUntil == 1) {
//                                    noty.add("בעוד יום אחד יפוג תוקף הביטוח שלך");
//                                }
//                                if (daysUntil < 0) {
//                                    noty.add("פג תוקף הביטוח שלך, נא לחדש את הביטוח בהקדם");
//                                }
//                            }
//                            if (currentTestDate > 0) {
//                                long now = System.currentTimeMillis();
//
//                                long oneYearMillis = TimeUnit.DAYS.toMillis(366);
//
//                                long dateEndMillis = currentTestDate + oneYearMillis;
//
//                                long diffMillis = dateEndMillis - now;
//                                long daysUntil = TimeUnit.MILLISECONDS.toDays(diffMillis);
//
//                                Log.d("Notifications", "daysUntil = " + daysUntil);
//                                // builds the notification list
//                                if (daysUntil <= 28 && daysUntil > 14) {
//                                    noty.add("בעוד פחות מ 28 ימים יפוג תוקף הטסט שלך");
//                                }
//                                if (daysUntil <= 14 && daysUntil > 7) {
//                                    noty.add("בעוד פחות מ 14 ימים יפוג תוקף הטסט שלך");
//                                }
//                                if (daysUntil <= 7 && daysUntil > 1) {
//                                    noty.add("בעוד פחות מ 7 ימים יפוג תוקף הטסט שלך");
//                                }
//                                if (daysUntil == 1) {
//                                    noty.add("בעוד יום אחד יפוג תוקף הטסט שלך");
//                                }
//                                if (daysUntil < 0) {
//                                    noty.add("פג תוקף הביטוח שלך, נא לחדש את הטסט בהקדם");
//                                }
//                            }
//                            showNotifications();
//                        }
//                    } else {
//
//                    }
//                })
//                .addOnFailureListener(e -> {
//                  //  welcomeText.setText("שגיאה בטעינת הנתונים");
//                });
    }

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
