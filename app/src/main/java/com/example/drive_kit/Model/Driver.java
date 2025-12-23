package com.example.drive_kit.Model;

import android.os.Build;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class Driver {
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String carNumber;
    private long insuranceDateMillis; // in millis
    private long testDateMillis;

    private ArrayList<Car> cars = new ArrayList<>();

    // Empty constructor for Firebase
    public Driver() {
    }
    // Constructor with all fields
    public Driver(String firstName,
                  String lastName,
                  String email,
                  String phone,
                  String carNumber,
                  long insuranceDateMillis,
                  long testDate) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.carNumber = carNumber;
        this.insuranceDateMillis = insuranceDateMillis;
        this.testDateMillis = testDate;
    }

    // Getters and setters
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
    public String getPhone() {
        return phone;
    }
    public void setPhone(String phone) {
        this.phone = phone;
    }


    public void setEmail(String email) {
        this.email = email;
    }

    public String getCarNumber() {
        return carNumber;
    }

    public void setCarNumber(String carNumber) {
        this.carNumber = carNumber;
    }

    public long getInsuranceDateMillis() {
        return insuranceDateMillis;
    }

    public void setInsuranceDateMillis(long insuranceDateMillis) {
        this.insuranceDateMillis = insuranceDateMillis;
    }
    public long getTestDateMillis() {
        return testDateMillis;
    }

    public void setTestDateMillis(long testDateMillis) {
        this.testDateMillis = testDateMillis;
    }
    public ArrayList<Car> getCars() {
        return cars;
    }

    public void setCars(ArrayList<Car> cars) {
        this.cars = cars;
    }


    // Returns the formatted insurance date
    public String getFormattedInsuranceDate() {
        if (insuranceDateMillis <= 0) {
            return "";
        }
        LocalDate date = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            date = Instant.ofEpochMilli(insuranceDateMillis)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        }
        DateTimeFormatter formatter = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return date.format(formatter);
        }
        return "";
    }
    // Returns the formatted test date
    public String getFormattedTestDate() {
        if (testDateMillis <= 0) {
            return "";
        }
        LocalDate date = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            date = Instant.ofEpochMilli(testDateMillis)
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();
        }
        DateTimeFormatter formatter = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return date.format(formatter);
        }
        return "";
    }
    public void addCar(Car car) {
        if (car == null) return;
        if (cars == null) cars = new ArrayList<>();


        if (car.getCarNum() != null && containsCarNumber(car.getCarNum())) {
            return;
        }
        cars.add(car);
    }
    public boolean removeCarByNumber(Double carNum) {
        if (cars == null || carNum == null) return false;

        for (int i = 0; i < cars.size(); i++) {
            Car c = cars.get(i);
            if (c != null && carNum.equals(c.getCarNum())) {
                cars.remove(i);
                return true;
            }
        }
        return false;
    }

    private boolean containsCarNumber(Double carNum) {
        if (cars == null) return false;
        for (Car c : cars) {
            if (c != null && c.getCarNum() != null && c.getCarNum().equals(carNum)) {
                return true;
            }
        }
        return false;
    }

    // toString method
    @Override
    public String toString() {
        return "Driver{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", carNumber='" + carNumber + '\'' +
                ", insuranceDate='" + getFormattedInsuranceDate() + '\'' +
                ", testDate='" +  getFormattedTestDate() + '\'' +
                '}';
    }

}
