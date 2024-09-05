package com.example.missingperson;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.airbnb.lottie.LottieAnimationView;

public class Splachscreen extends AppCompatActivity {
    TextView txtwelcome;
    LottieAnimationView animationView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        txtwelcome = findViewById(R.id.txtwelcome);
        animationView = findViewById(R.id.animationView);

        // Check user authentication status using SharedPreferences
        SharedPreferences preferences = getSharedPreferences("MyPrefs", MODE_PRIVATE);
        boolean isLoggedIn = preferences.getBoolean("isLoggedIn", false);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isLoggedIn) {
                    // User is logged in, go to HomeActivity
                    Intent homeIntent = new Intent(Splachscreen.this, HomeActivity.class);
                    startActivity(homeIntent);
                } else {
                    // User is not logged in, go to LoginActivity
                    Intent loginIntent = new Intent(Splachscreen.this, LoginActivity.class);
                    startActivity(loginIntent);
                }
                finish(); // Close the splash screen activity
            }
        }, 1000);
    }
}
