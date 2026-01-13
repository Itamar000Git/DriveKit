package com.example.drive_kit.Model;

public class Car {

    private Double carNum;
    private String id;
    private String model;
    private CarModel carModel;
    private String plate;
    private String Nickname;
    private boolean manufacturer;
    private String carColor;
    private long carLicenseTestDate;
    private long insuranceDateMills;
    private int year;
    private String insuranceCompanyId;
    private String insuranceCompanyName;

    public Car() {}

    public Double getCarNum() {
        return carNum;
    }
    public String getNickname() {
        return Nickname;
    }
    public String getPlate() {
        return plate;
    }

    public void setPlate(String plate) {
        this.plate = plate;
    }

    public void setNickname(String nickname) {
        Nickname = nickname;
    }


    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setCarNum(Double carNum) {
        this.carNum = carNum;
    }
    public String getInsuranceCompanyId() {
        return insuranceCompanyId;
    }

    public void setInsuranceCompanyId(String insuranceCompanyId) {
        this.insuranceCompanyId = insuranceCompanyId;
    }


    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public CarModel getCarModel() {
        return carModel;
    }

    public void setCarModel(CarModel carModel) {
        this.carModel = carModel;
    }

    public String getCarColor() {
        return carColor;
    }

    public void setCarColor(String carColor) {
        this.carColor = carColor;
    }

    public long getCarLicenseTestDate() {
        return carLicenseTestDate;
    }

    public void setCarLicenseTestDate(long carLicenseTestDate) {
        this.carLicenseTestDate = carLicenseTestDate;
    }

    public long getInsuranceDateMills() {
        return insuranceDateMills;
    }

    public void setInsuranceDateMills(long insuranceDateMills) {
        this.insuranceDateMills = insuranceDateMills;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public boolean getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(boolean manufacturer) {
        this.manufacturer = manufacturer;
    }
}
