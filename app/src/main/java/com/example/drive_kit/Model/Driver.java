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

import android.net.Uri;
import android.os.Build;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * Represents a Driver entity in the system.
 *
 * This model is designed for Firestore usage and represents:
 * - A single driver
 * - A single associated car (1:1 relationship)
 *
 * Key design decisions:
 * 1. Driver contains exactly one Car object (not a list).
 * 2. Includes formatted date helpers for UI convenience.
 * 3. Safe access to Car via ensureCar() to prevent null crashes from Firestore.
 *
 * Notes:
 * - Firestore may return partial objects (e.g., Driver without car),
 *   therefore all car access should go through ensureCar().
 */
public class Driver {

    private String firstName; // Driver first name
    private String lastName; // Driver last name
    private String email; // Driver email address
    private String phone; // Driver phone number
    private Car car; // Associated car object (can be null when loaded from Firestore)


    // Empty constructor for Firebase
    public Driver() {
        // Firestore fills fields
    }

    /**
     * Full constructor for creating a Driver with a Car.
     *
     * @param firstName driver's first name
     * @param lastName driver's last name
     * @param email driver's email
     * @param phone driver's phone number
     * @param carNumber car license plate
     * @param carModel car manufacturer
     * @param year car manufacturing year
     * @param insuranceDateMillis insurance expiration date (millis)
     * @param testDateMillis annual test date (millis)
     * @param treatDateMillis treatment/service date (millis)
     * @param carImageUri image URI of the car
     */
    public Driver(String firstName,
                  String lastName,
                  String email,
                  String phone,
                  String carNumber,
                  CarModel carModel,
                  int year,
                  long insuranceDateMillis,
                  long testDateMillis,
                  long treatDateMillis
                  ,String carImageUri // Firestore friendly
        ) {

        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;

        // Create a new instance of Car
        this.car = new Car(carNumber,carModel,year, insuranceDateMillis, testDateMillis, treatDateMillis,carImageUri);
    }

    // ====== Helpers ======
    /**
     * Ensures that the car object is never null.
     *
     * This prevents NullPointerExceptions when accessing car fields,
     * especially when Firestore returns a Driver without a car.
     *
     * @return non-null Car instance
     */
    private Car ensureCar() {
        if (car == null) car = new Car();
        return car;
    }

    // ====== Getters / Setters ======
    /** @return driver's first name */
    public String getFirstName() {
        return firstName;
    }

    /** @param firstName driver's first name */
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /** @return driver's last name */
    public String getLastName() {
        return lastName;
    }

    /** @param lastName driver's last name */
    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /** @return driver's email */
    public String getEmail() {
        return email;
    }

    /** @param email driver's email */
    public void setEmail(String email) {
        this.email = email;
    }

    /** @return driver's phone number */
    public String getPhone() {
        return phone;
    }

    /** @param phone driver's phone number */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Returns the driver's car.
     * Always safe (never null).
     *
     * @return Car instance
     */
    public Car getCar() {
        return ensureCar();
    }

    /**
     * Sets the driver's car.
     *
     * @param car Car object
     */
    public void setCar(Car car) {
        this.car = car;
    }


    /**
     * @return car image URI
     */
    public String getCarImageUri() {
        return ensureCar().getCarImageUri();
    }

    /**
     * @param carImageUri URI of the car image
     */
    public void setCarImageUri(String carImageUri) {
        ensureCar().setCarImageUri(carImageUri);
    }


    // ====== Formatted Fields (UI helpers) ======

    /**
     * Formats insurance date to dd/MM/yyyy.
     *
     * @return formatted date or empty string if invalid
     */
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

    /**
     * Formats test date to dd/MM/yyyy.
     *
     * @return formatted date or empty string if invalid
     */
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

    /**
     * Formats treatment date to dd/MM/yyyy.
     *
     * @return formatted date or empty string if invalid
     */
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

    /**
     * Returns a readable string representation of the Driver.
     *
     * @return string with driver and car details
     */
    @Override
    public String toString() {
        return "Driver{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", carNumber='" + car.getCarNum() + '\'' +
                ", carModel='" + car.getCarModel() + '\'' +
                ", carSpecificModel='" + car.getCarSpecificModel() + '\'' +
                ", insuranceDate='" + getFormattedInsuranceDate() + '\'' +
                ", testDate='" + getFormattedTestDate() + '\'' +
                ", treatmentDate='" + getFormattedTreatDate() + '\'' +
                '}';
    }
}
