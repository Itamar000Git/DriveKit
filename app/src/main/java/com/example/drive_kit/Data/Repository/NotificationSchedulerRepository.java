package com.example.drive_kit.Data.Repository;

import android.content.Context;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.drive_kit.Data.Workers.NotyWorker;

import java.util.concurrent.TimeUnit;

/**
 * Repository for accessing the database.
 * It uses the FirebaseFirestore class to access the database.
 */
public class NotificationSchedulerRepository {

    /**
     * Schedules the daily notification worker.
     * It uses the WorkManager class to schedule the worker.
     * @param context
     */
    public void scheduleDaily(Context context) {
        PeriodicWorkRequest req =
                new PeriodicWorkRequest.Builder(NotyWorker.class, 1, TimeUnit.DAYS)
                        .build();

        WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                        "drivekit_noty_worker",
                        ExistingPeriodicWorkPolicy.KEEP,
                        req
                );
    }
}
