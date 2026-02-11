package com.example.drive_kit.Data.Workers;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.drive_kit.Data.Notification_forAndroid.NotificationHelper;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;

/**
 * Periodic worker for insurance users.
 * Checks if there are new inquiries for the logged-in insurance company,
 * and shows a notification when count > 0.
 */
public class InsuranceNotyWorker extends Worker {

    public InsuranceNotyWorker(@NonNull Context context, @NonNull WorkerParameters params) {
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

            // 1) Find insurance company doc linked to this partner uid
            QuerySnapshot companyQs = Tasks.await(
                    FirebaseFirestore.getInstance()
                            .collection("insurance_companies")
                            .whereEqualTo("partnerUid", uid)
                            .whereEqualTo("isPartner", true)
                            .limit(1)
                            .get()
            );

            if (companyQs == null || companyQs.isEmpty()) {
                // Not an insurance user -> nothing to do
                return Result.success();
            }

            String companyId = companyQs.getDocuments().get(0).getId();
            if (companyId == null || companyId.trim().isEmpty()) {
                return Result.success();
            }

            // 2) Count "new" inquiries for this company
            QuerySnapshot inquiriesQs = Tasks.await(
                    FirebaseFirestore.getInstance()
                            .collection("insurance_inquiries")
                            .whereEqualTo("companyId", companyId.trim().toLowerCase())
                            .whereEqualTo("status", "new")
                            .get()
            );

            int newCount = (inquiriesQs == null) ? 0 : inquiriesQs.size();

            if (newCount > 0) {
                String body = (newCount == 1)
                        ? "יש לך פנייה חדשה אחת ממתינה"
                        : "יש לך " + newCount + " פניות חדשות ממתינות";
                NotificationHelper.show(getApplicationContext(), "DriveKit ביטוח", body);
            }

            return Result.success();
        } catch (Exception e) {
            return Result.retry();
        }
    }
}
