package com.example.missingperson;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.google.android.material.tabs.TabLayout;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SearchFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private CompareFragment compareFragment;
    private ChatListFragment chatListFragment;

    private static final int MAX_POPUP_SHOW_COUNT = 3;
    private static final String PREFS_NAME = "PopupPrefs";
    private static final String PREF_DATE = "lastShownDate";
    private static final String PREF_COUNT = "popupShowCount";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        viewPager = view.findViewById(R.id.view_pager);
        tabLayout = view.findViewById(R.id.tab_layout);

        setupViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);

        // Show instructions popup if allowed
        if (shouldShowPopup()) {
            showInstructionsPopup();
        }

        return view;
    }

    private void setupViewPager(ViewPager viewPager) {
        ViewPagerAdapter adapter = new ViewPagerAdapter(getChildFragmentManager());
        compareFragment = new CompareFragment();
        chatListFragment = new ChatListFragment();
        adapter.addFragment(compareFragment, "Compare");
        adapter.addFragment(chatListFragment, "Chat List");
        viewPager.setAdapter(adapter);
    }

    private void showInstructionsPopup() {
        String instructionsText = getInstructionsText();
        DialogFragment detailsPopupFragment = DetailsPopupFragment.newInstance(instructionsText);
        detailsPopupFragment.show(getParentFragmentManager(), "DetailsPopupFragment");
    }

    private String getInstructionsText() {
        return "Instructions:\n\n" +
                "1. Select Location: Tap the 'Select Location' button to mark the place where you are currently located on the map.\n" +
                "2. Enter Your Address: Provide your current address details accurately.\n" +
                "3. Capture the Image: Use the camera to capture the image of the person and save it to your device.\n" +
                "4. Choose the Image: Select the captured image from your gallery.\n" +
                "5. Start Search: Tap the 'Search' button to begin the search process.\n" +
                "6. View Match Details: If a match is found, details about the matched person and their contact information will be displayed.\n" +
                "7. Send Message: To communicate with the person who reported the match, tap 'Send Message'.\n" +
                "8. Access Chats: Swipe right to open the chats section and continue the conversation.\n";
    }

    private boolean shouldShowPopup() {
        SharedPreferences prefs = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String lastShownDate = prefs.getString(PREF_DATE, "");
        int showCount = prefs.getInt(PREF_COUNT, 0);

        // Get today's date as a string
        String todayDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(new Date());

        if (!todayDate.equals(lastShownDate)) {
            // New day, reset count
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(PREF_DATE, todayDate);
            editor.putInt(PREF_COUNT, 1); // First time showing today
            editor.apply();
            return true;
        } else if (showCount < MAX_POPUP_SHOW_COUNT) {
            // Same day, increment count
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(PREF_COUNT, showCount + 1);
            editor.apply();
            return true;
        } else {
            // Already shown maximum times today
            return false;
        }
    }

    private static class ViewPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> fragmentList = new ArrayList<>();
        private final List<String> fragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(@NonNull FragmentManager fm) {
            super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        public void addFragment(Fragment fragment, String title) {
            fragmentList.add(fragment);
            fragmentTitleList.add(title);
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitleList.get(position);
        }
    }
}
