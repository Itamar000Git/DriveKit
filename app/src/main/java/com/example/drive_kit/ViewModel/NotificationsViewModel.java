package com.example.drive_kit.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.drive_kit.Data.Repository.NotificationsRepository;
import com.example.drive_kit.Model.Driver;
import com.example.drive_kit.Model.NotificationItem;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * ViewModel for the NotificationsActivity.
 * It uses the NotificationsRepository to access the database.
 * It observes the LiveData in the ViewModel and updates the UI accordingly.
 * If the list is empty, it removes all views from the container.
 * Otherwise, it creates a new TextView for each notification and adds it to the container.
 */
public class NotificationsViewModel extends ViewModel {

    private final NotificationsRepository repo = new NotificationsRepository();//object for access to the database

    //private final MutableLiveData<ArrayList<String>> noty = new MutableLiveData<>(new ArrayList<>()); //list for notifications
    private final MutableLiveData<ArrayList<NotificationItem>> noty =
            new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> toastMessage = new MutableLiveData<>();

    //    public LiveData<ArrayList<String>> getNoty() {
//        return noty;
//    }
    public LiveData<ArrayList<NotificationItem>> getNoty() {
        return noty;
    }
    public LiveData<String> getToastMessage() {
        return toastMessage;
    }

    /**
     * loads the notifications for the given user
     * @param uid
     */
    public void loadNoty(String uid) {
        //check if the user is logged in
        repo.getDriver(uid, new NotificationsRepository.DriverCallback() {
            @Override
            public void onSuccess(Driver driver) {//if the user exists in the database, it loads the notifications
                noty.postValue(buildNotifications(driver)); //builds the notification list
            }

            @Override
            public void onError(Exception e) { //if the user does not exist in the database, it sets the list to empty
                noty.postValue(new ArrayList<>());
            }
        });
    }

    /**
     * builds the notification list based on the given driver object
     * if the driver object is null, it returns an empty list of notifications
     * @param driver
     * @return
     */
    private ArrayList<NotificationItem> buildNotifications(Driver driver) {
        ArrayList<NotificationItem> list = new ArrayList<>();
        if (driver == null) return list;

        long now = System.currentTimeMillis();
        long oneYearMillis = TimeUnit.DAYS.toMillis(366);

        // --- INSURANCE ---
        long insuranceStart = driver.getInsuranceDateMillis();
        if (insuranceStart > 0) {
            long insuranceEnd = insuranceStart + oneYearMillis;

            NotificationItem.Stage stage = calcStage(insuranceEnd, now);
            String dismissed = driver.getDismissedInsuranceStage();

            if (stage != NotificationItem.Stage.NONE &&
                    (dismissed == null || !stage.name().equals(dismissed))) {

                list.add(new NotificationItem(
                        NotificationItem.Type.INSURANCE,
                        stage,
                        messageFor("הביטוח", stage)
                ));
            }
        }

        // --- TEST ---
        long testStart = driver.getTestDateMillis();
        if (testStart > 0) {
            long testEnd = testStart + oneYearMillis;

            NotificationItem.Stage stage = calcStage(testEnd, now);
            String dismissed = driver.getDismissedTestStage(); // יכול להיות null

            if (stage != NotificationItem.Stage.NONE &&
                    (dismissed == null || !stage.name().equals(dismissed))) {

                list.add(new NotificationItem(
                        NotificationItem.Type.TEST,
                        stage,
                        messageFor("הטסט", stage)
                ));
            }
        }

        return list;
    }


//    /**
//     * adds the messages to the list based on the given parameters and the current time
//     * @param list
//     * @param endMillis
//     * @param now
//     * @param label
//     */
//    private void addExpiringMessages(ArrayList<String> list, long endMillis, long now, String label) {
//        long diffMillis = endMillis - now;
//        long daysUntil = TimeUnit.MILLISECONDS.toDays(diffMillis);
//
//        if (daysUntil <= 28 && daysUntil > 14) list.add("בעוד פחות מ 28 ימים יפוג תוקף " + label + " שלך");
//        if (daysUntil <= 14 && daysUntil > 7) list.add("בעוד פחות מ 14 ימים יפוג תוקף " + label + " שלך");
//        if (daysUntil <= 7  && daysUntil > 1) list.add("בעוד פחות מ 7 ימים יפוג תוקף " + label + " שלך");
//        if (daysUntil == 1) list.add("בעוד יום אחד יפוג תוקף " + label + " שלך");
//        if (daysUntil < 0) list.add("פג תוקף " + label + " שלך, נא לחדש בהקדם");
//    }

    private String messageFor(String label, NotificationItem.Stage stage) {
        switch (stage) {
            case D28: return "בעוד פחות מ 28 ימים יפוג תוקף " + label + " שלך";
            case D14: return "בעוד פחות מ 14 ימים יפוג תוקף " + label + " שלך";
            case D7:  return "בעוד פחות מ 7 ימים יפוג תוקף " + label + " שלך";
            case D1:  return "בעוד יום אחד יפוג תוקף " + label + " שלך";
            case EXPIRED: return "פג תוקף " + label + " שלך, נא לחדש בהקדם";
            default: return "";
        }
    }


    public static NotificationItem.Stage calcStage(long endMillis, long now) {
        long daysUntil = TimeUnit.MILLISECONDS.toDays(endMillis - now);

        if (daysUntil <= 28 && daysUntil > 14) return NotificationItem.Stage.D28;
        if (daysUntil <= 14 && daysUntil > 7)  return NotificationItem.Stage.D14;
        if (daysUntil <= 7  && daysUntil > 1)  return NotificationItem.Stage.D7;
        if (daysUntil == 1)                    return NotificationItem.Stage.D1;
        if (daysUntil < 0)                     return NotificationItem.Stage.EXPIRED;

        return NotificationItem.Stage.NONE;
    }

//
//    public void deferNotification(String uid, NotificationItem item) {
//        repo.saveDismissStage(
//                uid,
//                item.getType(),
//                item.getStage(),
//                new NotificationsRepository.SimpleCallback() {
//                    @Override
//                    public void onSuccess() {
//                        ArrayList<NotificationItem> current = noty.getValue();
//                        if (current == null) return;
//
//                        ArrayList<NotificationItem> updated = new ArrayList<>(current);
//                        updated.remove(item);
//
//                        noty.postValue(updated);
//                        toastMessage.postValue("ההתראה נדחתה לשלב הבא");
//                        toastMessage.postValue(null);
//
//                    }
//
//                    @Override
//                    public void onError(Exception e) {
//                        toastMessage.postValue("שגיאה בדחיית ההתראה");
//                        toastMessage.postValue(null);
//
//                    }
//                }
//        );
//    }

    public void deferNotification(String uid, NotificationItem item) {
        repo.deferNotification(uid, item, new NotificationsRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                ArrayList<NotificationItem> current = noty.getValue();
                if (current == null) return;

                ArrayList<NotificationItem> updated = new ArrayList<>(current);
                updated.remove(item);
                noty.postValue(updated);
            }

            @Override
            public void onError(Exception e) {

            }
        });
    }



}
