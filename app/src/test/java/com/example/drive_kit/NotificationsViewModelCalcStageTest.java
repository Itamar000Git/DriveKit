//package com.example.drive_kit;
//
//import static org.junit.Assert.assertEquals;
//
//import com.example.drive_kit.Model.Driver;
//import com.example.drive_kit.Model.NotificationItem;
//import com.example.drive_kit.ViewModel.NotificationsViewModel;
//
//import org.junit.Test;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//
//
//import java.util.ArrayList;
//import java.util.Calendar;
//
//import java.util.concurrent.TimeUnit;
//
//
//
//
//public class NotificationsViewModelCalcStageTest {
//
//    @Test
//    public void calcStage_returnsCorrectStages() {
//        long now = System.currentTimeMillis();
//
//        // 20 days -> D28
//        assertEquals(NotificationItem.Stage.D28,
//                NotificationsViewModel.calcStage(now + TimeUnit.DAYS.toMillis(20), now));
//
//        // 10 days -> D14
//        assertEquals(NotificationItem.Stage.D14,
//                NotificationsViewModel.calcStage(now + TimeUnit.DAYS.toMillis(10), now));
//
//        // 3 days -> D7
//        assertEquals(NotificationItem.Stage.D7,
//                NotificationsViewModel.calcStage(now + TimeUnit.DAYS.toMillis(3), now));
//
//        // 1 day -> D1
//        assertEquals(NotificationItem.Stage.D1,
//                NotificationsViewModel.calcStage(now + TimeUnit.DAYS.toMillis(1), now));
//
//        // expired -> EXPIRED
//        assertEquals(NotificationItem.Stage.EXPIRED,
//                NotificationsViewModel.calcStage(now - TimeUnit.DAYS.toMillis(1), now));
//
//        // far away -> NONE (e.g. 60 days)
//        assertEquals(NotificationItem.Stage.NONE,
//                NotificationsViewModel.calcStage(now + TimeUnit.DAYS.toMillis(60), now));
//    }
//    @Test
//    public void buildNotifications_addsTest_D14() {
//        long now = System.currentTimeMillis();
//        long oneYearMillis = TimeUnit.DAYS.toMillis(366);
//
//        // Make testEnd = now + 10 days  => Stage.D14 according to your calcStage tests
//        long testStart = now - (oneYearMillis - TimeUnit.DAYS.toMillis(10));
//
//        Driver driver = mock(Driver.class);
//
//        // Make sure only TEST creates a notification
//        when(driver.getCar().getInsuranceDateMillis()).thenReturn(0L);
//        when(driver.getCar().getTreatmentDateMillis()).thenReturn(0L);
//
//        when(driver.getCar().getTestDateMillis()).thenReturn(testStart);
//        when(driver.getCar().getDismissedTestStage()).thenReturn(null);
//
//        NotificationsViewModel vm = new NotificationsViewModel();
//        ArrayList<NotificationItem> list = vm.buildNotifications(driver);
//
//        assertEquals(1, list.size());
//        assertEquals(NotificationItem.Type.TEST, list.get(0).getType());
//        assertEquals(NotificationItem.Stage.D14, list.get(0).getStage());
//    }
//
//
//    private long testStartForEndInDays(long now, long daysUntilEnd) {
//        long oneYearMillis = TimeUnit.DAYS.toMillis(366);
//        long end = now + TimeUnit.DAYS.toMillis(daysUntilEnd);
//        return end - oneYearMillis; // start = end - 1 year
//    }
//
//    @Test
//    public void deferHidesNotification_thenNextStageShowsAgain() {
//        NotificationsViewModel vm = new NotificationsViewModel();
//        Driver driver = mock(Driver.class);
//        // keep only TEST active
//        when(driver.getCar().getInsuranceDateMillis()).thenReturn(0L);
//        when(driver.getCar().getTreatmentDateMillis()).thenReturn(0L);
//        long now = 1_700_000_000_000L; // fixed "now" for stable test
//        // Make TEST stage = D14 (end in 10 days)
//        long testStart = testStartForEndInDays(now, 10);
//        when(driver.getCar().getTestDateMillis()).thenReturn(testStart);
//        // 1) Before defer: dismissed is null -> should appear
//        when(driver.getCar().getDismissedTestStage()).thenReturn(null);
//        ArrayList<NotificationItem> list1 = vm.buildNotifications(driver, now);
//        assertEquals(1, list1.size());
//        assertEquals(NotificationItem.Type.TEST, list1.get(0).getType());
//        assertEquals(NotificationItem.Stage.D14, list1.get(0).getStage());
//        // 2) Simulate pressing "Defer": store dismissed stage = current stage name ("D14")
//        when(driver.getCar().getDismissedTestStage()).thenReturn(NotificationItem.Stage.D14.name());
//        ArrayList<NotificationItem> list2 = vm.buildNotifications(driver, now);
//        assertEquals(0, list2.size()); //  hidden after defer
//        // 3) Simulate time passing: now is later so stage becomes D7
//        // Example: move forward 8 days (10 days -> 2 days until end -> stage should be D7)
//        long nowLater = now + TimeUnit.DAYS.toMillis(8);
//        // dismissed is still D14, but current stage should become D7
//        ArrayList<NotificationItem> list3 = vm.buildNotifications(driver, nowLater);
//        assertEquals(1, list3.size()); //  shows again because stage changed
//        assertEquals(NotificationItem.Type.TEST, list3.get(0).getType());
//        assertEquals(NotificationItem.Stage.D7, list3.get(0).getStage());
//    }
//
//
//    private long monthsAgoMillis(int monthsAgo) {
//            Calendar cal = Calendar.getInstance();
//            cal.add(Calendar.MONTH, -monthsAgo);
//            return cal.getTimeInMillis();
//        }
//
//        @Test
//        public void calcTreatStage_returnsM6_M7_M8_Expired_None() {
//            long now = System.currentTimeMillis();
//
//            assertEquals(NotificationItem.Stage.M6,
//                    NotificationsViewModel.calcTreatStage(monthsAgoMillis(6), now));
//
//            assertEquals(NotificationItem.Stage.M7,
//                    NotificationsViewModel.calcTreatStage(monthsAgoMillis(7), now));
//
//            assertEquals(NotificationItem.Stage.M8,
//                    NotificationsViewModel.calcTreatStage(monthsAgoMillis(8), now));
//
//            assertEquals(NotificationItem.Stage.EXPIRED_TREAT,
//                    NotificationsViewModel.calcTreatStage(monthsAgoMillis(9), now));
//
//            assertEquals(NotificationItem.Stage.NONE,
//                    NotificationsViewModel.calcTreatStage(monthsAgoMillis(5), now));
//        }
//
//    @Test
//    public void buildNotifications_addsTreatment10k_M6() {
//        Driver driver = mock(Driver.class);
//
//        when(driver.getCar().getInsuranceDateMillis()).thenReturn(0L);
//        when(driver.getCar().getTestDateMillis()).thenReturn(0L);
//        when(driver.getCar().getTreatmentDateMillis()).thenReturn(monthsAgoMillis(6));
//        when(driver.getCar().getDismissedTreatment10kStage()).thenReturn(null);
//
//        NotificationsViewModel vm = new NotificationsViewModel();
//
//        ArrayList<NotificationItem> list = vm.buildNotifications(driver);
//
//        assertEquals(1, list.size());
//        assertEquals(NotificationItem.Type.TREATMENT_10K, list.get(0).getType());
//        assertEquals(NotificationItem.Stage.M6, list.get(0).getStage());
//    }
//
//
//}
