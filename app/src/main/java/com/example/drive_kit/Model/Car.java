
package com.example.drive_kit.Model;


public class Car {

    // ====== Core fields ======
    private String carNumber;                // היה אצלך carNumber
    private long insuranceDateMillis;     // millis
    private long testDateMillis;          // millis
    private long treatmentDateMillis;     // millis

    private String dismissedInsuranceStage;
    private String dismissedTestStage;
    private String dismissedTreatment10kStage;

    // ====== Extra fields shown in UI / future use ======
    //private String id;                  // החלטה: לא בשימוש כרגע, נשאר בהערה
    private CarModel carModel;
    private String carSpecificModel;
    private String nickname;              // היה אצלך Nickname
    private String carColor;
    private int year;

    private String insuranceCompanyId;
    private String insuranceCompanyName;
    private String carImageUri;

    private String carImageBase64; // NEW: Firestore-friendly image string



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

    public String getCarNum() {
        return carNumber;
    }

    /**
     * FIX: היה אצלך באג - השמה ל-carNumber במקום לפרמטר.
     */
    public void setCarNum(String carNum) {
        this.carNumber = carNum;
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

    public long getTreatmentDateMillis() {
        return treatmentDateMillis;
    }

    public void setTreatmentDateMillis(long treatmentDateMillis) {
        this.treatmentDateMillis = treatmentDateMillis;
    }

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
    // אם יש לך כבר מסמכים ב-Firestore עם השמות האלה, או קוד ישן שקורא להם,
    // זה ימנע שבירה. אחרי שתיישרי מסד+קוד, אפשר להסיר.

    /**
     * תאימות לשדה הישן carNumber (אם עדיין מופיע במסד/קוד).
     * מומלץ לעבור ל-carNum.
     */
    @Deprecated
    public String getCarNumber() {
        return carNumber;
    }

    @Deprecated
    public void setCarNumber(String carNumber) {
        this.carNumber = carNumber;
    }



    @Deprecated
    public void setTreatDateMillis(long treatDateMillis) {
        this.treatmentDateMillis = treatDateMillis;
    }

    public String getCarImageBase64() {
        return carImageBase64;
    }

    public void setCarImageBase64(String carImageBase64) {
        this.carImageBase64 = carImageBase64;
    }

}
