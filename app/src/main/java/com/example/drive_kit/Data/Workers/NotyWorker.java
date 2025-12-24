package com.example.drive_kit.Data.Workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.drive_kit.Data.Notification.NotificationHelper;
import com.example.drive_kit.Model.Driver;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.TimeUnit;

public class NotyWorker extends Worker {

    public NotyWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {

        try {
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                return Result.success();
            }

            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            DocumentSnapshot doc = Tasks.await(
                    FirebaseFirestore.getInstance()
                            .collection("drivers")
                            .document(uid)
                            .get()
            );

            Driver driver = doc.toObject(Driver.class);
            if (driver == null) return Result.success();

            buildAndNotify(driver);

            return Result.success();
        } catch (Exception e) {
            return Result.retry();
        }
    }

    private void buildAndNotify(Driver driver) {
        long now = System.currentTimeMillis();
        long oneYearMillis = TimeUnit.DAYS.toMillis(366);

        long insuranceStart = driver.getInsuranceDateMillis();
        if (insuranceStart > 0) {
            long insuranceEnd = insuranceStart + oneYearMillis;
            maybeNotify(insuranceEnd, now, "הביטוח");
        }

        long testStart = driver.getTestDateMillis();
        if (testStart > 0) {
            long testEnd = testStart + oneYearMillis;
            maybeNotify(testEnd, now, "הטסט");
        }
    }

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
