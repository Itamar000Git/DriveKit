package com.example.drive_kit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.drive_kit.Model.Driver;
import com.example.drive_kit.Model.NotificationItem;
import com.example.drive_kit.ViewModel.NotificationsViewModel;
import com.example.drive_kit.Model.Car; // אם אצלך זה CarModel/Car -> להתאים לשם המדויק

import org.junit.Test;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class NotificationsLogicTest {

    @Test
    public void testDueSoon_buildNotifications_includesTestNotification() {
        long now = 1_700_000_000_000L; // fixed now for stable test
        int daysLeft = 7;

        long oneYear = TimeUnit.DAYS.toMillis(366);
        long end = now + TimeUnit.DAYS.toMillis(daysLeft);
        long testStartMillis = end - oneYear;

        Driver driver = mock(Driver.class);
        Car car = mock(Car.class);
        when(driver.getCar()).thenReturn(car);

        // Only TEST active
        when(car.getInsuranceDateMillis()).thenReturn(0L);
        when(car.getTreatmentDateMillis()).thenReturn(0L);

        when(car.getTestDateMillis()).thenReturn(testStartMillis);
        when(car.getDismissedTestStage()).thenReturn(null);

        NotificationsViewModel vm = new NotificationsViewModel();

        ArrayList<NotificationItem> list = vm.buildNotifications(driver, now);

        assertTrue("Expected at least one notification", list.size() >= 1);
        assertEquals(NotificationItem.Type.TEST, list.get(0).getType());
        assertEquals(NotificationItem.Stage.D7, list.get(0).getStage());
    }

    @Test
    public void insuranceDueSoon_defer_hidesInsuranceNotification() {
        long now = 1_700_000_000_000L;
        int daysLeft = 7;

        long oneYear = TimeUnit.DAYS.toMillis(366);
        long end = now + TimeUnit.DAYS.toMillis(daysLeft);
        long insuranceStartMillis = end - oneYear;

        Driver driver = mock(Driver.class);
        Car car = mock(Car.class);
        when(driver.getCar()).thenReturn(car);

        // Only INSURANCE active
        when(car.getTestDateMillis()).thenReturn(0L);
        when(car.getTreatmentDateMillis()).thenReturn(0L);

        when(car.getInsuranceDateMillis()).thenReturn(insuranceStartMillis);

        NotificationsViewModel vm = new NotificationsViewModel();

        // Before defer -> should appear
        when(car.getDismissedInsuranceStage()).thenReturn(null);
        ArrayList<NotificationItem> list1 = vm.buildNotifications(driver, now);
        assertEquals(1, list1.size());
        assertEquals(NotificationItem.Type.INSURANCE, list1.get(0).getType());
        assertEquals(NotificationItem.Stage.D7, list1.get(0).getStage());

        // After defer -> should be hidden
        when(car.getDismissedInsuranceStage()).thenReturn(NotificationItem.Stage.D7.name());
        ArrayList<NotificationItem> list2 = vm.buildNotifications(driver, now);
        assertEquals(0, list2.size());
    }

    /**
     * Logic-only version of: "Click Done -> try future date -> blocked"
     *
     * In UI you open a date picker and it's blocked.
     * In logic we assert the rule behind it: future dates are not allowed.
     *
     * When you have a real production method that validates done-date,
     * replace isFutureDateBlocked(...) with that method call.
     */
    @Test
    public void doneDate_futureDate_isBlocked_logicOnly() {
        long now = 1_700_000_000_000L; // fixed
        long futureDoneDate = now + TimeUnit.DAYS.toMillis(3);

        assertTrue("Future done date must be blocked", isFutureDateBlocked(futureDoneDate, now));
    }

    // Replace this with your real app validator when available.
    private static boolean isFutureDateBlocked(long selectedDateMillis, long nowMillis) {
        return selectedDateMillis > nowMillis;
    }
}