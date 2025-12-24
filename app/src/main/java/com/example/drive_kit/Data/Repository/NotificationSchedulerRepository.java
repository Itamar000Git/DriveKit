package com.example.drive_kit.Data.Repository;

import android.content.Context;

import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.example.drive_kit.Data.Workers.NotyWorker;

import java.util.concurrent.TimeUnit;

public class NotificationSchedulerRepository {

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
