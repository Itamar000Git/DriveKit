
package com.example.drive_kit.Model;

/**
 * Represents a Car entity in the system.
 *
 * This class is used to store and transfer car-related data,
 * including identification details, maintenance dates, insurance info,
 * and UI-related fields.
 *
 * Compatible with Firestore serialization/deserialization.
 */
public class Car {

    // ====== Core fields ======
    private String carNumber;                // Unique car number (license plate)
    private long insuranceDateMillis;     // Insurance expiration date in milliseconds
    private long testDateMillis;          // Annual test (inspection) date in milliseconds
    private long treatmentDateMillis;     // 10,000 km treatment/service date in milliseconds

    private String dismissedInsuranceStage; // flag to mark dismissed insurance notification stage
    private String dismissedTestStage; // flag to mark dismissed test notification stage
    private String dismissedTreatment10kStage; // flag to mark dismissed treatment notification stage

    // ====== Extra fields shown in UI / future use ======
    private CarModel carModel; // General car manufacturer model
    private String carSpecificModel; // Specific model name (free text or API-based)
    private String nickname; // User-defined nickname for the car
    private String carColor; // Car color
    private int year; // Manufacturing year

    private String insuranceCompanyId; // Insurance company ID (Firestore reference)
    private String insuranceCompanyName; // Insurance company display name
    private String carImageUri; // Local or remote URI for car image

    private String carImageBase64; // Base64 representation of the car image (Firestore friendly)



    // Empty constructor for Firestore
    public Car() {
        // keep defaults, Firestore will fill values
        this.carModel = CarModel.UNKNOWN;
        this.carColor = "";
        this.carSpecificModel = "";
        this.nickname = "";
        this.year = 0;
        this.insuranceCompanyId = "";
        this.insuranceCompanyName = "";

    }
    /**
     * Full constructor for creating a Car object.
     *
     * @param carNum License plate number
     * @param carModel Car manufacturer model
     * @param year Manufacturing year
     * @param insuranceDateMillis Insurance date in millis
     * @param testDateMillis Test date in millis
     * @param treatmentDateMillis Treatment date in millis
     * @param carImageUri Image URI
     */
    public Car(String carNum,
               CarModel carModel,
               int year,
               long insuranceDateMillis,
               long testDateMillis,
               long treatmentDateMillis,
               String carImageUri) {

        this.carNumber = carNum;
        this.carModel = carModel;
        this.insuranceDateMillis = insuranceDateMillis;
        this.testDateMillis = testDateMillis;
        this.treatmentDateMillis = treatmentDateMillis;

        // Defaults
        this.year = year;
        this.carColor = "";
        this.carSpecificModel = "";
        this.nickname = "";
        this.insuranceCompanyId = "";
        this.insuranceCompanyName = "";
        this.carImageUri = carImageUri;
    }

    // ====== Getters / Setters (Bean-standard) ======

    /** @return car number */
    public String getCarNum() {
        return carNumber;
    }
    /** @param carNum license plate number */
    public void setCarNum(String carNum) {
        this.carNumber = carNum;
    }
    /** @return insurance date in millis */
    public long getInsuranceDateMillis() {
        return insuranceDateMillis;
    }
    /** @param insuranceDateMillis insurance date in millis */
    public void setInsuranceDateMillis(long insuranceDateMillis) {
        this.insuranceDateMillis = insuranceDateMillis;
    }
    /** @return test date in millis */
    public long getTestDateMillis() {
        return testDateMillis;
    }
    /** @param testDateMillis test date in millis */
    public void setTestDateMillis(long testDateMillis) {
        this.testDateMillis = testDateMillis;
    }
    /** @return treatment date in millis */
    public long getTreatmentDateMillis() {
        return treatmentDateMillis;
    }
    /** @param treatmentDateMillis treatment date in millis */
    public void setTreatmentDateMillis(long treatmentDateMillis) {
        this.treatmentDateMillis = treatmentDateMillis;
    }
    /** @return dismissed insurance stage */
    public String getDismissedInsuranceStage() {
        return dismissedInsuranceStage;
    }

    public void setDismissedInsuranceStage(String dismissedInsuranceStage) {
        this.dismissedInsuranceStage = dismissedInsuranceStage;
    }

    public String getDismissedTestStage() {
        return dismissedTestStage;
    }

    public void setDismissedTestStage(String dismissedTestStage) {
        this.dismissedTestStage = dismissedTestStage;
    }

    public String getDismissedTreatment10kStage() {
        return dismissedTreatment10kStage;
    }

    public void setDismissedTreatment10kStage(String dismissedTreatment10kStage) {
        this.dismissedTreatment10kStage = dismissedTreatment10kStage;
    }

    public CarModel getCarModel() {
        return carModel;
    }

    public String getCarSpecificModel() {
        return carSpecificModel;
    }

    public void setCarSpecificModel(String carSpecificModel) {
        this.carSpecificModel = carSpecificModel;
    }

    public void setCarModel(CarModel carModel) {
        this.carModel = carModel;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getCarColor() {
        return carColor;
    }

    public void setCarColor(String carColor) {
        this.carColor = carColor;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public String getInsuranceCompanyId() {
        return insuranceCompanyId;
    }

    public void setInsuranceCompanyId(String insuranceCompanyId) {
        this.insuranceCompanyId = insuranceCompanyId;
    }

    public String getInsuranceCompanyName() {
        return insuranceCompanyName;
    }

    public void setInsuranceCompanyName(String insuranceCompanyName) {
        this.insuranceCompanyName = insuranceCompanyName;
    }
    public void setCarImageUri(String carImageUri){
        this.carImageUri = carImageUri;
    }
    public String getCarImageUri(){
        return carImageUri;
    }


    // ====== Backward compatibility aliases (do NOT remove yet) ======
    /**
     * @deprecated Use getCarNum instead
     */
    @Deprecated
    public String getCarNumber() {
        return carNumber;
    }
    /**
     * @deprecated Use setCarNum instead
     */
    @Deprecated
    public void setCarNumber(String carNumber) {
        this.carNumber = carNumber;
    }

    /**
     * @deprecated Use setTreatmentDateMillis instead
     */
    @Deprecated
    public void setTreatDateMillis(long treatDateMillis) {
        this.treatmentDateMillis = treatDateMillis;
    }
    /** @return base64 image */
    public String getCarImageBase64() {
        return carImageBase64;
    }

    /** @param carImageBase64 base64 image string */
    public void setCarImageBase64(String carImageBase64) {
        this.carImageBase64 = carImageBase64;
    }

}
