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
 * NotyWorker
 *
 * A WorkManager background worker responsible for checking
 * driver-related expiration dates and triggering local notifications.
 *
 * Responsibilities:
 * - Fetch the current logged-in driver from Firestore.
 * - Calculate expiration stages (Insurance, Test, Treatment 10K).
 * - Trigger notifications based on time thresholds.
 *
 * Execution:
 * - Triggered periodically by WorkManager (typically once per day).
 * - Runs even if the app is closed.
 *
 * Important:
 * - Uses synchronous Firestore call (Tasks.await()).
 * - Returns Result.retry() if an exception occurs.
 */
public class NotyWorker extends Worker {

    /**
     * Worker constructor.
     *
     * @param context Application context
     * @param params  Worker parameters provided by WorkManager
     */
    public NotyWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    /**
     * Main background execution method.
     *
     * Steps:
     * 1. Verify user is logged in.
     * 2. Fetch Driver document from Firestore.
     * 3. If driver exists → build notifications.
     *
     * @return Result.success() if completed
     *         Result.retry() if exception occurs
     */
    @NonNull
    @Override
    public Result doWork() {

        try {

            // If no logged-in user → nothing to do
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                return Result.success();
            }

            String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

            // Fetch driver document synchronously from Firestore
            DocumentSnapshot doc = Tasks.await(
                    FirebaseFirestore.getInstance()
                            .collection("drivers")
                            .document(uid)
                            .get()
            );

            // Convert Firestore document to Driver object
            Driver driver = doc.toObject(Driver.class);

            if (driver == null) return Result.success();

            // Build and trigger notifications
            buildAndNotify(driver);

            return Result.success();

        } catch (Exception e) {

            // Retry later if something failed (e.g. network issue)
            return Result.retry();
        }
    }

    /**
     * Builds and evaluates all notification types:
     * - Insurance expiration
     * - Vehicle Test expiration
     * - 10K Treatment reminder
     */
    private void buildAndNotify(Driver driver) {

        long now = System.currentTimeMillis();
        long oneYearMillis = TimeUnit.DAYS.toMillis(366);

        // ================= INSURANCE =================

        long insuranceStart = driver.getCar().getInsuranceDateMillis();
        long insuranceEnd = insuranceStart + oneYearMillis;

        NotificationItem.Stage stage =
                NotificationsViewModel.calcStage(insuranceEnd, now);

        String dismissed = driver.getCar().getDismissedInsuranceStage();

        if (stage != NotificationItem.Stage.NONE &&
                (dismissed == null || !stage.name().equals(dismissed))) {

            if (insuranceStart > 0) {
                maybeNotify(insuranceEnd, now, "הביטוח");
            }
        }

        // ================= VEHICLE TEST =================

        long testStart = driver.getCar().getTestDateMillis();
        long testEnd = testStart + oneYearMillis;

        stage = NotificationsViewModel.calcStage(testEnd, now);
        dismissed = driver.getCar().getDismissedTestStage();

        if (stage != NotificationItem.Stage.NONE &&
                (dismissed == null || !stage.name().equals(dismissed))) {

            if (testStart > 0) {
                maybeNotify(testEnd, now, "הטסט");
            }
        }

        // ================= TREATMENT 10K =================

        long treatStart = driver.getCar().getTreatmentDateMillis();

        if (treatStart > 0) {

            NotificationItem.Stage treatStage =
                    NotificationsViewModel.calcTreatStage(treatStart, now);

            String dismissedTreat =
                    driver.getCar().getDismissedTreatment10kStage();

            if (treatStage != NotificationItem.Stage.NONE &&
                    (dismissedTreat == null ||
                            !treatStage.name().equals(dismissedTreat))) {

                maybeNotifyTreat(treatStage);
            }
        }
    }

    /**
     * Evaluates expiration thresholds and triggers notification
     * based on number of days remaining.
     *
     * @param endMillis Expiration timestamp
     * @param now       Current timestamp
     * @param label     Label for the item (Insurance / Test)
     */
    private void maybeNotify(long endMillis, long now, String label) {

        long daysUntil =
                TimeUnit.MILLISECONDS.toDays(endMillis - now);

        if (daysUntil <= 28 && daysUntil > 14) {
            NotificationHelper.show(getApplicationContext(),
                    "DriveKit",
                    "בעוד פחות מ 28 ימים יפוג תוקף " + label + " שלך");

        } else if (daysUntil <= 14 && daysUntil > 7) {
            NotificationHelper.show(getApplicationContext(),
                    "DriveKit",
                    "בעוד פחות מ 14 ימים יפוג תוקף " + label + " שלך");

        } else if (daysUntil <= 7 && daysUntil > 1) {
            NotificationHelper.show(getApplicationContext(),
                    "DriveKit",
                    "בעוד פחות מ 7 ימים יפוג תוקף " + label + " שלך");

        } else if (daysUntil == 1) {
            NotificationHelper.show(getApplicationContext(),
                    "DriveKit",
                    "בעוד יום אחד יפוג תוקף " + label + " שלך");

        } else if (daysUntil < 0) {
            NotificationHelper.show(getApplicationContext(),
                    "DriveKit",
                    "פג תוקף " + label + " שלך, נא לחדש בהקדם");
        }
    }

    /**
     * Triggers notification based on treatment stage.
     *
     * @param stage Calculated treatment stage
     */
    private void maybeNotifyTreat(NotificationItem.Stage stage) {

        switch (stage) {

            case M6:
                NotificationHelper.show(getApplicationContext(),
                        "DriveKit",
                        "עברו 6 חודשים מהטיפול האחרון. מומלץ לקבוע טיפול 10K");
                break;

            case M7:
                NotificationHelper.show(getApplicationContext(),
                        "DriveKit",
                        "עברו 7 חודשים מהטיפול האחרון. מומלץ לקבוע טיפול 10K");
                break;

            case M8:
                NotificationHelper.show(getApplicationContext(),
                        "DriveKit",
                        "עברו 8 חודשים מהטיפול האחרון. מומלץ לקבוע טיפול 10K");
                break;

            case EXPIRED_TREAT:
                NotificationHelper.show(getApplicationContext(),
                        "DriveKit",
                        "עברו 9 חודשים מהטיפול האחרון. פג תוקף טיפול 10K, נא לטפל בהקדם");
                break;

            default:
                break;
        }
    }
}
