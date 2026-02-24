package com.example.drive_kit;

import android.view.accessibility.AccessibilityEvent;

import androidx.test.platform.app.InstrumentationRegistry;

import java.util.concurrent.TimeoutException;

public final class TestToastUtils {
    private TestToastUtils() {}

    public static void assertToastShownOnClick(Runnable clickAction, String expectedText, long timeoutMs) throws TimeoutException {
        var uiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();

        AccessibilityEvent event = uiAutomation.executeAndWaitForEvent(
                clickAction,
                e -> e != null
                        && e.getEventType() == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
                        && e.getText() != null
                        && e.getText().toString().contains(expectedText),
                timeoutMs
        );

        if (event == null) {
            throw new AssertionError("Toast not captured within " + timeoutMs + "ms: " + expectedText);
        }
    }
}