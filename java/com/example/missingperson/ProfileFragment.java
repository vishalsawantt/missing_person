package com.example.missingperson;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ProfileFragment extends Fragment {

    TextView txtfullname, txtaddres, txtmobailno, txtemail;
    Button btnlogout;
    ImageView imgprofile;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        // Find and initialize the TextViews and ImageView using their IDs
        txtfullname = view.findViewById(R.id.txtfullname);
        txtaddres = view.findViewById(R.id.txtaddres);
        txtmobailno = view.findViewById(R.id.txtmobailno);
        txtemail = view.findViewById(R.id.txtemail);
        btnlogout = view.findViewById(R.id.btnlogout);
        imgprofile = view.findViewById(R.id.imgprofile);

        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser == null) {
            // User is not authenticated, redirect to login screen
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            getActivity().finish(); // Close the current activity
            return view; // Return the view early to avoid further execution
        }

        String userId = currentUser.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference("users").child(userId);
        getData("users/" + userId);

        btnlogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setTitle("Logout Confirmation");
                builder.setMessage("Are you sure you want to log out?");
                builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mAuth.signOut(); // Sign out the user
                        Intent i = new Intent(getActivity(), LoginActivity.class);
                        startActivity(i);
                        getActivity().finish(); // Close the current activity
                    }
                });
                builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss(); // Close the dialog
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });

        return view;
    }

    public void getData(String url) {
        System.out.println("-------------" + url);
        try {
            FirebaseDatabase mFirebaseInstance = FirebaseDatabase.getInstance();
            DatabaseReference mFirebaseDatabase = mFirebaseInstance.getReference(url);
            mFirebaseDatabase.keepSynced(true);
            mFirebaseDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    System.out.println("is req----------------" + dataSnapshot.toString());
                    User user = dataSnapshot.getValue(User.class); // Retrieve User object

                    if (user != null) {
                        txtfullname.setText(user.getFullName());
                        txtaddres.setText(user.getAddress());
                        txtmobailno.setText(user.getMobileNumber());
                        txtemail.setText(user.getEmail());

                        // Load the profile image using Picasso
                        String imageUrl = user.getImgurl(); // Get the image URL from the User object
                        if (imageUrl != null && !imageUrl.isEmpty()) {
                            Picasso.get().load(imageUrl).into(imgprofile);
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Handle possible errors
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
