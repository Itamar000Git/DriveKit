//package com.example.drive_kit.Model;
//
//import android.os.Build;
//
//import java.time.Instant;
//import java.time.LocalDate;
//import java.time.ZoneId;
//import java.time.format.DateTimeFormatter;
//import java.util.ArrayList;
//
//public class Driver {
//    private String firstName;
//    private String lastName;
//    private String email;
//    private String phone;
//    //private String carNumber;
//
//
//    //private ArrayList<Car> cars = new ArrayList<>();
//    private Car car;
////    private String dismissedInsuranceStage;
////    private String dismissedTestStage;
////    private String dismissedTreatment10kStage;
////    private long insuranceDateMillis; // in millis
////    private long testDateMillis;
////    private long treatmentDateMillis;
//
//    // Empty constructor for Firebase
//    public Driver() {
//    }
//    // Constructor with all fields
//    public Driver(String firstName,
//                  String lastName,
//                  String email,
//                  String phone,
//                  String carNumber,
//                  long insuranceDateMillis,
//                  long testDate,
//                  long treatDate) {
//        this.firstName = firstName;
//        this.lastName = lastName;
//        this.email = email;
//        this.phone = phone;
//        this.car = new Car( carNumber, insuranceDateMillis, testDate, treatDate); // Create a new instance of Car
////        this.carNumber = carNumber;
////        this.insuranceDateMillis = insuranceDateMillis;
////        this.testDateMillis = testDate;
////        this.treatmentDateMillis= treatDate;
//    }
//
//    // Getters and setters
//    public String getFirstName() {
//        return firstName;
//    }
//
//    public void setFirstName(String firstName) {
//        this.firstName = firstName;
//    }
//
//    public String getLastName() {
//        return lastName;
//    }
//
//    public void setLastName(String lastName) {
//        this.lastName = lastName;
//    }
//
//    public String getEmail() {
//        return email;
//    }
//    public String getPhone() {
//        return phone;
//    }
//    public void setPhone(String phone) {
//        this.phone = phone;
//    }
//
//
//    public void setEmail(String email) {
//        this.email = email;
//    }
//
//    public Car getCar() {
//        return car;
//    }
//
////    public String getCarNumber() {
////        return carNumber;
////    }
//
////    public void setCarNumber(String carNumber) {
////        this.carNumber = carNumber;
////    }
//
////    public long getInsuranceDateMillis() {
////        return insuranceDateMillis;
////    }
////    public void setInsuranceDateMillis(long insuranceDateMillis) {
////        this.insuranceDateMillis = insuranceDateMillis;
////    }
////    public long getTestDateMillis() {
////        return testDateMillis;
////    }
////
////    public void setTestDateMillis(long testDateMillis) {
////        this.testDateMillis = testDateMillis;
//////    }
////    public long getTreatmentDateMillis() {
////        return treatmentDateMillis;
////    }
////    public void setTreatDateMillis(long treatDateMillis) {
////        this.treatmentDateMillis = treatDateMillis;
////    }
////    public ArrayList<Car> getCars() {
////        return cars;
////    }
////
////    public void setCars(ArrayList<Car> cars) {
////        this.cars = cars;
////    }
////
////    public String getDismissedInsuranceStage() {
////        return dismissedInsuranceStage;
////    }
////    public void setDismissedInsuranceStage(String dismissedInsuranceStage) {
////        this.dismissedInsuranceStage = dismissedInsuranceStage;
////    }
//
////    public String getDismissedTestStage() {
////        return dismissedTestStage;
////    }
////    public void setDismissedTestStage(String dismissedTestStage) {
////        this.dismissedTestStage = dismissedTestStage;
////    }
////
////    public String getDismissedTreatment10kStage() {
////        return dismissedTreatment10kStage;
////    }
////
////    public void setDismissedTreatment10kStage(String dismissedTreatment10kStage) {
////        this.dismissedTreatment10kStage = dismissedTreatment10kStage;
////    }
//
//    // Returns the formatted insurance date
//    public String getFormattedInsuranceDate() {
//        if (car.getInsuranceDateMillis() <= 0) {
//            return "";
//        }
//        LocalDate date = null;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            date = Instant.ofEpochMilli(car.getInsuranceDateMillis())
//                    .atZone(ZoneId.systemDefault())
//                    .toLocalDate();
//        }
//        DateTimeFormatter formatter = null;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
//        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            return date.format(formatter);
//        }
//        return "";
//    }
//    // Returns the formatted test date
//    public String getFormattedTestDate() {
//        if (car.getTestDateMillis() <= 0) {
//            return "";
//        }
//        LocalDate date = null;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            date = Instant.ofEpochMilli(car.getTestDateMillis())
//                    .atZone(ZoneId.systemDefault())
//                    .toLocalDate();
//        }
//        DateTimeFormatter formatter = null;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
//        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            return date.format(formatter);
//        }
//        return "";
//    }
//    public String getFormattedTreatDate() {
//        if (car.getTreatmentDateMillis() <= 0) {
//            return "";
//        }
//        LocalDate date = null;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            date = Instant.ofEpochMilli(car.getTreatmentDateMillis())
//                    .atZone(ZoneId.systemDefault())
//                    .toLocalDate();
//        }
//        DateTimeFormatter formatter = null;
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
//        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            return date.format(formatter);
//        }
//        return "";
//    }
////    public void addCar(Car car) {
////        if (car == null) return;
////        if (cars == null) cars = new ArrayList<>();
////
////
////        if (car.getCarNum() != null && containsCarNumber(car.getCarNum())) {
////            return;
////        }
////        cars.add(car);
////    }
////    public boolean removeCarByNumber(Double carNum) {
////        if (cars == null || carNum == null) return false;
////
////        for (int i = 0; i < cars.size(); i++) {
////            Car c = cars.get(i);
////            if (c != null && carNum.equals(c.getCarNum())) {
////                cars.remove(i);
////                return true;
////            }
////        }
////        return false;
////    }
//
////    private boolean containsCarNumber(String carNum) {
////        if (cars == null) return false;
////        for (Car c : cars) {
////            if (c != null && c.getCarNum() != null && c.getCarNum().equals(carNum)) {
////                return true;
////            }
////        }
////        return false;
////    }
//
//    // toString method
//    @Override
//    public String toString() {
//        return "Driver{" +
//                "firstName='" + firstName + '\'' +
//                ", lastName='" + lastName + '\'' +
//                ", email='" + email + '\'' +
//                ", phone='" + phone + '\'' +
//                ", carNumber='" + car.getCarNum() + '\'' +
//                ", insuranceDate='" + getFormattedInsuranceDate() + '\'' +
//                ", testDate='" +  getFormattedTestDate() + '\'' +
//                ", treatmentDate='"+ getFormattedTreatDate()+ '\'' +
//                '}';
//    }
//
//}



package com.example.drive_kit.Model;

import android.os.Build;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Driver model for Firestore (נהג יחיד + רכב יחיד).
 *
 * החלטות/שינויים (בהתאם למה שביקשת):
 * 1) רכב יחיד: לנהג יש שדה אחד בשם car מסוג Car (לא רשימה).
 * 2) שמרתי על פונקציות formatted כי "נוח לראות".
 * 3) מניעת קריסות: Firestore יכול להחזיר Driver בלי car -> משתמשים ב-ensureCar().
 *
 * הערות:
 * - השדות הישנים (cars / תאריכים top-level / dismissed top-level) נשארו בהערות כמו אצלך,
 *   כדי שאם תחליטו לחזור/להשוות גרסאות יהיה קל.
 */
public class Driver {

    private String firstName;
    private String lastName;
    private String email;
    private String phone;

    // רכב יחיד
    private Car car;

    //private String carNumber;

    //private ArrayList<Car> cars = new ArrayList<>();
    //private String dismissedInsuranceStage;
    //private String dismissedTestStage;
    //private String dismissedTreatment10kStage;
    //private long insuranceDateMillis; // in millis
    //private long testDateMillis;
    //private long treatmentDateMillis;

    // Empty constructor for Firebase
    public Driver() {
        // Firestore fills fields
    }

    // Constructor with all fields
    public Driver(String firstName,
                  String lastName,
                  String email,
                  String phone,
                  String carNumber,
                  CarModel carModel,
                  int year,
                  long insuranceDateMillis,
                  long testDateMillis,
                  long treatDateMillis) {

        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;

        // Create a new instance of Car (רכב יחיד)
        this.car = new Car(carNumber,carModel,year, insuranceDateMillis, testDateMillis, treatDateMillis);
    }

    // ====== Helpers ======

    /**
     * Firestore יכול להחזיר Driver בלי car (null) -> לא מפילים מסכים.
     */
    private Car ensureCar() {
        if (car == null) car = new Car();
        return car;
    }

    // ====== Getters / Setters ======

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * חשוב ל-Firestore mapping: getter/setter לשדה car.
     */
    public Car getCar() {
        return ensureCar();
    }

    public void setCar(Car car) {
        this.car = car;
    }
//
//    // ====== Convenience methods (נוחים לשימוש במסכים/ריפוזיטורי) ======
//    // אם אתה לא רוצה אותם – אפשר למחוק, אבל הם מקלים מאוד כשעובדים עם רכב יחיד.
//
//    public String getCarNumber() {
//        return ensureCar().getCarNum();
//    }
//
//    public void setCarNumber(String carNumber) {
//        ensureCar().setCarNum(carNumber);
//    }
//
//    public long getInsuranceDateMillis() {
//        return ensureCar().getInsuranceDateMillis();
//    }
//
//    public void setInsuranceDateMillis(long insuranceDateMillis) {
//        ensureCar().setInsuranceDateMillis(insuranceDateMillis);
//    }
//
//    public long getTestDateMillis() {
//        return ensureCar().getTestDateMillis();
//    }
//
//    public void setTestDateMillis(long testDateMillis) {
//        ensureCar().setTestDateMillis(testDateMillis);
//    }
//
//    public long getTreatmentDateMillis() {
//        return ensureCar().getTreatmentDateMillis();
//    }
//
//    public void setTreatmentDateMillis(long treatmentDateMillis) {
//        ensureCar().setTreatmentDateMillis(treatmentDateMillis);
//    }
//
//    public String getDismissedInsuranceStage() {
//        return ensureCar().getDismissedInsuranceStage();
//    }
//
//    public void setDismissedInsuranceStage(String dismissedInsuranceStage) {
//        ensureCar().setDismissedInsuranceStage(dismissedInsuranceStage);
//    }
//
//    public String getDismissedTestStage() {
//        return ensureCar().getDismissedTestStage();
//    }
//
//    public void setDismissedTestStage(String dismissedTestStage) {
//        ensureCar().setDismissedTestStage(dismissedTestStage);
//    }
//
//    public String getDismissedTreatment10kStage() {
//        return ensureCar().getDismissedTreatment10kStage();
//    }
//
//    public void setDismissedTreatment10kStage(String dismissedTreatment10kStage) {
//        ensureCar().setDismissedTreatment10kStage(dismissedTreatment10kStage);
//    }
//
//    // ====== Formatted fields (נוח לראות) ======

    public String getFormattedInsuranceDate() {
        long millis = car.getInsuranceDateMillis();
        if (millis <= 0) return "";
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return "";

        LocalDate date = Instant.ofEpochMilli(millis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return date.format(formatter);
    }

    public String getFormattedTestDate() {
        long millis = car.getTestDateMillis();
        if (millis <= 0) return "";
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return "";

        LocalDate date = Instant.ofEpochMilli(millis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return date.format(formatter);
    }

    public String getFormattedTreatDate() {
        long millis = car.getTreatmentDateMillis();
        if (millis <= 0) return "";
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return "";

        LocalDate date = Instant.ofEpochMilli(millis)
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        return date.format(formatter);
    }

    // ====== toString ======

    @Override
    public String toString() {
        return "Driver{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", carNumber='" + car.getCarNum() + '\'' +
                ", carModel='" + car.getCarModel() + '\'' +
                ", insuranceDate='" + getFormattedInsuranceDate() + '\'' +
                ", testDate='" + getFormattedTestDate() + '\'' +
                ", treatmentDate='" + getFormattedTreatDate() + '\'' +
                '}';
    }
}
