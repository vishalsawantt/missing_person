package com.example.missingperson;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class DetailsPopupFragment extends DialogFragment {

    private static final String ARG_DETAILS_TEXT = "details_text";

    private TextView textDetails;
    private ImageButton btnClose;

    public DetailsPopupFragment() {
        // Required empty public constructor
    }

    public static DetailsPopupFragment newInstance(String detailsText) {
        DetailsPopupFragment fragment = new DetailsPopupFragment();
        Bundle args = new Bundle();
        args.putString(ARG_DETAILS_TEXT, detailsText);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.popup_layout, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        textDetails = view.findViewById(R.id.textDetails);
        btnClose = view.findViewById(R.id.btnClose);

        if (getArguments() != null) {
            String detailsText = getArguments().getString(ARG_DETAILS_TEXT);
            textDetails.setText(detailsText);
        }

        btnClose.setOnClickListener(v -> dismiss());
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}
