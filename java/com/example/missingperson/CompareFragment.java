package com.example.missingperson;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.objdetect.CascadeClassifier;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CompareFragment extends Fragment implements OnMapReadyCallback {
    private static final int PICK_IMAGE_REQUEST = 72;
    //---------------------------------------------------
    EditText edtaddress;
    Button btntime;
    private Button btndate;
    Button sendMessageButton;

    private int year, month, day;
    int hour, minute;
    String selectedTime;
    private String selectedDate;
    MapView mapView;
    GoogleMap googleMap;
    private Marker lastMarker;
    private double latitude;
    private double longitude;
    //----------------------------------------------------
    private ImageView selectedImageView;
    private ImageView matchedImageView;
    private TextView userDetailsTextView;
    private Uri selectedImageUri;
    private DatabaseReference databaseRef;
    private DatabaseReference usersDatabaseRef;
    private List<UserReport> userReports;
    private FaceRecognitionHelper faceRecognitionHelper;
    private User matchedUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_imagerecognition, container, false);

        selectedImageView = view.findViewById(R.id.selected_image_view);
        matchedImageView = view.findViewById(R.id.matched_image_view);
        userDetailsTextView = view.findViewById(R.id.user_details_text_view);

        //-------------------------------------------------
        edtaddress = view.findViewById(R.id.edtaddress);
        btndate = view.findViewById(R.id.btndate);
        btntime = view.findViewById(R.id.btntime);
        sendMessageButton = view.findViewById(R.id.sendMessageButton);


        final Calendar calendar = Calendar.getInstance();
        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        day = calendar.get(Calendar.DAY_OF_MONTH);

        mapView = view.findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);
        //------------------------------------------------------

        Button compareButton = view.findViewById(R.id.compare_button);

        databaseRef = FirebaseDatabase.getInstance().getReference("userReports");
        usersDatabaseRef = FirebaseDatabase.getInstance().getReference("users");

        if (!OpenCVLoader.initDebug()) {
            Toast.makeText(getActivity(), "OpenCV initialization failed", Toast.LENGTH_SHORT).show();
            return view;
        }

        try {
            faceRecognitionHelper = new FaceRecognitionHelper(getActivity(), "model.tflite");
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(getActivity(), "Failed to load face recognition model", Toast.LENGTH_SHORT).show();
            return view;
        }

        fetchUserReports();

        selectedImageView.setOnClickListener(v -> chooseImage());

        compareButton.setOnClickListener(v -> {
            if (selectedImageUri != null) {
                Toast processingToast = Toast.makeText(getActivity(), "Processing...", Toast.LENGTH_SHORT);
                processingToast.show();

                new CompareTask(processingToast).execute(selectedImageUri);
            } else {
                Toast.makeText(getActivity(), "Please select an image first", Toast.LENGTH_SHORT).show();
            }
        });

        //---------------------------------------------------------------------------

        btntime.setOnClickListener(v -> {
            TimePickerDialog timePickerDialog = new TimePickerDialog(getContext(),
                    (view12, hourOfDay, minute) -> {
                        selectedTime = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                        btntime.setText(selectedTime);
                    }, hour, minute, false);
            timePickerDialog.show();
        });


        final FirebaseUpdateSeenDetails firebaseUpdater = new FirebaseUpdateSeenDetails(); // Create an instance of the FirebaseUpdateSeenDetails class


        btndate.setOnClickListener(v -> {
            DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                    (view1, year, month, dayOfMonth) -> {
                        selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                        btndate.setText(selectedDate);
                    }, year, month, day);
            datePickerDialog.show();
        });

        //-----------------------------------------------------------------
        // Set up the button click listener to send a message
        // Initially hide the button
        sendMessageButton.setVisibility(View.GONE);

        // Set up the button click listener to send a message
        sendMessageButton.setOnClickListener(v -> {
            if (matchedUser != null) {
                Intent intent = new Intent(getActivity(), ChatActivity.class);
                intent.putExtra("receiverUid", matchedUser.getUserId()); // Pass the receiver UID
                startActivity(intent);
            } else {
                Toast.makeText(getActivity(), "No matched user to send a message to", Toast.LENGTH_SHORT).show();
            }
        });



        return view;
    }

    private void fetchUserReports() {
        userReports = new ArrayList<>();
        databaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot childSnapshot : snapshot.getChildren()) {
                    UserReport userReport = childSnapshot.getValue(UserReport.class);
                    if (userReport != null) {
                        userReports.add(userReport);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getActivity(), "Failed to fetch user reports", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void chooseImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            if (data != null && data.getData() != null) {
                selectedImageUri = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImageUri);
                    selectedImageView.setImageBitmap(bitmap);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //----------------------------------------------------------------
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
    //------------------------------------------------------------------------------

    private void updateMatchedUserReportDetails(UserReport matchedUserReport, String selectedImageUrl) {
        String seenAt = edtaddress.getText().toString();
        String date = btndate.getText().toString();
        String time = btntime.getText().toString();

        if (matchedUserReport != null) {
            String matchedImageUrl = matchedUserReport.getImageUrl();

            // Fetch all user reports and find the one that contains the matched image URL
            DatabaseReference userReportsRef = FirebaseDatabase.getInstance().getReference().child("userReports");
            userReportsRef.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    boolean reportFound = false;
                    for (DataSnapshot reportSnapshot : snapshot.getChildren()) {
                        UserReport userReport = reportSnapshot.getValue(UserReport.class);
                        if (userReport != null && matchedImageUrl.equals(userReport.getImageUrl())) {
                            String reportId = reportSnapshot.getKey(); // Get the reportId of the matched report
                            reportFound = true;

                            DatabaseReference updateReportsRef = FirebaseDatabase.getInstance().getReference()
                                    .child("updateReports")
                                    .child(reportId)
                                    .child("seenDetails");

                            // Generate a unique ID for the new seenDetails entry
                            String uniqueId = updateReportsRef.push().getKey();

                            Map<String, Object> seenDetailsMap = new HashMap<>();
                            seenDetailsMap.put("seenAt", seenAt);
                            seenDetailsMap.put("latitude", latitude);
                            seenDetailsMap.put("longitude", longitude);
                            seenDetailsMap.put("date", date);
                            seenDetailsMap.put("time", time);
                            seenDetailsMap.put("reportId", reportId); // Store the report ID in the seenDetails object
                            seenDetailsMap.put("matchedImageUrl", matchedImageUrl); // Add the matched image URL to the seen details
                            seenDetailsMap.put("selectedImageUrl", selectedImageUrl); // Add the selected image URL to the seen details

                            // Use the generated unique ID to create a new entry under seenDetails
                            updateReportsRef.child(uniqueId).setValue(seenDetailsMap)
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(getActivity(), "Seen details updated successfully!", Toast.LENGTH_SHORT).show();
                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            Toast.makeText(getActivity(), "Failed to update seen details: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        }
                                    });

                            break; // Exit the loop once the matched report is found
                        }
                    }

                    if (!reportFound) {
                        Toast.makeText(getActivity(), "Matched user report not found", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getActivity(), "Failed to fetch user reports", Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            Toast.makeText(getActivity(), "No matched user report found", Toast.LENGTH_SHORT).show();
        }
    }
    //----------------------------------------------------------------

    private class CompareTask extends AsyncTask<Uri, Void, UserReport> {
        private Toast processingToast;
        private Bitmap selectedBitmap;

        public CompareTask(Toast processingToast) {
            this.processingToast = processingToast;
        }

        @Override
        protected void onPreExecute() {
            matchedImageView.setImageBitmap(null);
            userDetailsTextView.setText("");
            Toast.makeText(getActivity(), "Processing...", Toast.LENGTH_SHORT).show();
        }

        @Override
        protected UserReport doInBackground(Uri... uris) {
            Uri selectedImageUri = uris[0];
            try {
                selectedBitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), selectedImageUri);

                if (!containsHuman(selectedBitmap)) {
                    return null;
                }

                float[] selectedEmbeddings = faceRecognitionHelper.recognizeImage(selectedBitmap);

                UserReport bestMatchReport = null;
                float bestSimilarity = -1;

                for (UserReport userReport : userReports) {
                    try {
                        Bitmap storedBitmap = getBitmapFromURL(userReport.getImageUrl());
                        if (storedBitmap == null || !containsHuman(storedBitmap)) {
                            continue;
                        }

                        float[] storedEmbeddings = faceRecognitionHelper.recognizeImage(storedBitmap);
                        float similarity = FaceRecognitionHelper.cosineSimilarity(selectedEmbeddings, storedEmbeddings);
                        if (similarity > bestSimilarity) {
                            bestSimilarity = similarity;
                            bestMatchReport = userReport;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                return bestMatchReport;

            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(UserReport result) {
            processingToast.cancel();

            if (result != null) {
                Toast.makeText(getActivity(), "Match found!", Toast.LENGTH_SHORT).show();
                new LoadImageTask(matchedImageView).execute(result.getImageUrl());
                fetchUserDetails(result.getUserUid());

                // Upload the selected image to Firebase Storage and update seenDetails
                uploadSelectedImage(result, selectedBitmap);
            } else {
                Toast.makeText(getActivity(), "No match found or no human face detected", Toast.LENGTH_SHORT).show();
            }
        }

        private boolean containsHuman(Bitmap bitmap) {
            Mat imageMat = new Mat();
            Utils.bitmapToMat(bitmap, imageMat);

            CascadeClassifier cascadeClassifier = new CascadeClassifier(getFaceCascadeFile());
            MatOfRect faces = new MatOfRect();
            cascadeClassifier.detectMultiScale(imageMat, faces);

            return !faces.empty();
        }

        private String getFaceCascadeFile() {
            try {
                File cascadeFile = new File(getActivity().getCacheDir(), "haarcascade_frontalface_alt.xml");
                if (!cascadeFile.exists()) {
                    InputStream is = getActivity().getAssets().open("haarcascade_frontalface_alt.xml");
                    OutputStream os = new FileOutputStream(cascadeFile);
                    byte[] buffer = new byte[4096];
                    int bytesRead;
                    while ((bytesRead = is.read(buffer)) != -1) {
                        os.write(buffer, 0, bytesRead);
                    }
                    is.close();
                    os.close();
                }
                return cascadeFile.getAbsolutePath();
            } catch (IOException e) {
                e.printStackTrace();
                return "";
            }
        }

        private Bitmap getBitmapFromURL(String src) throws IOException {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        }

        // Method to fetch user details
        private void fetchUserDetails(String userId) {
            usersDatabaseRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    matchedUser = snapshot.getValue(User.class);
                    if (matchedUser != null) {
                        String userDetails = "This report is for the following user:\n\n" +
                                "Name: " + matchedUser.getFullName() + "\n" +
                                "Email: " + matchedUser.getEmail() + "\n" +
                                "Mob No: " + matchedUser.getMobileNumber();
                        userDetailsTextView.setText(userDetails);

                        // Update the button text with the user's name
                        sendMessageButton.setText("Send message to " + matchedUser.getFullName());

                        // Make the button visible
                        sendMessageButton.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Toast.makeText(getActivity(), "Failed to fetch user details", Toast.LENGTH_SHORT).show();
                }
            });
        }


        // Upload the selected image to Firebase Storage and update seenDetails
        private void uploadSelectedImage(UserReport matchedUserReport, Bitmap selectedBitmap) {
            // Get a reference to the Firebase Storage location
            StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("matchImages");

            // Create a unique file name for the uploaded image (you can customize this as per your requirement)
            String imageName = "image_" + System.currentTimeMillis() + ".jpg";
            StorageReference imageRef = storageRef.child(imageName);

            // Convert Bitmap to byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            selectedBitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] imageData = baos.toByteArray();

            // Upload the image data to Firebase Storage
            UploadTask uploadTask = imageRef.putBytes(imageData);
            uploadTask.addOnSuccessListener(taskSnapshot -> {
                // Image uploaded successfully, get the download URL
                imageRef.getDownloadUrl().addOnSuccessListener(uri -> {
                    String downloadUrl = uri.toString();
                    // Update seenDetails in Firebase Database with the selected image URL
                    updateMatchedUserReportDetails(matchedUserReport, downloadUrl);
                }).addOnFailureListener(e -> {
                    Toast.makeText(getActivity(), "Failed to get download URL: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
            }).addOnFailureListener(e -> {
                Toast.makeText(getActivity(), "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }


    private static class LoadImageTask extends AsyncTask<String, Void, Bitmap> {
        private final ImageView imageView;

        public LoadImageTask(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        protected Bitmap doInBackground(String... urls) {
            String url = urls[0];
            try {
                return getBitmapFromURL(url);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (bitmap != null) {
                imageView.setImageBitmap(bitmap);
            } else {
                Toast.makeText(imageView.getContext(), "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        }

        private Bitmap getBitmapFromURL(String src) throws IOException {
            URL url = new URL(src);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            InputStream input = connection.getInputStream();
            return BitmapFactory.decodeStream(input);
        }
    }
    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    public void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    public void onDestroy() {
        mapView.onDestroy();
        super.onDestroy();
    }
}