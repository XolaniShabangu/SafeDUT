package com.example.safedut;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import android.content.Intent;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import androidx.fragment.app.FragmentTransaction;


public class MoreFragment extends Fragment {

    private static final String ARG_ROLE = "userRole";
    private String userRole;


    public static MoreFragment newInstance(String userRole) {
        MoreFragment fragment = new MoreFragment();
        Bundle args = new Bundle();
        args.putString(ARG_ROLE, userRole);
        fragment.setArguments(args);
        return fragment;
    }

    public MoreFragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            userRole = getArguments().getString(ARG_ROLE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_more, container, false);


        setupUI(rootView);

        return rootView;
    }

    private void setupUI(View rootView) {
        Button logoutButton = rootView.findViewById(R.id.logoutButton);
        logoutButton.setOnClickListener(v -> logoutUser());


        if ("admin".equals(userRole) || "staff".equals(userRole)) {

            rootView.findViewById(R.id.liveChatButton).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.lockdownButton).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.noticesButton).setVisibility(View.VISIBLE);
            rootView.findViewById(R.id.reportsButton).setVisibility(View.VISIBLE);


            rootView.findViewById(R.id.liveChatButton).setOnClickListener(v -> openLiveChatAdminPanel());
            rootView.findViewById(R.id.lockdownButton).setOnClickListener(v -> openLockdownProtocols());
            rootView.findViewById(R.id.noticesButton).setOnClickListener(v -> openNotices());
            rootView.findViewById(R.id.reportsButton).setOnClickListener(v -> openReportsPage());
        } else {

            rootView.findViewById(R.id.liveChatButton).setVisibility(View.GONE);
            rootView.findViewById(R.id.lockdownButton).setVisibility(View.GONE);
            rootView.findViewById(R.id.noticesButton).setVisibility(View.GONE);
            rootView.findViewById(R.id.reportsButton).setVisibility(View.GONE);
        }
    }


    private void logoutUser() {

        FirebaseAuth.getInstance().signOut();


        Toast.makeText(getActivity(), "Logging out...", Toast.LENGTH_SHORT).show();


        startActivity(new Intent(getActivity(), LoginActivity.class));
        getActivity().finish();
    }


    private void openLiveChatAdminPanel() {
        Intent intent = new Intent(getActivity(), AdminChatListActivity.class);
        startActivity(intent);
    }


    private void openLockdownProtocols() {

        Intent intent = new Intent(getActivity(), LockdownProtocolsActivity.class);
       startActivity(intent);
    }





    private void openReportsPage() {
        Intent intent = new Intent(getActivity(), AdminReportsActivity.class);
        startActivity(intent);
    }

    private void openNotices() {
        Intent intent = new Intent(getActivity(), NoticesActivity.class);
        startActivity(intent);
    }



}
