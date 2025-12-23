package com.example.drive_kit.Model;

import java.util.ArrayList;

public class InsuranceCompany {
    private String name;
    private Double licenseNumber;
    private String mail;
    //private ArrayList<Driver>contents;
    public InsuranceCompany() {
       // contents = new ArrayList<>();
    }
    public InsuranceCompany(String name,
                            Double licenseNumber,
                            String mail,
                            ArrayList<Driver> contents) {
        this.name = name;
        this.licenseNumber = licenseNumber;
        this.mail = mail;
        //this.contents = contents;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getLicenseNumber() {
        return licenseNumber;
    }

    public void setLicenseNumber(Double licenseNumber) {
        this.licenseNumber = licenseNumber;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    //public ArrayList<Driver> getContents() {
      //  return contents;
    //}

    //public void setContents(ArrayList<Driver> contents) {
        //this.contents = contents;
    //}

}

