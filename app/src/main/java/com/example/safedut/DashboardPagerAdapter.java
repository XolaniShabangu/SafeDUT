package com.example.safedut;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class DashboardPagerAdapter extends FragmentStateAdapter {

    private String userRole;

    public DashboardPagerAdapter(@NonNull FragmentActivity fragmentActivity, String userRole) {
        super(fragmentActivity);
        this.userRole = userRole;
    }



    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new HomeFragment();
            case 1:
                return new IncidentFragment();
            case 2:
                return new ChatFragment();
            case 3:
                return new SafetyFragment();
            case 4:
                return MoreFragment.newInstance(userRole);
            default:
                return new HomeFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 5;
    }
}
