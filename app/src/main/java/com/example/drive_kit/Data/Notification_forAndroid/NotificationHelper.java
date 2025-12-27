package com.example.drive_kit.Data.Notification_forAndroid;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationCompat;

import com.example.drive_kit.R;

/**
 * Helper class for showing notifications.
 * It uses the NotificationManager to show notifications.
 * If the device is running on Android Oreo or higher, it also creates a notification channel.
 * It uses the NotificationCompat.Builder class to create the notification.
 * The notification is shown using the NotificationManager.
 */
public class NotificationHelper {
    public static final String CHANNEL_ID = "drivekit_noty";

    /**
     * Ensures that the notification channel exists.
     * If the device is running on Android Oreo or higher, it creates a notification channel.
     * @param ctx
     */
    public static void ensureChannel(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "DriveKit Notifications",
                    NotificationManager.IMPORTANCE_HIGH
            );
            NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
            nm.createNotificationChannel(channel);
        }
    }

    /**
     * Shows a notification.
     * It uses the NotificationCompat.Builder class to create the notification.
     * @param ctx
     * @param title
     * @param msg
     */
    public static void show(Context ctx, String title, String msg) {
        ensureChannel(ctx);

        NotificationCompat.Builder b = new NotificationCompat.Builder(ctx, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(msg)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        NotificationManager nm = (NotificationManager) ctx.getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify((int) System.currentTimeMillis(), b.build());
    }
}
