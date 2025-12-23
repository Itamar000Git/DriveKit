package com.example.drive_kit.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.drive_kit.Data.Repository.NotificationsRepository;
import com.example.drive_kit.Model.Driver;

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

    private final MutableLiveData<ArrayList<String>> noty = new MutableLiveData<>(new ArrayList<>()); //list for notifications

    public LiveData<ArrayList<String>> getNoty() {
        return noty;
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
    private ArrayList<String> buildNotifications(Driver driver) {
        ArrayList<String> list = new ArrayList<>();
        if (driver == null) return list;

        long now = System.currentTimeMillis(); // get the current time in milliseconds
        long oneYearMillis = TimeUnit.DAYS.toMillis(366); // calculate the number of milliseconds in a year


        long insuranceStart = driver.getInsuranceDateMillis(); // get the insurance start date
        if (insuranceStart > 0) {
            long insuranceEnd = insuranceStart + oneYearMillis; // calculate the insurance end date
            addExpiringMessages(list, insuranceEnd, now, "הביטוח"); // add the messages to the list
        }

        long testStart = driver.getTestDateMillis(); // get the test start date
        if (testStart > 0) {
            long testEnd = testStart + oneYearMillis; // calculate the test end date
            addExpiringMessages(list, testEnd, now, "הטסט"); // add the messages to the list
        }

        return list; // return the list
    }

    /**
     * adds the messages to the list based on the given parameters and the current time
     * @param list
     * @param endMillis
     * @param now
     * @param label
     */
    private void addExpiringMessages(ArrayList<String> list, long endMillis, long now, String label) {
        long diffMillis = endMillis - now;
        long daysUntil = TimeUnit.MILLISECONDS.toDays(diffMillis);

        if (daysUntil <= 28 && daysUntil > 14) list.add("בעוד פחות מ 28 ימים יפוג תוקף " + label + " שלך");
        if (daysUntil <= 14 && daysUntil > 7) list.add("בעוד פחות מ 14 ימים יפוג תוקף " + label + " שלך");
        if (daysUntil <= 7  && daysUntil > 1) list.add("בעוד פחות מ 7 ימים יפוג תוקף " + label + " שלך");
        if (daysUntil == 1) list.add("בעוד יום אחד יפוג תוקף " + label + " שלך");
        if (daysUntil < 0) list.add("פג תוקף " + label + " שלך, נא לחדש בהקדם");
    }
}
