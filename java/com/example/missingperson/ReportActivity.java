package com.example.missingperson;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class ReportActivity extends AppCompatActivity {
    EditText edtname, edtage, edtcontactInformation, edtlastSeenLocation, edtclothingDescription, edtpoliceReportNumber, edtrelationshipToMissingPerson, edtrewardInformation;
    Button btnsubmit;
    private Button btnmissingdate;
    private int year, month, day;
    private String selectedDate;
    Spinner spigender;
    FirebaseDatabase rootNode;
    DatabaseReference reference;
    private FragmentManager fragmentManager;
    private double latitude;
    private double longitude;
    MapView mapView;
    GoogleMap googleMap;
    private Marker lastMarker;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseUser currentUser = mAuth.getCurrentUser();
    String userUid, reportUid;
    String personimageUrl = "";
    ImageView mpersonimage;
    private int mGalleryRequestCode = 2;
    private Bitmap mBitmap;
    ImageView imgcrop;
    private int mInputSize = 224;
    Button googlesearch;
    private FirebaseStorage storage;
    ProgressBar progressBarTwo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_homefrg);
        storage = FirebaseStorage.getInstance();
        fragmentManager = getSupportFragmentManager();
        mpersonimage = findViewById(R.id.mpersonimage);
        edtname = findViewById(R.id.edtname);
        spigender = findViewById(R.id.spigender);
        edtage = findViewById(R.id.edtage);
        btnmissingdate = findViewById(R.id.btnmissingdate);
        edtcontactInformation = findViewById(R.id.edtcontactInformation);
        edtlastSeenLocation = findViewById(R.id.edtlastSeenLocation);
        edtclothingDescription = findViewById(R.id.edtclothingDescription);
        edtpoliceReportNumber = findViewById(R.id.edtpoliceReportNumber);
        edtrelationshipToMissingPerson = findViewById(R.id.edtrelationshipToMissingPerson);
        edtrewardInformation = findViewById(R.id.edtrewardInformation);
        btnsubmit = findViewById(R.id.btnsubmit);
        progressBarTwo = findViewById(R.id.progressBarTwo);

        rootNode = FirebaseDatabase.getInstance();
        reference = rootNode.getReference("userReports");

        // Assign userUid from the current Firebase user
        if (currentUser != null) {
            userUid = currentUser.getUid();
        }

        ArrayAdapter<CharSequence> genderAdapter = new ArrayAdapter<>(ReportActivity.this, android.R.layout.simple_spinner_item);
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        genderAdapter.add("Male");
        genderAdapter.add("Female");
        spigender.setAdapter(genderAdapter);

        final Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);

        mpersonimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callGalleryIntent = new Intent(Intent.ACTION_PICK);
                callGalleryIntent.setType("image/*");
                startActivityForResult(callGalleryIntent, mGalleryRequestCode);
            }
        });

        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap map) {
                googleMap = map;

                // Add a long-press listener to the map
                googleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                    @Override
                    public void onMapLongClick(LatLng latLng) {
                        latitude = latLng.latitude;
                        longitude = latLng.longitude;

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
        });

        btnmissingdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Show the DatePickerDialog when the button is clicked
                DatePickerDialog datePickerDialog = new DatePickerDialog(ReportActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                                // Handle the selected date here
                                year = selectedYear;
                                month = selectedMonth;
                                day = selectedDay;
                                // Format the selected date as needed, e.g., "yyyy-MM-dd"
                                selectedDate = year + "-" + (month + 1) + "-" + day;
                                btnmissingdate.setText(selectedDate); // Display the selected date on the button
                            }
                        }, year, month, day);

                datePickerDialog.show();
            }
        });

        btnsubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get the current time formatted as "HH:mm"
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
                String timestamp = sdf.format(new Date());

                String name = edtname.getText().toString().trim();
                String gender = spigender.getSelectedItem().toString();
                String age = edtage.getText().toString().trim() + " year old"; // Add "year" to the age input
                String contact = "+91 " + edtcontactInformation.getText().toString().trim(); // Prefix "+91" to the contact input
                String lastlocation = edtlastSeenLocation.getText().toString().trim();
                String clothdes = edtclothingDescription.getText().toString().trim();
                String policreportno = edtpoliceReportNumber.getText().toString().trim();
                String relationship = edtrelationshipToMissingPerson.getText().toString().trim();
                String reward = edtrewardInformation.getText().toString().trim();

                if (name.isEmpty() || age.isEmpty() || contact.isEmpty() || lastlocation.isEmpty() || clothdes.isEmpty() || policreportno.isEmpty() || relationship.isEmpty() || reward.isEmpty() || selectedDate.isEmpty()) {
                    Toast.makeText(ReportActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                } else if (latitude == 0.0 || longitude == 0.0) {
                    Toast.makeText(ReportActivity.this, "Please select a location on the map", Toast.LENGTH_SHORT).show();
                } else {
                    // Generate a unique key for the new report
                    reportUid = reference.push().getKey();

                    // Set the reportUid in the userReport object
                    UserReport userReport = new UserReport(reportUid,name, gender, age, selectedDate, contact, lastlocation, clothdes, policreportno, relationship, reward, latitude, longitude, userUid, personimageUrl, timestamp);

                    // Push the UserReport object to the Firebase database using the generated key
                    reference.child(reportUid).setValue(userReport);

                    // Reset UI fields
                    edtname.setText("");
                    spigender.setSelection(0);
                    edtage.setText("");
                    btnmissingdate.setText("");
                    edtcontactInformation.setText("");
                    edtlastSeenLocation.setText("");
                    edtclothingDescription.setText("");
                    edtpoliceReportNumber.setText("");
                    edtrelationshipToMissingPerson.setText("");
                    edtrewardInformation.setText("");

                    // Show a success message
                    Toast.makeText(ReportActivity.this, "Submit successful", Toast.LENGTH_SHORT).show();
                    // Navigate to HomeFragment
                    Intent intent = new Intent(ReportActivity.this, HomeFragment.class);
                    startActivity(intent);
                    finish();
                }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == mGalleryRequestCode && resultCode == RESULT_OK && data != null) {
            // Show ProgressBar while uploading
            progressBarTwo.setVisibility(View.VISIBLE);

            Uri uri = data.getData();
            try {
                uploadImageToStorage(uri);
                mBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
            } catch (IOException e) {
                e.printStackTrace();
            }
            mBitmap = scaleImage(mBitmap);
            mpersonimage.setImageBitmap(mBitmap);
        } else {
            Toast.makeText(this, "Unrecognized request code or no data received", Toast.LENGTH_LONG).show();
        }
    }

    private Bitmap scaleImage(Bitmap bitmap) {
        int originalWidth = bitmap.getWidth();
        int originalHeight = bitmap.getHeight();
        float scaleWidth = (float) mInputSize / originalWidth;
        float scaleHeight = (float) mInputSize / originalHeight;
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        return Bitmap.createBitmap(bitmap, 0, 0, originalWidth, originalHeight, matrix, true);
    }

    private void uploadImageToStorage(Uri fileUri) {
        // Replace "images" with your desired folder name in Firebase Storage
        final StorageReference storageRef = storage.getReference().child("PersonImages");

        // Create a reference to the file in Firebase Storage
        final StorageReference imageRef = storageRef.child(UUID.randomUUID().toString() + ".jpg");

        // Upload the file to Firebase Storage
        UploadTask uploadTask = imageRef.putFile(fileUri);

        // Register observers to listen for when the upload is done or if it fails
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
                progressBarTwo.setVisibility(View.GONE); // Hide ProgressBar on failure
                Toast.makeText(ReportActivity.this, "Upload failed: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // Task completed successfully
                // Get the download URL for the image
                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        personimageUrl = uri.toString();
                        // Hide ProgressBar on success
                        progressBarTwo.setVisibility(View.GONE);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Hide ProgressBar on failure
                        progressBarTwo.setVisibility(View.GONE);
                        // Handle any errors getting the download URL
                        Toast.makeText(ReportActivity.this, "Failed to retrieve download URL: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
