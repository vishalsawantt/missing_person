package com.example.missingperson;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class UserDetailsActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final String TAG = "UserDetailsActivity";

    private MapView mapView;
    private GoogleMap googleMap;
    private UserReport user;
    private List<SeenDetails> seenDetailsList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);

        // Get the user object from the intent
        user = getIntent().getParcelableExtra("user");
        // Initialize seenDetailsList
        seenDetailsList = new ArrayList<>();

        // Display user details in the activity
        if (user != null) {
            TextView textViewDetailsFragment = findViewById(R.id.textViewDetailsFragment);
            TextView textViewSeenDetails = findViewById(R.id.textViewSeenDetails);

            // Populate details text
            String details = "Name: " + user.getName() +
                    "\nGender: " + user.getGender() +
                    "\nAge: " + user.getAge() +
                    "\nMiss Date: " + user.getMissdate() +
                    "\nContact: " + user.getContact() +
                    "\nLast Location: " + user.getLastlocation() +
                    "\nCloth Description: " + user.getClothdes() +
                    "\nPolice Report No: " + user.getPolicreportno() +
                    "\nRelationship: " + user.getRelationship() +
                    "\nReward: " + user.getReward();
            textViewDetailsFragment.setText(details);

            // Fetch and display seenDetails from updateReports
            fetchAndDisplaySeenDetails(user.getReportUid());

            // Initialize MapView and get reference to GoogleMap object
            mapView = findViewById(R.id.mapView);
            if (mapView != null) {
                mapView.onCreate(null);
                mapView.onResume();
                mapView.getMapAsync(this);
            }
        }
    }

    private void fetchAndDisplaySeenDetails(String reportId) {
        DatabaseReference updateReportsRef = FirebaseDatabase.getInstance().getReference()
                .child("updateReports")
                .child(reportId)
                .child("seenDetails");

        updateReportsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    LinearLayout linearLayoutEntriesContainer = findViewById(R.id.linearLayoutEntriesContainer);
                    linearLayoutEntriesContainer.removeAllViews(); // Clear previous entries

                    seenDetailsList.clear(); // Clear previous data before populating

                    for (DataSnapshot seenDetailSnapshot : snapshot.getChildren()) {
                        SeenDetails seenDetails = seenDetailSnapshot.getValue(SeenDetails.class);
                        if (seenDetails != null) {
                            seenDetailsList.add(seenDetails);

                            // Inflate the item_seen_details.xml layout
                            View entryView = LayoutInflater.from(UserDetailsActivity.this).inflate(R.layout.item_seen_details, linearLayoutEntriesContainer, false);

                            // Find views within inflated layout
                            TextView textViewEntryDetails = entryView.findViewById(R.id.textViewEntryDetails);
                            ImageView imageViewSelectedImage = entryView.findViewById(R.id.imageViewSelectedImage);

                            // Set text and image if available
                            textViewEntryDetails.setText("Date: " + seenDetails.getDate() +
                                    "\nSaw Location: " + seenDetails.getSeenAt() +
                                    "\nTime: " + seenDetails.getTime() + "\n");

                            if (seenDetails.getSelectedImageUrl() != null && !seenDetails.getSelectedImageUrl().isEmpty()) {
                                Picasso.get().load(seenDetails.getSelectedImageUrl()).into(imageViewSelectedImage);
                            }

                            // Add the inflated layout to the container
                            linearLayoutEntriesContainer.addView(entryView);
                        } else {
                            Log.e(TAG, "SeenDetails is null");
                        }
                    }

                    // Update map with seenDetails locations
                    if (googleMap != null) {
                        updateMapWithSeenDetails(seenDetailsList);
                    }
                } else {
                    TextView textViewSeenDetails = findViewById(R.id.textViewSeenDetails);
                    textViewSeenDetails.setText("No seen details found for this report.");
                    Log.w(TAG, "No seenDetails found for reportId: " + reportId);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                TextView textViewSeenDetails = findViewById(R.id.textViewSeenDetails);
                textViewSeenDetails.setText("Failed to fetch seen details.");
                Log.e(TAG, "DatabaseError: " + error.getMessage());
            }
        });
    }



    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;

        // Add marker for initial location (UserReport)
        LatLng initialLocation = new LatLng(user.getLatitude(), user.getLongitude());
        googleMap.addMarker(new MarkerOptions().position(initialLocation).title("Initial Location"));

        // Fetch seenDetails from updateReports and update the map
        fetchAndDisplaySeenDetails(user.getReportUid());
    }

    private void updateMapWithSeenDetails(List<SeenDetails> seenDetailsList) {
        LatLng initialLocation = new LatLng(user.getLatitude(), user.getLongitude());
        LatLng previousLocation = initialLocation; // Initialize with initial location

        for (SeenDetails seenDetails : seenDetailsList) {
            LatLng location = new LatLng(seenDetails.getLatitude(), seenDetails.getLongitude());
            googleMap.addMarker(new MarkerOptions().position(location).title("Seen Location"));

            // Add polyline between previous location and current location
            googleMap.addPolyline(new PolylineOptions()
                    .add(previousLocation, location)
                    .width(5)
                    .color(Color.RED)); // Adjust line width and color as needed

            previousLocation = location; // Update previous location
        }

        // Move camera to initial location
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 12));
    }
}
