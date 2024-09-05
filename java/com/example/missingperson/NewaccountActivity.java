package com.example.missingperson;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.InputType;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.util.UUID;

public class NewaccountActivity extends AppCompatActivity {

    private static final int mGalleryRequestCode = 1;
    private static final int mInputSize = 500;

    private EditText editTextFullName, editTextMobileNumber, editTextEmail, editTextPassword, editTextAddress;
    private Button buttonLogin;
    private Spinner spinnerGender;
    private CheckBox checkBoxShowPassword; // Added CheckBox
    private FirebaseAuth mAuth;
    private ImageView userimage;

    private DatabaseReference databaseReference;
    private FirebaseStorage storage;
    private Bitmap mBitmap;
    private String personimageUrlu = "";
    ProgressBar progressBarThree;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newaccount);

        editTextFullName = findViewById(R.id.editTextFullName);
        editTextMobileNumber = findViewById(R.id.editTextMobileNumber);
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        checkBoxShowPassword = findViewById(R.id.checkBoxShowPassword); // Initialize CheckBox
        editTextAddress = findViewById(R.id.editTextAddress);
        spinnerGender = findViewById(R.id.spinnerGender);
        userimage = findViewById(R.id.userimage); // Initialize ImageView
        progressBarThree = findViewById(R.id.progressBarThree);


        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.gender_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerGender.setAdapter(adapter);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("users");
        storage = FirebaseStorage.getInstance(); // Initialize FirebaseStorage

        checkBoxShowPassword.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                // Toggle password visibility
                int inputType = isChecked ? InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD : InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD;
                editTextPassword.setInputType(inputType);
            }
        });

        userimage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent callGalleryIntent = new Intent(Intent.ACTION_PICK);
                callGalleryIntent.setType("image/*");
                startActivityForResult(callGalleryIntent, mGalleryRequestCode);
            }
        });

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String fullName = editTextFullName.getText().toString();
                String mobileNumber = "+91 " + editTextMobileNumber.getText().toString().trim();
                String email = editTextEmail.getText().toString();
                String password = editTextPassword.getText().toString();
                String address = editTextAddress.getText().toString();
                String selectedGender = spinnerGender.getSelectedItem().toString();

                if (!isValidEmail(email)) {
                    Toast.makeText(NewaccountActivity.this, "Invalid email address", Toast.LENGTH_SHORT).show();
                } else if (!isValidPassword(password)) {
                    Toast.makeText(NewaccountActivity.this, "Invalid password. Password must have at least 8 characters with a mix of letters and numbers", Toast.LENGTH_SHORT).show();
                } else {
                    mAuth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener(NewaccountActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        String userId = mAuth.getCurrentUser().getUid();

                                        // Store user information in the database
                                        User user = new User(fullName, mobileNumber, address, email, selectedGender, personimageUrlu, userId);
                                        databaseReference.child(userId).setValue(user)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            Toast.makeText(NewaccountActivity.this, "Account created successfully", Toast.LENGTH_SHORT).show();

                                                            Intent intent = new Intent(NewaccountActivity.this, HomeActivity.class);
                                                            startActivity(intent);
                                                            finish();
                                                        } else {
                                                            Toast.makeText(NewaccountActivity.this, "Failed to create account. Please try again.", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }
                                                });
                                    } else {
                                        String errorMessage = task.getException().getMessage();
                                        if (errorMessage != null && errorMessage.contains("email address is already in use")) {
                                            Toast.makeText(NewaccountActivity.this, "Email is already in use. Please log in or use a different email.", Toast.LENGTH_SHORT).show();
                                        } else {
                                            Toast.makeText(NewaccountActivity.this, "Account creation failed. Please try again.", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                }
                            });
                }
            }
        });
    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidPassword(String password) {
        return password.length() >= 8 && password.matches(".*\\d.*") && password.matches(".*[a-zA-Z].*");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == mGalleryRequestCode && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            try {
                mBitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                mBitmap = scaleImage(mBitmap);
                userimage.setImageBitmap(mBitmap);
                uploadImageToStorage(uri);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Unrecognized request code", Toast.LENGTH_LONG).show();
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
        progressBarThree.setVisibility(View.VISIBLE); // Show the ProgressBar

        final StorageReference storageRef = storage.getReference().child("UserImages");
        final StorageReference imageRef = storageRef.child(UUID.randomUUID().toString() + ".jpg");

        UploadTask uploadTask = imageRef.putFile(fileUri);

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                progressBarThree.setVisibility(View.GONE); // Hide the ProgressBar on failure
                Toast.makeText(NewaccountActivity.this, "Failed to upload image: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                imageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(Uri uri) {
                        progressBarThree.setVisibility(View.GONE); // Hide the ProgressBar on success
                        personimageUrlu = uri.toString();
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        progressBarThree.setVisibility(View.GONE); // Hide the ProgressBar on failure
                        Toast.makeText(NewaccountActivity.this, "Failed to get download URL: " + exception.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

}
