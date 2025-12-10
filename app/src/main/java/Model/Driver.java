package Model;

import android.os.Build;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class Driver {
    private String firstName;
    private String lastName;
    private String email;
    private String carNumber;
    private long insuranceDateMillis;   // התאריך נשמר במילישניות


    // Empty constructor for Firebase
    public Driver() {
    }

    public Driver(String firstName,
                  String lastName,
                  String email,
                  String carNumber,
                  long insuranceDateMillis) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.carNumber = carNumber;
        this.insuranceDateMillis = insuranceDateMillis;
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


    @Override
    public String toString() {
        return "Driver{" +
                "firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", email='" + email + '\'' +
                ", carNumber='" + carNumber + '\'' +
                ", insuranceDate='" + getFormattedInsuranceDate() + '\'' +
                '}';
    }

}
