package com.example.missingperson;

public class ChatUser {
    private String userId;
    private String fullName;
    private String imageUrl;  // Add this line

    public ChatUser() {}

    public ChatUser(String userId, String fullName, String imageUrl) {
        this.userId = userId;
        this.fullName = fullName;
        this.imageUrl = imageUrl;  // Add this line
    }

    public String getUserId() {
        return userId;
    }

    public String getFullName() {
        return fullName;
    }

    public String getImageUrl() {  // Add this getter
        return imageUrl;
    }
}
