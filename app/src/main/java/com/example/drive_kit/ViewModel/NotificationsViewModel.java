package com.example.drive_kit.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.drive_kit.Data.Repository.NotificationsRepository;
import com.example.drive_kit.Model.Driver;
import com.example.drive_kit.Model.NotificationItem;

import java.util.ArrayList;
import java.util.Calendar;
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
    private final MutableLiveData<ArrayList<NotificationItem>> noty =
            new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> toastMessage = new MutableLiveData<>();
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
    public ArrayList<NotificationItem> buildNotifications(Driver driver) {
        return buildNotifications(driver, System.currentTimeMillis());
    }

    public ArrayList<NotificationItem> buildNotifications(Driver driver, long now) {

        ArrayList<NotificationItem> list = new ArrayList<>();
        if (driver == null) return list;

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
            String dismissed = driver.getDismissedTestStage();

            if (stage != NotificationItem.Stage.NONE &&
                    (dismissed == null || !stage.name().equals(dismissed))) {

                list.add(new NotificationItem(
                        NotificationItem.Type.TEST,
                        stage,
                        messageFor("הטסט", stage)
                ));
            }
        }

        // --- TREATMENT 10K ---
        long treatStart = driver.getTreatmentDateMillis();
        if (treatStart > 0) {

            NotificationItem.Stage stage = calcTreatStage(treatStart, now);
            String dismissed = driver.getDismissedTreatment10kStage();

            if (stage != NotificationItem.Stage.NONE &&
                    (dismissed == null || !stage.name().equals(dismissed))) {

                list.add(new NotificationItem(
                        NotificationItem.Type.TREATMENT_10K,
                        stage,
                        messageFor("טיפול 10K", stage)
                ));
            }
        }

        return list;
    }


    /**
     * builds the message for the given notification item
     * based on the given label and stage
     * @param label
     * @param stage
     * @return
     */
    private String messageFor(String label, NotificationItem.Stage stage) {
        switch (stage) {
            case D28: return "בעוד פחות מ 28 ימים יפוג תוקף " + label + " שלך";
            case D14: return "בעוד פחות מ 14 ימים יפוג תוקף " + label + " שלך";
            case D7:  return "בעוד פחות מ 7 ימים יפוג תוקף " + label + " שלך";
            case D1:  return "בעוד יום אחד יפוג תוקף " + label + " שלך";
            case EXPIRED: return "פג תוקף " + label + " שלך, נא לחדש בהקדם";
            case M6: return "עברו 6 חודשים מהטיפול האחרון. מומלץ לקבוע טיפול 10K";
            case M7: return "עברו 7 חודשים מהטיפול האחרון. מומלץ לקבוע טיפול 10K";
            case M8: return "עברו 8 חודשים מהטיפול האחרון. מומלץ לקבוע טיפול 10K";
            case EXPIRED_TREAT: return "עברו 9 חודשים מהטיפול האחרון. פג תוקף טיפול 10K, נא לטפל בהקדם";

            default: return "";
        }
    }


    /**
     * calculates the stage of the given notification item based on the given end date and now
     * and returns the corresponding stage
     * @param endMillis
     * @param now
     * @return
     */
    public static NotificationItem.Stage calcStage(long endMillis, long now) {
        long daysUntil = TimeUnit.MILLISECONDS.toDays(endMillis - now);

        if (daysUntil <= 28 && daysUntil > 14) return NotificationItem.Stage.D28;
        if (daysUntil <= 14 && daysUntil > 7) return NotificationItem.Stage.D14;
        if (daysUntil <= 7  && daysUntil > 1) return NotificationItem.Stage.D7;
        if (daysUntil == 1) return NotificationItem.Stage.D1;
        if (daysUntil < 0) return NotificationItem.Stage.EXPIRED;

        return NotificationItem.Stage.NONE;
    }

    /**
     * calculates the number of full months between the given start and end dates
     * @param startMillis
     * @param endMillis
     * @return
     */
    private static int fullMonthsBetween(long startMillis, long endMillis) {
        Calendar start = Calendar.getInstance();
        start.setTimeInMillis(startMillis);

        Calendar end = Calendar.getInstance();
        end.setTimeInMillis(endMillis);

        int startTotal = start.get(Calendar.YEAR) * 12 + start.get(Calendar.MONTH);
        int endTotal   = end.get(Calendar.YEAR)   * 12 + end.get(Calendar.MONTH);

        int diff = endTotal - startTotal;

        if (end.get(Calendar.DAY_OF_MONTH) < start.get(Calendar.DAY_OF_MONTH)) {
            diff--;
        }

        return Math.max(diff, 0);
    }


    /**
     * @param treatStartMillis
     * @param now
     * @return
     */
    public static NotificationItem.Stage calcTreatStage(long treatStartMillis, long now) {
        int monthsSince = fullMonthsBetween(treatStartMillis, now);

        if (monthsSince >= 9) return NotificationItem.Stage.EXPIRED_TREAT;
        if (monthsSince == 8) return NotificationItem.Stage.M8;
        if (monthsSince == 7) return NotificationItem.Stage.M7;
        if (monthsSince == 6) return NotificationItem.Stage.M6;

        return NotificationItem.Stage.NONE;
    }


    /**
     * defers the given notification item for the given user
     * and removes it from the list of notifications
     * @param uid
     * @param item
     */
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

    /**
     * updates the service date for the given notification item for the given user
     * and updates the list of notifications
     * @param uid
     * @param item
     * @param newDateMillis
     */
    public void doneNotification(String uid, NotificationItem item, long newDateMillis) {
        repo.doneButton(uid, item.getType(), newDateMillis, new NotificationsRepository.SimpleCallback() {
            @Override
            public void onSuccess() {
                toastMessage.postValue("עודכן בהצלחה");
                loadNoty(uid);
                toastMessage.postValue(null);
            }
            @Override
            public void onError(Exception e) {
                toastMessage.postValue("שגיאה בעדכון התאריך");
                toastMessage.postValue(null);
            }
        });
    }




}
