package com.example.missingperson;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment implements OnMapReadyCallback {

    private GoogleMap mMap;
    private List<Marker> markers = new ArrayList<>();
    private String currentUserUid; // Declare currentUserUid field
    private SearchView searchView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_home, container, false);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }

        // Add the FloatingActionButton to the layout and set an OnClickListener
        FloatingActionButton fab = rootView.findViewById(R.id.btnnewreport);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Handle the click event for the FloatingActionButton
                Intent reportIntent = new Intent(getActivity(), ReportActivity.class);
                startActivity(reportIntent);
            }
        });

        // Find the TextView for report count
        final TextView reportCountTextView = rootView.findViewById(R.id.tvReportCount);

        // Retrieve count of reports from Firebase and update the TextView
        DatabaseReference reportsRef = FirebaseDatabase.getInstance().getReference().child("userReports");
        reportsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                long reportCount = dataSnapshot.getChildrenCount();
                reportCountTextView.setText("Total Reports: " + reportCount);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle database error
            }
        });

        // Initialize currentUserUid
        currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Initialize SearchView
        searchView = rootView.findViewById(R.id.searchView);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // Perform search when the user submits the query
                searchMarker(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                // Handle text changes if needed
                return false;
            }
        });

        return rootView;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        addMarkersFromFirebase();

        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                // Retrieve the name associated with the clicked marker
                String missingPersonName = marker.getTitle();
                // Create and show a custom dialog
                showDialogWithDetails(marker);
                return true; // Consume the marker click event
            }
        });
    }

    private void searchMarker(String name) {
        for (Marker marker : markers) {
            // Get the title of the marker (name associated with it)
            String markerTitle = marker.getTitle();

            // Perform case insensitive comparison
            if (markerTitle != null && markerTitle.toLowerCase().contains(name.toLowerCase())) {
                // Found the marker with a name that contains the search query
                LatLng position = marker.getPosition();
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(position, 15)); // Zoom level can be adjusted
                showDialogWithDetails(marker); // Show dialog with marker details
                return;
            }
        }

        // Show a message if no marker with the specified name is found
        Toast.makeText(getContext(), "Marker with name " + name + " not found", Toast.LENGTH_SHORT).show();
    }


    private void showDialogWithDetails(Marker marker) {
        String uid = marker.getSnippet(); // Assuming the UID is stored as the snippet

        // Create and configure a custom dialog with the transparent theme
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.TransparentDialogTheme);
        View dialogView = getLayoutInflater().inflate(R.layout.custom_marker_info_dialog, null);
        builder.setView(dialogView);

        // Set the name in the dialog
        TextView nameTextView = dialogView.findViewById(R.id.nameTextView);
        nameTextView.setText(marker.getTitle());

        // Load and display the image
        ImageView imageView = dialogView.findViewById(R.id.imageView);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("userReports").child(uid);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String imageUrl = dataSnapshot.child("imageUrl").getValue(String.class);
                    if (imageUrl != null && !imageUrl.isEmpty()) {
                        Picasso.get().load(imageUrl).into(imageView);
                    } else {
                        // Handle case where image URL is not available
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle database error
            }
        });

        // Add a button to open a new activity
        Button detailButton = dialogView.findViewById(R.id.detailButton);
        detailButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Handle the button click to open a new activity
                Intent intent = new Intent(getContext(), DetailsActivity.class);
                intent.putExtra("RID", uid);
                startActivity(intent);
            }
        });

        // Create and show the dialog
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    // Function to retrieve coordinates from Firebase and add markers
    private void addMarkersFromFirebase() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("userReports");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                markers.clear(); // Clear the list to ensure it's up-to-date

                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Double latitude = snapshot.child("latitude").getValue(Double.class);
                    Double longitude = snapshot.child("longitude").getValue(Double.class);
                    String name = snapshot.child("name").getValue(String.class);
                    String reportId = snapshot.getKey(); // Assuming the report ID is the key

                    // Check if all necessary values are present
                    if (latitude != null && longitude != null && name != null) {
                        LatLng location = new LatLng(latitude, longitude);
                        MarkerOptions markerOptions = new MarkerOptions()
                                .position(location)
                                .title(name)
                                .snippet(reportId);

                        // Check if the report belongs to the current user
                        if (snapshot.child("userUid").exists()) {
                            String reportUserUid = snapshot.child("userUid").getValue(String.class);
                            if (reportUserUid != null && reportUserUid.equals(currentUserUid)) {
                                // If it belongs to the current user, set marker color to red
                                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
                            } else {
                                // If it doesn't belong to the current user, set marker color to green
                                markerOptions.icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                            }
                        } else {
                            // Handle case where userUid is not available
                        }

                        Marker marker = mMap.addMarker(markerOptions);
                        markers.add(marker); // Add the marker to the list
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Handle database error
            }
        });
    }
}
