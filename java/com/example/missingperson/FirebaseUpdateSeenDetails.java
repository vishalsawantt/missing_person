package com.example.missingperson;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class FirebaseUpdateSeenDetails {
    private DatabaseReference databaseReference;

    public FirebaseUpdateSeenDetails() {
        // Initialize the Firebase Database
        databaseReference = FirebaseDatabase.getInstance().getReference();
    }

    public void updateSeenDetails(String uid, String seenAt, double latitude, double longitude, String date, String time) {
        // Create a reference to the specific person's record
        DatabaseReference personRef = databaseReference.child("userReports").child(uid);

        // Create a new SeenDetails entry
        DatabaseReference seenDetailsRef = personRef.child("seenDetails").push();

        // Set the "seenDetails" values
        seenDetailsRef.child("seenAt").setValue(seenAt);
        seenDetailsRef.child("latitude").setValue(latitude);
        seenDetailsRef.child("longitude").setValue(longitude);
        seenDetailsRef.child("date").setValue(date);
        seenDetailsRef.child("time").setValue(time);
    }
}