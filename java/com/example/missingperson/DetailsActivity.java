package com.example.missingperson;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class DetailsActivity extends AppCompatActivity {
    TextView txtname, txtgender, txtage, txtdate, txtlocation, txtdescription, txtreportno, txtrelationship, txtreward, txtcontact;
    Button btnseen,btnback;
    private FirebaseAuth mAuth;
    String receivedData ;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.details_window);

        txtname = findViewById(R.id.txtname);
        txtgender = findViewById(R.id.txtgender);
        txtage = findViewById(R.id.txtage);
        txtdate = findViewById(R.id.txtdate);
        txtlocation = findViewById(R.id.txtlocation);
        txtdescription = findViewById(R.id.txtdescription);
        txtreportno = findViewById(R.id.txtreportno);
        txtrelationship = findViewById(R.id.txtrelationship);
        txtreward = findViewById(R.id.txtreward);
        txtcontact = findViewById(R.id.txtcontact);
        btnseen = findViewById(R.id.btnseen);
        btnback = findViewById(R.id.btnback);

        btnback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailsActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });

        btnseen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DetailsActivity.this, HomeActivity.class);
                intent.putExtra("openFragment", "SearchFragment");
                startActivity(intent);
            }
        });

        mAuth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        if (intent.hasExtra("RID")) {
            receivedData = intent.getStringExtra("RID");
            System.out.println("RID-----"+receivedData);
            getdata("userReports/" + receivedData);
            // Now, receivedData contains the string sent from the first activity
            // You can use receivedData as needed in the second activity
        } else {
            // Handle the case where the key is not present in the Intent
        }
    }

    public void getdata(String url) {
        try {
            FirebaseDatabase mFirebaseInstance = FirebaseDatabase.getInstance();
            DatabaseReference mFirebaseDatabase = mFirebaseInstance.getReference(url);
            mFirebaseDatabase.keepSynced(true);
            mFirebaseDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    System.out.println("is req----------------" + dataSnapshot.toString());
                    if (dataSnapshot.exists()) {
                        HashMap<String, Object> yourData = (HashMap<String, Object>) dataSnapshot.getValue();

                        // Now set the values to your TextViews
                        txtname.setText(yourData.get("name").toString());
                        txtgender.setText(yourData.get("gender").toString());
                        txtage.setText(yourData.get("age").toString());
                        txtdate.setText(yourData.get("missdate").toString());
                        txtlocation.setText(yourData.get("lastlocation").toString());
                        txtdescription.setText(yourData.get("clothdes").toString());
                        txtreportno.setText(yourData.get("policreportno").toString());
                        txtrelationship.setText(yourData.get("relationship").toString());
                        txtreward.setText(yourData.get("reward").toString());
                        txtcontact.setText(yourData.get("contact").toString());
                    }
                }

                @Override
                public void onCancelled(DatabaseError error) {
                    // Handle any errors here
                    System.out.println("Database Error: " + error.getMessage());
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
