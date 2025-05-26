package com.example.safedut;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.fragment.app.Fragment;
import androidx.cardview.widget.CardView;
import android.widget.TextView;

public class SafetyFragment extends Fragment {

    public SafetyFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_safety, container, false);


        CardView fireCard = rootView.findViewById(R.id.fireCard);
        CardView medicalCard = rootView.findViewById(R.id.medicalCard);
        CardView suspiciousCard = rootView.findViewById(R.id.suspiciousActivityCard);
        CardView stalkingCard = rootView.findViewById(R.id.stalkingCard);
        CardView lateTravelCard = rootView.findViewById(R.id.travelingLateCard);


        setCollapsibleCard(fireCard, R.id.fireContent);
        setCollapsibleCard(medicalCard, R.id.medicalContent);
        setCollapsibleCard(suspiciousCard, R.id.suspiciousActivityContent);
        setCollapsibleCard(stalkingCard, R.id.stalkingContent);
        setCollapsibleCard(lateTravelCard, R.id.travelingLateContent);


        Button callAmbulanceButton = rootView.findViewById(R.id.callAmbulanceButton);
        Button callPoliceButton = rootView.findViewById(R.id.callPoliceButton);
        Button callFireButton = rootView.findViewById(R.id.callFireButton);


        callAmbulanceButton.setOnClickListener(v -> callEmergency("10177"));
        callPoliceButton.setOnClickListener(v -> callEmergency("10111"));
        callFireButton.setOnClickListener(v -> callEmergency("998"));

        return rootView;
    }

    private void setCollapsibleCard(CardView card, int contentId) {
        TextView contentView = card.findViewById(contentId);


        contentView.setVisibility(View.GONE);


        card.setOnClickListener(v -> {

            if (contentView.getVisibility() == View.GONE) {
                contentView.setVisibility(View.VISIBLE);
            } else {
                contentView.setVisibility(View.GONE);
            }

            card.requestLayout();
        });
    }

    private void callEmergency(String emergencyNumber) {
        Intent callIntent = new Intent(Intent.ACTION_DIAL);
        callIntent.setData(Uri.parse("tel:" + emergencyNumber));
        startActivity(callIntent);
    }
}

