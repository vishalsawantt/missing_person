package com.example.missingperson;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;


public class GetLatLog extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_homefrg);
        MapsInitializer.initialize(this);

        SupportMapFragment mapFragment=(SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.mapfrg);
        mapFragment.getMapAsync(this);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        // Set a long-press listener on the map
        mMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                // Handle the long-press event here
                // You can access the latitude and longitude (latLng) to store or use as needed
                double latitude = latLng.latitude;
                double longitude = latLng.longitude;
            }
        });
    }
}
