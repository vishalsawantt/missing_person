package com.example.missingperson;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class HomeActivity extends AppCompatActivity {
    private BottomNavigationView bottomNavigationView;
    private FragmentManager fragmentManager;
    private HomeFragment homeFragment;
    private SearchFragment searchFragment;
    private ProfileFragment profileFragment;
    private MyReportsFragment myreportsFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.home);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setLabelVisibilityMode(NavigationBarView.LABEL_VISIBILITY_LABELED);

        fragmentManager = getSupportFragmentManager();

        homeFragment = new HomeFragment();
        searchFragment = new SearchFragment();
        profileFragment = new ProfileFragment();
        myreportsFragment = new MyReportsFragment();

        // Check the intent for the fragment to open
        String openFragment = getIntent().getStringExtra("openFragment");
        if (openFragment != null && openFragment.equals("SearchFragment")) {
            replaceFragment(searchFragment);
        } else {
            // Set the initial fragment (replace this with your default fragment)
            replaceFragment(homeFragment);
        }

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();
                if (itemId == R.id.homef) {
                    replaceFragment(homeFragment);
                    return true;
                } else if (itemId == R.id.seaf) {
                    replaceFragment(searchFragment);
                    return true;
                } else if (itemId == R.id.proff) {
                    replaceFragment(profileFragment);
                    return true;
                } else if (itemId == R.id.repf) {
                    replaceFragment(myreportsFragment);
                    return true;
                } else
                    return false;
            }
        });
    }

    private void replaceFragment(Fragment fragment) {
        if (fragment != null) {
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }
}
