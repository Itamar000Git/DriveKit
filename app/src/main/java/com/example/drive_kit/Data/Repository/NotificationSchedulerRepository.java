package com.example.drive_kit.Data.Repository;

import android.content.Context;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.drive_kit.Data.Workers.InsuranceNotyWorker;
import com.example.drive_kit.Data.Workers.NotyWorker;

import java.util.concurrent.TimeUnit;

/**
 * NotificationSchedulerRepository
 *
 * Responsible for scheduling and canceling background notification tasks
 * using Android WorkManager.
 *
 * Responsibilities:
 * - Schedule daily notifications for Drivers.
 * - Schedule daily notifications for Insurance users.
 * - Cancel scheduled work when needed.
 *
 * Architecture Notes:
 * - Uses WorkManager for reliable background execution.
 * - Each work is registered as UNIQUE to avoid duplication.
 * - Periodic work runs once every 24 hours.
 *
 * IMPORTANT:
 * Always use Application Context when calling these methods.
 */
public class NotificationSchedulerRepository {

    /**
     * Unique name for Driver periodic notification work.
     * Used to prevent duplicate scheduling.
     */
    private static final String DRIVER_WORK_NAME = "drivekit_noty_worker_driver";

    /**
     * Unique name for Insurance periodic notification work.
     */
    private static final String INS_WORK_NAME = "drivekit_noty_worker_insurance";

    /**
     * Schedules daily notifications for Driver users.
     *
     * This registers a PeriodicWorkRequest that runs once every 24 hours.
     * If work with the same name already exists, it will be kept
     * (ExistingPeriodicWorkPolicy.KEEP).
     *
     * @param context Application context (NOT Activity context)
     */
    public void scheduleDriverDaily(Context context) {

        // Create periodic work request that runs every 1 day
        PeriodicWorkRequest req =
                new PeriodicWorkRequest.Builder(NotyWorker.class, 1, TimeUnit.DAYS)
                        .build();

        // Enqueue unique periodic work to prevent duplicates
        WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(
                        DRIVER_WORK_NAME,
                        ExistingPeriodicWorkPolicy.KEEP,
                        req
                );
    }

    /**
     * Schedules daily notifications for Insurance users.
     *
     * This typically checks for new inquiries or relevant updates.
     *
     * @param context Application context
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
     * Backward compatibility method.
     *
     * If older code calls scheduleDaily(),
     * it will default to Driver notifications.
     *
     * @param context Application context
     */
    public void scheduleDaily(Context context) {
        scheduleDriverDaily(context);
    }

    /**
     * Cancels Driver periodic notification work.
     *
     * @param context Application context
     */
    public void cancelDriver(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(DRIVER_WORK_NAME);
    }

    /**
     * Cancels Insurance periodic notification work.
     *
     * @param context Application context
     */
    public void cancelInsurance(Context context) {
        WorkManager.getInstance(context).cancelUniqueWork(INS_WORK_NAME);
    }

    /**
     * Cancels ALL app-related notification background work.
     *
     * This method is typically called on logout.
     *
     * @param context Application context
     */
    public void cancelAllAppNoty(Context context) {
        cancelDriver(context);
        cancelInsurance(context);
    }
}
