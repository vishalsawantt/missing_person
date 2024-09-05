package com.example.missingperson;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class MyReportsFragment extends Fragment {
    private RecyclerView recyclerView;
    private UserAdapter userAdapter;
    private List<UserReport> userReports;
    private TextView textViewDetailsFragment;
    private FirebaseUser currentUser;
    private LinearLayout detailsLayout;
    private View view;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_myreports, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        userReports = new ArrayList<>();

        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            String currentUserId = currentUser.getUid();
            textViewDetailsFragment = view.findViewById(R.id.textViewDetailsFragment);
            userAdapter = new UserAdapter(userReports);
            recyclerView.setAdapter(userAdapter);

            // Add item click listener here
            userAdapter.setOnItemClickListener(new UserAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(UserReport user, int position) {
                    // Handle item click, e.g., show details
                    showUserDetails(user, position);
                }
            });

            fetchDataForCurrentUser(currentUserId);
        } else {
            // Handle the case when the user is not logged in
        }

        return view;
    }


    private void fetchDataForCurrentUser(String currentUserId) {
        DatabaseReference usersRef = FirebaseDatabase.getInstance().getReference("userReports");

        usersRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userReports.clear();

                for (DataSnapshot reportSnapshot : dataSnapshot.getChildren()) {
                    String userKey = reportSnapshot.child("userUid").getValue(String.class);

                    FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

                    if (currentUser != null && userKey != null && userKey.equals(currentUser.getUid())) {
                        UserReport user = reportSnapshot.getValue(UserReport.class);

                        if (user != null) {
                            // Log the values to check
                            Log.d("UserDetails", "Name: " + user.getName());

                            // Set seenDetails list to the UserReport object
                            List<SeenDetails> seenDetailsList = new ArrayList<>();
                            for (DataSnapshot seenDetailsSnapshot : reportSnapshot.child("seenDetails").getChildren()) {
                                SeenDetails seenDetails = seenDetailsSnapshot.getValue(SeenDetails.class);
                                if (seenDetails != null) {
                                    seenDetailsList.add(seenDetails);
                                }
                            }
                            user.setSeenDetailsList(seenDetailsList);

                            userReports.add(user);
                        }
                    }
                }

                userAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Handle database errors if needed
            }
        });
    }


    private void showUserDetails(UserReport user, int position) {
        // Create an Intent to start the UserDetailsActivity
        Intent intent = new Intent(getActivity(), UserDetailsActivity.class);

        // Pass the user object to the UserDetailsActivity
        intent.putExtra("user", user);

        // Start the activity
        startActivity(intent);
    }

}

