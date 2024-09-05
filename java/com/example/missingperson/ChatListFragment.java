package com.example.missingperson;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ChatListFragment extends Fragment {

    private RecyclerView recyclerViewChatList;
    private ChatUserAdapter chatUserAdapter;
    private List<ChatUser> chatUserList;

    private FirebaseAuth auth;
    private FirebaseFirestore firestore;
    private DatabaseReference usersDatabaseRef;

    private static final String TAG = "ChatListFragment";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chatlist, container, false);

        recyclerViewChatList = view.findViewById(R.id.recyclerViewChatList);
        recyclerViewChatList.setLayoutManager(new LinearLayoutManager(getContext()));

        chatUserList = new ArrayList<>();
        chatUserAdapter = new ChatUserAdapter(chatUserList);
        recyclerViewChatList.setAdapter(chatUserAdapter);

        auth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        usersDatabaseRef = FirebaseDatabase.getInstance().getReference("users");

        loadChatUsers();

        return view;
    }

    private void loadChatUsers() {
        String currentUserUid = auth.getCurrentUser().getUid();
        Set<String> userIdSet = new HashSet<>();

        firestore.collection("user_messages")
                .whereEqualTo("senderId", currentUserUid)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String receiverId = document.getString("receiverId");
                            Log.d(TAG, "ReceiverId found: " + receiverId);
                            if (receiverId != null) {
                                userIdSet.add(receiverId);
                            }
                        }

                        firestore.collection("user_messages")
                                .whereEqualTo("receiverId", currentUserUid)
                                .get()
                                .addOnCompleteListener(task2 -> {
                                    if (task2.isSuccessful()) {
                                        for (QueryDocumentSnapshot document : task2.getResult()) {
                                            String senderId = document.getString("senderId");
                                            Log.d(TAG, "SenderId found: " + senderId);
                                            if (senderId != null) {
                                                userIdSet.add(senderId);
                                            }
                                        }

                                        for (String userId : userIdSet) {
                                            fetchUserDetails(userId);
                                        }
                                    } else {
                                        Log.d(TAG, "Error getting documents: ", task2.getException());
                                    }
                                });
                    } else {
                        Log.d(TAG, "Error getting documents: ", task.getException());
                    }
                });
    }

    private void fetchUserDetails(String userId) {
        usersDatabaseRef.child(userId).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String fullName = snapshot.child("fullName").getValue(String.class);
                    String imageUrl = snapshot.child("imgurl").getValue(String.class);  // Fetch image URL
                    Log.d(TAG, "User details found: " + fullName);
                    chatUserList.add(new ChatUser(userId, fullName, imageUrl));  // Pass image URL to ChatUser
                    chatUserAdapter.notifyDataSetChanged();
                } else {
                    Log.d(TAG, "No such user in Realtime Database");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d(TAG, "Error getting user data: ", error.toException());
            }
        });
    }

}
