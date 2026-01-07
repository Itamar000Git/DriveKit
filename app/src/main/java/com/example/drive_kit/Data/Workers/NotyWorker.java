package com.example.drive_kit.Data.Workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.drive_kit.Data.Notification_forAndroid.NotificationHelper;
import com.example.drive_kit.Model.Driver;
import com.example.drive_kit.Model.NotificationItem;
import com.example.drive_kit.ViewModel.NotificationsViewModel;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.TimeUnit;

/**
 * Worker for scheduling the daily notification.
 * It uses the NotificationHelper class to show notifications.
 */
public class NotyWorker extends Worker {

    /**
     * Constructor for the worker.
     * @param context
     * @param params
     */
    public NotyWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    /**
     * Does the work.
     * It uses the NotificationHelper class to show notifications.
     * @return
     */
    @NonNull
    @Override
    public Result doWork() {

        try {
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                return Result.success();
            }

            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            //getting the driver object from the database
            DocumentSnapshot doc = Tasks.await(
                    FirebaseFirestore.getInstance()
                            .collection("drivers")
                            .document(uid)
                            .get()
            );

            //if the driver object is null, it returns success
            Driver driver = doc.toObject(Driver.class);
            if (driver == null) return Result.success();

            //building the notification list and showing it
            buildAndNotify(driver);

            return Result.success();
        } catch (Exception e) {
            return Result.retry();
        }
    }

    /**
     * Builds the notification list and shows it.
     * It uses the NotificationHelper class to show notifications.
     * @param driver
     */
    private void buildAndNotify(Driver driver) {
        long now = System.currentTimeMillis();
        long oneYearMillis = TimeUnit.DAYS.toMillis(366);

        long insuranceStart = driver.getInsuranceDateMillis();
        long insuranceEnd = insuranceStart + oneYearMillis;

        NotificationItem.Stage stage = NotificationsViewModel.calcStage(insuranceEnd, now);
        String dismissed = driver.getDismissedInsuranceStage();

        if (stage != NotificationItem.Stage.NONE && (dismissed == null || !stage.name().equals(dismissed))) {
            if (insuranceStart > 0) {
                maybeNotify(insuranceEnd, now, "הביטוח");
            }
        }

        long testStart = driver.getTestDateMillis();
        long testEnd = testStart + oneYearMillis;

        stage = NotificationsViewModel.calcStage(testEnd, now);
        dismissed = driver.getDismissedTestStage();

        if (stage != NotificationItem.Stage.NONE && (dismissed == null || !stage.name().equals(dismissed))) {
            if (testStart > 0) {
                maybeNotify(testEnd, now, "הטסט");
            }
        }

    }

    /**
     * Maybe shows a notification.
     * It uses the NotificationHelper class to show notifications.
     * If the driver object is null, it returns.
     * If the driver object is not null, it checks if the notification should be shown.
     * If the notification should be shown, it shows it.
     * @param endMillis
     * @param now
     * @param label
     */
    private void maybeNotify(long endMillis, long now, String label) {
        long daysUntil = TimeUnit.MILLISECONDS.toDays(endMillis - now);

        if (daysUntil <= 28 && daysUntil > 14) {
            NotificationHelper.show(getApplicationContext(), "DriveKit", "בעוד פחות מ 28 ימים יפוג תוקף " + label + " שלך");
        } else if (daysUntil <= 14 && daysUntil > 7) {
            NotificationHelper.show(getApplicationContext(), "DriveKit", "בעוד פחות מ 14 ימים יפוג תוקף " + label + " שלך");
        } else if (daysUntil <= 7 && daysUntil > 1) {
            NotificationHelper.show(getApplicationContext(), "DriveKit", "בעוד פחות מ 7 ימים יפוג תוקף " + label + " שלך");
        } else if (daysUntil == 1) {
            NotificationHelper.show(getApplicationContext(), "DriveKit", "בעוד יום אחד יפוג תוקף " + label + " שלך");
        } else if (daysUntil < 0) {
            NotificationHelper.show(getApplicationContext(), "DriveKit", "פג תוקף " + label + " שלך, נא לחדש בהקדם");
        }
    }
}
