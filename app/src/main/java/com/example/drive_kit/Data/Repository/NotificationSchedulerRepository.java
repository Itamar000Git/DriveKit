package com.example.drive_kit.Data.Repository;

import android.content.Context;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.drive_kit.Data.Workers.InsuranceNotyWorker;
import com.example.drive_kit.Data.Workers.NotyWorker;

import java.util.concurrent.TimeUnit;

public class NotificationSchedulerRepository {

    private static final String DRIVER_WORK_NAME = "drivekit_noty_worker_driver";
    private static final String INS_WORK_NAME = "drivekit_noty_worker_insurance";

    /**
     * Driver periodic notifications (existing logic).
     */
    public void scheduleDriverDaily(Context context) {
        PeriodicWorkRequest req =
                new PeriodicWorkRequest.Builder(NotyWorker.class, 1, TimeUnit.DAYS)
                        .build();

        WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                        DRIVER_WORK_NAME,
                        ExistingPeriodicWorkPolicy.KEEP,
                        req
                );
    }

    /**
     * Insurance periodic notifications (new inquiries).
     */
    public void scheduleInsuranceDaily(Context context) {
        PeriodicWorkRequest req =
                new PeriodicWorkRequest.Builder(InsuranceNotyWorker.class, 1, TimeUnit.DAYS)
                        .build();

        WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                        INS_WORK_NAME,
                        ExistingPeriodicWorkPolicy.KEEP,
                        req
                );
    }

    /**
     * Backward compatibility (if called from old code).
     */
    public void scheduleDaily(Context context) {
        scheduleDriverDaily(context);
    }

    public void cancelDriver(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(DRIVER_WORK_NAME);
    }

    public void cancelInsurance(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(INS_WORK_NAME);
    }

    public void cancelAllAppNoty(Context context) {
        cancelDriver(context);
        cancelInsurance(context);
    }
}
