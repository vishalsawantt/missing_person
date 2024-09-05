package com.example.missingperson;

public class User {
    private String fullName;
    private String mobileNumber;
    private String address;
    private String email;
    private String spinnerGender;
    private String userId;
    private String imgurl;

    public User() {}

    public User(String fullName, String mobileNumber, String address, String email, String spinnerGender, String imgurl, String userId) {
        this.fullName = fullName;
        this.mobileNumber = mobileNumber;
        this.address = address;
        this.email = email;
        this.spinnerGender = spinnerGender;
        this.imgurl = imgurl;
        this.userId = userId;
    }

    // Getters and setters
    public String getUserId() {return userId;}
    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getSpinnerGender() {
        return spinnerGender;
    }

    public void setSpinnerGender(String spinnerGender) {
        this.spinnerGender = spinnerGender;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getImgurl() {
        return imgurl;
    }

    public void setImgurl(String imgurl) {
        this.imgurl = imgurl;
    }
}
