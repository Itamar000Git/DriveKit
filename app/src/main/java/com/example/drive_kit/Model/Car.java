//package com.example.drive_kit.Model;
//
//public class Car {
//
//    private String carNumber;
//    //private String id;
//    //private String model;
//    private CarModel carModel;
//    //private String plate;
//    private String Nickname;
//    //private boolean manufacturer;
//    private String carColor;
//    //private long carLicenseTestDate;
//    //private long insuranceDateMills;
//    private int year;
//    private String insuranceCompanyId;
//    private String insuranceCompanyName;
//    private String dismissedInsuranceStage;
//    private String dismissedTestStage;
//    private String dismissedTreatment10kStage;
//    private long insuranceDateMillis; // in millis
//    private long testDateMillis;
//    private long treatmentDateMillis;
//
//    public Car(String carNumber,
//               long insuranceDateMillis,
//               long testDateMillis,
//               long treatDateMillis) {
//        this.carNumber = carNumber;
//        this.carModel = CarModel.UNKNOWN;
//        this.carColor = "";
//        this.year = 2023;
//        this.insuranceCompanyId = "";
//        this.insuranceCompanyName = "";
//        this.insuranceDateMillis = insuranceDateMillis;
//        this.testDateMillis = testDateMillis;
//        this.treatmentDateMillis = treatDateMillis;
//        this.Nickname = "";
//
//    }
//    public Car() {}
//
//    public String getCarNum() {
//        return carNumber;
//    }
//
//    public String getNickname() {
//        return Nickname;
//    }
////    public String getPlate() {
////        return plate;
////    }
//
////    public void setPlate(String plate) {
////        this.plate = plate;
////    }
//
////    public void setNickname(String nickname) {
////        Nickname = nickname;
////    }
//
//
////    public String getId() {
////        return id;
////    }
////
////    public void setId(String id) {
////        this.id = id;
////    }
//
//    public void setCarNum(String carNum) {
//        this.carNumber = carNum;
//    }
//    public String getInsuranceCompanyId() {
//        return insuranceCompanyId;
//    }
//
//    public void setInsuranceCompanyId(String insuranceCompanyId) {
//        this.insuranceCompanyId = insuranceCompanyId;
//    }
//
//
////    public String getModel() {
////        return model;
////    }
////
////    public void setModel(String model) {
////        this.model = model;
////    }
//
//    public CarModel getCarModel() {
//        return carModel;
//    }
//
//    public void setCarModel(CarModel carModel) {
//        this.carModel = carModel;
//    }
//
//    public String getCarColor() {
//        return carColor;
//    }
//
//    public void setCarColor(String carColor) {
//        this.carColor = carColor;
//    }
//
//    public long getTestDateMillis() {
//        return testDateMillis;
//    }
//
//    public void setTestDateMillis(long testDateMillis) {
//        this.testDateMillis = testDateMillis;
//    }
//
//    public long getInsuranceDateMillis() {
//        return insuranceDateMillis;
//    }
//
//    public void setInsuranceDateMillis(long insuranceDateMills) {
//        this.insuranceDateMillis = insuranceDateMills;
//    }
//
//    public long getTreatmentDateMillis() {
//        return treatmentDateMillis;
//    }
//    public void setTreatDateMillis(long treatDateMillis) {
//        this.treatmentDateMillis = treatDateMillis;
//    }
//
//
//    public String getDismissedInsuranceStage() {
//        return dismissedInsuranceStage;
//    }
//    public void setDismissedInsuranceStage(String dismissedInsuranceStage) {
//        this.dismissedInsuranceStage = dismissedInsuranceStage;
//    }
//
//    public String getDismissedTestStage() {
//        return dismissedTestStage;
//    }
//    public void setDismissedTestStage(String dismissedTestStage) {
//        this.dismissedTestStage = dismissedTestStage;
//    }
//
//    public String getDismissedTreatment10kStage() {
//        return dismissedTreatment10kStage;
//    }
//
//    public void setDismissedTreatment10kStage(String dismissedTreatment10kStage) {
//        this.dismissedTreatment10kStage = dismissedTreatment10kStage;
//    }
//
//
//    public int getYear() {
//        return year;
//    }
//
//    public void setYear(int year) {
//        this.year = year;
//    }
//
////    public boolean getManufacturer() {
////        return manufacturer;
////    }
//
////    public void setManufacturer(boolean manufacturer) {
////        this.manufacturer = manufacturer;
////    }
//}


package com.example.drive_kit.Model;

/**
 * Car model for Firestore.
 *
 * החלטות/שינויים (נשמרו + עודכנו):
 * 1) תיקון Naming ל-Bean תקני כדי ש-Firestore ימפה אוטומטית:
 *    - Nickname (N גדולה) -> nickname
 *    - carNumber -> carNum (כשם שדה פנימי עקבי עם getter/setter)
 *    - setCarNum היה עם באג (השמה לעצמו) -> תוקן
 *
 * 2) שמרתי על תאימות אחורה:
 *    - הוספתי "גשרים" (aliases) לשמות ישנים עם הערות @Deprecated
 *      כדי שגם אם במסד עדיין יש שדות ישנים/קריאות ישנות בקוד – זה לא ישבור.
 *
 * 3) לא הורדתי שום שדה רלוונטי.
 *    שדות שלא בשימוש כרגע נשארו (insuranceCompanyName וכו').
 *
 * הערה חשובה:
 * - כרגע זה רכב יחיד בתוך Driver (שדה: car). עדיין טוב להשאיר את המודל נקי וממופה נכון.
 */
public class Car {

    // ====== Core fields ======
    private String carNum;                // היה אצלך carNumber
    private long insuranceDateMillis;     // millis
    private long testDateMillis;          // millis
    private long treatmentDateMillis;     // millis

    private String dismissedInsuranceStage;
    private String dismissedTestStage;
    private String dismissedTreatment10kStage;

    // ====== Extra fields shown in UI / future use ======
    //private String id;                  // החלטה: לא בשימוש כרגע, נשאר בהערה
    private CarModel carModel;
    private String nickname;              // היה אצלך Nickname
    private String carColor;
    private int year;

    private String insuranceCompanyId;
    private String insuranceCompanyName;

    // Empty constructor for Firestore
    public Car() {
        // keep defaults, Firestore will fill values
        this.carModel = CarModel.UNKNOWN;
        this.carColor = "";
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
               long treatmentDateMillis) {

        this.carNum = carNum;
        this.carModel = carModel;
        this.insuranceDateMillis = insuranceDateMillis;
        this.testDateMillis = testDateMillis;
        this.treatmentDateMillis = treatmentDateMillis;

        // Defaults
        this.year = year;
        this.carColor = "";
        this.nickname = "";
        this.insuranceCompanyId = "";
        this.insuranceCompanyName = "";
    }

    // ====== Getters / Setters (Bean-standard) ======

    public String getCarNum() {
        return carNum;
    }

    /**
     * FIX: היה אצלך באג - השמה ל-carNumber במקום לפרמטר.
     */
    public void setCarNum(String carNum) {
        this.carNum = carNum;
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

    // ====== Backward compatibility aliases (do NOT remove yet) ======
    // אם יש לך כבר מסמכים ב-Firestore עם השמות האלה, או קוד ישן שקורא להם,
    // זה ימנע שבירה. אחרי שתיישרי מסד+קוד, אפשר להסיר.

    /**
     * תאימות לשדה הישן carNumber (אם עדיין מופיע במסד/קוד).
     * מומלץ לעבור ל-carNum.
     */
    @Deprecated
    public String getCarNumber() {
        return carNum;
    }

    @Deprecated
    public void setCarNumber(String carNumber) {
        this.carNum = carNumber;
    }


    /**
     * שם ישן שהיה אצלך עבור טיפול (Treat).
     * מומלץ לעבור ל-setTreatmentDateMillis.
     */
    @Deprecated
    public void setTreatDateMillis(long treatDateMillis) {
        this.treatmentDateMillis = treatDateMillis;
    }
}
