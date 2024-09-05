package com.example.missingperson;

import android.os.Parcel;
import android.os.Parcelable;

public class SeenDetails implements Parcelable {
    private String date;
    private double latitude;
    private double longitude;
    private String seenAt;
    private String time;

    public String getSelectedImageUrl() {
        return selectedImageUrl;
    }

    public void setSelectedImageUrl(String selectedImageUrl) {
        this.selectedImageUrl = selectedImageUrl;
    }

    private String selectedImageUrl;
    public SeenDetails() {}

    protected SeenDetails(Parcel in) {
        date = in.readString();
        latitude = in.readDouble();
        longitude = in.readDouble();
        seenAt = in.readString();
        time = in.readString();
        selectedImageUrl = in.readString();
    }

    public static final Creator<SeenDetails> CREATOR = new Creator<SeenDetails>() {
        @Override
        public SeenDetails createFromParcel(Parcel in) {
            return new SeenDetails(in);
        }

        @Override
        public SeenDetails[] newArray(int size) {
            return new SeenDetails[size];
        }
    };

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public String getSeenAt() {
        return seenAt;
    }

    public void setSeenAt(String seenAt) {
        this.seenAt = seenAt;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(date);
        dest.writeDouble(latitude);
        dest.writeDouble(longitude);
        dest.writeString(seenAt);
        dest.writeString(time);
        dest.writeString(selectedImageUrl);
    }
}
