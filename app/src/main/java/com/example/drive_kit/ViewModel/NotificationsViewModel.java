package com.example.drive_kit.ViewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.drive_kit.Data.Repository.NotificationsRepository;
import com.example.drive_kit.Model.Driver;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class NotificationsViewModel extends ViewModel {

    private final NotificationsRepository repo = new NotificationsRepository();

    private final MutableLiveData<ArrayList<String>> noty =
            new MutableLiveData<>(new ArrayList<>());

    public LiveData<ArrayList<String>> getNoty() {
        return noty;
    }

    public void loadNoty(String uid) {
        repo.getDriver(uid, new NotificationsRepository.DriverCallback() {
            @Override
            public void onSuccess(Driver driver) {
                noty.postValue(buildNotifications(driver));
            }

            @Override
            public void onError(Exception e) {
                noty.postValue(new ArrayList<>());
            }
        });
    }
    private ArrayList<String> buildNotifications(Driver driver) {
        ArrayList<String> list = new ArrayList<>();
        if (driver == null) return list;

        long now = System.currentTimeMillis();
        long oneYearMillis = TimeUnit.DAYS.toMillis(366);

        long insuranceStart = driver.getInsuranceDateMillis();
        if (insuranceStart > 0) {
            long insuranceEnd = insuranceStart + oneYearMillis;
            addExpiringMessages(list, insuranceEnd, now, "הביטוח");
        }

        long testStart = driver.getTestDateMillis();
        if (testStart > 0) {
            long testEnd = testStart + oneYearMillis;
            addExpiringMessages(list, testEnd, now, "הטסט");
        }

        return list;
    }

    private void addExpiringMessages(ArrayList<String> list, long endMillis, long now, String label) {
        long diffMillis = endMillis - now;
        long daysUntil = TimeUnit.MILLISECONDS.toDays(diffMillis);

        if (daysUntil <= 28 && daysUntil > 14) list.add("בעוד פחות מ 28 ימים יפוג תוקף " + label + " שלך");
        if (daysUntil <= 14 && daysUntil > 7)  list.add("בעוד פחות מ 14 ימים יפוג תוקף " + label + " שלך");
        if (daysUntil <= 7  && daysUntil > 1)  list.add("בעוד פחות מ 7 ימים יפוג תוקף " + label + " שלך");
        if (daysUntil == 1)                    list.add("בעוד יום אחד יפוג תוקף " + label + " שלך");
        if (daysUntil < 0)                     list.add("פג תוקף " + label + " שלך, נא לחדש בהקדם");
    }
}

//    auth = FirebaseAuth.getInstance();
//    db = FirebaseFirestore.getInstance();
//    FirebaseUser user = auth.getCurrentUser();
//    String uid = user.getUid();
//    db.collection("drivers")
//            .document(uid)
//                .get()
//                .addOnSuccessListener(documentSnapshot -> {
//        if (documentSnapshot.exists()) {
//            //converts the document to a driver object
//            Driver driver = documentSnapshot.toObject(Driver.class);
//
//            if (driver != null) {
//                currentInsuranceDate = driver.getInsuranceDateMillis();
//                currentTestDate= driver.getTestDateMillis();
//
//                Log.d("Notifications", "currentInsuranceDate = " + currentInsuranceDate);
//                Log.d("Notifications", "currentTestDate = " + currentTestDate);
//
//
//                if (currentInsuranceDate > 0) {
//                    long now = System.currentTimeMillis();
//
//                    long oneYearMillis = TimeUnit.DAYS.toMillis(366);
//
//                    long insuranceEndMillis = currentInsuranceDate + oneYearMillis;
//
//                    long diffMillis = insuranceEndMillis - now;
//                    long daysUntil = TimeUnit.MILLISECONDS.toDays(diffMillis);
//
//                    Log.d("Notifications", "daysUntil = " + daysUntil);
//                    // builds the notification list
//                    if (daysUntil <= 28 && daysUntil > 14) {
//                        noty.add("בעוד פחות מ 28 ימים יפוג תוקף הביטוח שלך");
//                    }
//                    if (daysUntil <= 14 && daysUntil > 7) {
//                        noty.add("בעוד פחות מ 14 ימים יפוג תוקף הביטוח שלך");
//                    }
//                    if (daysUntil <= 7 && daysUntil > 1) {
//                        noty.add("בעוד פחות מ 7 ימים יפוג תוקף הביטוח שלך");
//                    }
//                    if (daysUntil == 1) {
//                        noty.add("בעוד יום אחד יפוג תוקף הביטוח שלך");
//                    }
//                    if (daysUntil < 0) {
//                        noty.add("פג תוקף הביטוח שלך, נא לחדש את הביטוח בהקדם");
//                    }
//                }
//                if (currentTestDate > 0) {
//                    long now = System.currentTimeMillis();
//
//                    long oneYearMillis = TimeUnit.DAYS.toMillis(366);
//
//                    long dateEndMillis = currentTestDate + oneYearMillis;
//
//                    long diffMillis = dateEndMillis - now;
//                    long daysUntil = TimeUnit.MILLISECONDS.toDays(diffMillis);
//
//                    Log.d("Notifications", "daysUntil = " + daysUntil);
//                    // builds the notification list
//                    if (daysUntil <= 28 && daysUntil > 14) {
//                        noty.add("בעוד פחות מ 28 ימים יפוג תוקף הטסט שלך");
//                    }
//                    if (daysUntil <= 14 && daysUntil > 7) {
//                        noty.add("בעוד פחות מ 14 ימים יפוג תוקף הטסט שלך");
//                    }
//                    if (daysUntil <= 7 && daysUntil > 1) {
//                        noty.add("בעוד פחות מ 7 ימים יפוג תוקף הטסט שלך");
//                    }
//                    if (daysUntil == 1) {
//                        noty.add("בעוד יום אחד יפוג תוקף הטסט שלך");
//                    }
//                    if (daysUntil < 0) {
//                        noty.add("פג תוקף הביטוח שלך, נא לחדש את הטסט בהקדם");
//                    }
//                }
//                //showNotifications();
//            }
//        } else {
//
//        }
//    })
//            .addOnFailureListener(e -> {
//        //  welcomeText.setText("שגיאה בטעינת הנתונים");
//    });

