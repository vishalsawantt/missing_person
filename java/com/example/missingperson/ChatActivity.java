package com.example.missingperson;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    private EditText edtMessage;
    TextView receiverNameTextView;
    private ImageButton btnSend;
    private RecyclerView recyclerViewMessages;
    private MessageAdapter messageAdapter;
    private List<Message> messageList;
    private FirebaseFirestore firestore;
    private FirebaseAuth auth;
    private String receiverUid; // Receiver's UID
    private String receiverName; // Receiver's Name


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.chat_activity);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false); // Disable the default title


        edtMessage = findViewById(R.id.edtmessage);
        btnSend = findViewById(R.id.btnsend);
        recyclerViewMessages = findViewById(R.id.recyclerViewMessages);
        receiverNameTextView = findViewById(R.id.receiverNameTextView);


        firestore = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        // Initialize the message list and adapter
        messageList = new ArrayList<>();
        messageAdapter = new MessageAdapter(messageList, auth.getCurrentUser().getUid());

        // Set up RecyclerView
        recyclerViewMessages.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewMessages.setAdapter(messageAdapter);

        // Get receiver UID from intent
        receiverUid = getIntent().getStringExtra("receiverUid");
        receiverName = getIntent().getStringExtra("receiverName");


        if (receiverUid == null) {
            Toast.makeText(this, "Receiver UID is null", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set the receiver name in the TextView
        if (receiverName != null) {
            receiverNameTextView.setText(receiverName);
        }


        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String message = edtMessage.getText().toString().trim();
                if (!message.isEmpty()) {
                    sendMessageToReceiver(message);
                    edtMessage.setText(""); // Clear the EditText after sending the message
                }
            }
        });

        loadMessages();
    }

    private void sendMessageToReceiver(String message) {
        String senderUid = auth.getCurrentUser().getUid();
        Timestamp timestamp = new Timestamp(new Date());

        // Create a new message document
        Map<String, Object> messageData = new HashMap<>();
        messageData.put("senderId", senderUid);
        messageData.put("receiverId", receiverUid);
        messageData.put("timestamp", timestamp);
        messageData.put("message", message);

        // Add the message to Firestore
        firestore.collection("user_messages")
                .add(messageData)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        // Message sent successfully
                        Toast.makeText(ChatActivity.this, "Message sent", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        // Error sending message
                        Toast.makeText(ChatActivity.this, "Failed to send message", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void loadMessages() {
        String currentUserUid = auth.getCurrentUser().getUid();

        // Query messages where current user is the sender and receiver is the recipient
        firestore.collection("user_messages")
                .whereEqualTo("senderId", currentUserUid)
                .whereEqualTo("receiverId", receiverUid)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Toast.makeText(ChatActivity.this, "Error loading messages: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (queryDocumentSnapshots != null) {
                            messageList.clear();
                            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                Message message = document.toObject(Message.class);
                                messageList.add(message);
                            }
                            messageAdapter.notifyDataSetChanged();
                        }
                    }
                });

        // Query messages where current user is the receiver and sender is the recipient
        firestore.collection("user_messages")
                .whereEqualTo("receiverId", currentUserUid)
                .whereEqualTo("senderId", receiverUid)
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot queryDocumentSnapshots, @Nullable FirebaseFirestoreException e) {
                        if (e != null) {
                            Toast.makeText(ChatActivity.this, "Error loading messages: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (queryDocumentSnapshots != null) {
                            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                Message message = document.toObject(Message.class);
                                messageList.add(message);
                            }
                            messageAdapter.notifyDataSetChanged();
                        }
                    }
                });
    }
}
