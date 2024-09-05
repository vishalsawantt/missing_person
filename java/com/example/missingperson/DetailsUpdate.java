package com.example.missingperson;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.Calendar;
import java.util.Locale;

public class DetailsUpdate extends AppCompatActivity  implements OnMapReadyCallback {
    EditText edtaddress;
    Button btnupdate,btntime;
    private Button btndate;

    private int year, month, day;
    int hour, minute;
    String selectedTime;
    private String selectedDate;
    MapView mapView;
    GoogleMap googleMap;
    private Marker lastMarker;
    private double latitude;
    private double longitude;
    String Rid;
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details_update);

        edtaddress = findViewById(R.id.edtaddress);
        btndate = findViewById(R.id.btndate);
        btntime = findViewById(R.id.btntime);
        btnupdate = findViewById(R.id.btnupdate);

        final Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);


        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        Intent intent = getIntent();
        if (intent.hasExtra("RID")) {
            Rid = intent.getStringExtra("RID");

        } else {
            // Handle the case where the key is not present in the Intent
        }

        btntime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show a custom TimePickerDialog with AM/PM options
                TimePickerDialog timePickerDialog = new TimePickerDialog(
                        DetailsUpdate.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int selectedHour, int selectedMinute) {
                                // Handle the selected time here
                                hour = selectedHour;
                                minute = selectedMinute;
                                // Format the selected time as needed, e.g., "hh:mm a"
                                selectedTime = String.format(Locale.getDefault(), "%02d:%02d %s", hour % 12, minute, (hour < 12) ? "AM" : "PM");
                                btntime.setText(selectedTime); // Display the selected time on the button
                            }
                        },
                        hour, minute, false // Set is24HourView to false for AM/PM options
                );
                timePickerDialog.show();
            }
        });


        btndate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show the DatePickerDialog when the button is clicked
                DatePickerDialog datePickerDialog = new DatePickerDialog(DetailsUpdate.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                                // Handle the selected date here
                                year = selectedYear;
                                month = selectedMonth;
                                day = selectedDay;
                                // Format the selected date as needed, e.g., "yyyy-MM-dd"
                                selectedDate = year + "-" + (month + 1) + "-" + day;
                                btndate.setText(selectedDate); // Display the selected date on the button
                            }
                        }, year, month, day);

                datePickerDialog.show();
            }
        });

        final FirebaseUpdateSeenDetails firebaseUpdater = new FirebaseUpdateSeenDetails(); // Create an instance of the FirebaseUpdateSeenDetails class

        btnupdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String seenAt = edtaddress.getText().toString(); // You can use the address as "seenAt" for the sighting
                String date = btndate.getText().toString();
                String time = btntime.getText().toString();

                // Call the updateSeenDetails method to update the Firebase database
                firebaseUpdater.updateSeenDetails(Rid, seenAt, latitude,longitude, date, time);

                // Show a toast message after updating the report
                Toast.makeText(DetailsUpdate.this, "Report updated successfully!", Toast.LENGTH_SHORT).show();

                // After updating the details, pass the new latitude and longitude back to HomeFragment
                Intent intent = new Intent();
                intent.putExtra("NewLatitude", latitude);
                intent.putExtra("NewLongitude", longitude);
                setResult(RESULT_OK, intent);

                // Navigate to HomeActivity
                Intent homeIntent = new Intent(DetailsUpdate.this, HomeActivity.class);
                startActivity(homeIntent);

            }
        });

    }

    @Override
    public void onMapReady(GoogleMap map) {
        googleMap = map;

        // Add a long-press listener to the map
        googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng latLng) {
                latitude = latLng.latitude;
                longitude = latLng.longitude;
                //googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15)); // Adjust the zoom level as needed

                // Remove the last added marker, if it exists
                if (lastMarker != null) {
                    lastMarker.remove();
                }

                // Add a new marker at the long-pressed location
                lastMarker = googleMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title("Location Marker"));

            }
        });
}

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }
}
