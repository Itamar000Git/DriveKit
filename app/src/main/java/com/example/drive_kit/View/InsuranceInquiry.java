package com.example.drive_kit.View;

import com.google.firebase.Timestamp;

public class InsuranceInquiry {
    private String companyId;
    private String driverUid;
    private String driverName;
    private String driverPhone;
    private String driverEmail;
    private String carNumber;
    private String carModel;
    private String message;
    private String status;      // "new" / "contacted"
    private Timestamp createdAt;
    private Timestamp updatedAt;

    // Required empty constructor for Firestore
    public InsuranceInquiry() {}

    public InsuranceInquiry(String companyId,
                            String driverUid,
                            String driverName,
                            String driverPhone,
                            String driverEmail,
                            String carNumber,
                            String carModel,
                            String message,
                            String status,
                            Timestamp createdAt,
                            Timestamp updatedAt) {
        this.companyId = companyId;
        this.driverUid = driverUid;
        this.driverName = driverName;
        this.driverPhone = driverPhone;
        this.driverEmail = driverEmail;
        this.carNumber = carNumber;
        this.carModel = carModel;
        this.message = message;
        this.status = status;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getCompanyId() { return companyId; }
    public String getDriverUid() { return driverUid; }
    public String getDriverName() { return driverName; }
    public String getDriverPhone() { return driverPhone; }
    public String getDriverEmail() { return driverEmail; }
    public String getCarNumber() { return carNumber; }
    public String getCarModel() { return carModel; }
    public String getMessage() { return message; }
    public String getStatus() { return status; }
    public Timestamp getCreatedAt() { return createdAt; }
    public Timestamp getUpdatedAt() { return updatedAt; }

    public void setCompanyId(String companyId) { this.companyId = companyId; }
    public void setDriverUid(String driverUid) { this.driverUid = driverUid; }
    public void setDriverName(String driverName) { this.driverName = driverName; }
    public void setDriverPhone(String driverPhone) { this.driverPhone = driverPhone; }
    public void setDriverEmail(String driverEmail) { this.driverEmail = driverEmail; }
    public void setCarNumber(String carNumber) { this.carNumber = carNumber; }
    public void setCarModel(String carModel) { this.carModel = carModel; }
    public void setMessage(String message) { this.message = message; }
    public void setStatus(String status) { this.status = status; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }
}
