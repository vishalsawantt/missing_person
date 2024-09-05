package com.example.missingperson;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.database.PropertyName;

import java.util.List;

public class UserReport implements Parcelable {
    private String name;
    private String gender;
    private String age;
    private String missdate; // Assuming this is selectedDate based on your usage
    private String contact;
    private String lastlocation;
    private String clothdes;
    private String policreportno;
    private String relationship;
    private String reward;
    private double latitude;
    private double longitude;
    private String userUid;
    private String timestamp; // Add timestamp field
    private String imageUrl;
    private String reportUid;
    @PropertyName("seenDetails")
    private List<SeenDetails> seenDetailsList;
    public UserReport() {}

    public UserReport(String reportUid, String name, String gender, String age, String missdate, String contact, String lastlocation, String clothdes, String policreportno, String relationship, String reward, double latitude, double longitude, String userUid, String imageUrl, String timestamp) {
        this.reportUid = reportUid;
        this.name = name;
        this.gender = gender;
        this.age = age;
        this.missdate = missdate;
        this.contact = contact;
        this.lastlocation = lastlocation;
        this.clothdes = clothdes;
        this.policreportno = policreportno;
        this.relationship = relationship;
        this.reward = reward;
        this.latitude = latitude;
        this.longitude = longitude;
        this.userUid = userUid;
        this.imageUrl = imageUrl;
        this.timestamp = timestamp;
        this.seenDetailsList = seenDetailsList;
    }

    protected UserReport(Parcel in) {
        name = in.readString();
        gender = in.readString();
        age = in.readString();
        missdate = in.readString();
        contact = in.readString();
        lastlocation = in.readString();
        clothdes = in.readString();
        policreportno = in.readString();
        relationship = in.readString();
        reward = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        userUid = in.readString();
        timestamp = in.readString();
        imageUrl = in.readString();
        reportUid = in.readString();
        seenDetailsList = in.createTypedArrayList(SeenDetails.CREATOR);
    }

    public UserReport(String name, String gender, String age, String selectedDate, String contact, String lastlocation, String clothdes, String policreportno, String relationship, String reward, double latitude, double longitude, String userUid, String personimageUrl, String timestamp) {
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(gender);
        dest.writeString(age);
        dest.writeString(missdate);
        dest.writeString(contact);
        dest.writeString(lastlocation);
        dest.writeString(clothdes);
        dest.writeString(policreportno);
        dest.writeString(relationship);
        dest.writeString(reward);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(userUid);
        dest.writeString(timestamp);
        dest.writeString(imageUrl);
        dest.writeString(reportUid);
        dest.writeTypedList(seenDetailsList);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<UserReport> CREATOR = new Creator<UserReport>() {
        @Override
        public UserReport createFromParcel(Parcel in) {
            return new UserReport(in);
        }

        @Override
        public UserReport[] newArray(int size) {
            return new UserReport[size];
        }
    };

    // Getters and Setters
    public String getReportUid() { return reportUid; }
    public void setReportUid(String reportUid) { this.reportUid = reportUid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getAge() { return age; }
    public void setAge(String age) { this.age = age; }

    public String getMissdate() { return missdate; }
    public void setMissdate(String missdate) { this.missdate = missdate; }

    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    public String getLastlocation() { return lastlocation; }
    public void setLastlocation(String lastlocation) { this.lastlocation = lastlocation; }

    public String getClothdes() { return clothdes; }
    public void setClothdes(String clothdes) { this.clothdes = clothdes; }

    public String getPolicreportno() { return policreportno; }
    public void setPolicreportno(String policreportno) { this.policreportno = policreportno; }

    public String getRelationship() { return relationship; }
    public void setRelationship(String relationship) { this.relationship = relationship; }

    public String getReward() { return reward; }
    public void setReward(String reward) { this.reward = reward; }

    public double getLatitude() { return latitude; }
    public void setLatitude(double latitude) { this.latitude = latitude; }

    public double getLongitude() { return longitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }

    public String getUserUid() { return userUid; }
    public void setUserUid(String userUid) { this.userUid = userUid; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public List<SeenDetails> getSeenDetailsList() { return seenDetailsList; }
    public void setSeenDetailsList(List<SeenDetails> seenDetailsList) { this.seenDetailsList = seenDetailsList; }
}
