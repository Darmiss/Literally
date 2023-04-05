package com.cjcj55.literallynot;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.cjcj55.literallynot.databinding.ReportscreenuiBinding;
import com.cjcj55.literallynot.db.MySQLHelper;


public class ProfileScreen extends Fragment {

    private ReportscreenuiBinding binding;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = ReportscreenuiBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.reportBckBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(ProfileScreen.this)
                        .navigate(R.id.action_ReportScreen_to_MainScreen);
            }
        });
        binding.imageButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(ProfileScreen.this)
                        .navigate(R.id.action_ReportScreen_to_MainScreen);
            }
        });
        binding.logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MySQLHelper.logout(getContext(), ProfileScreen.this);
            }
        });


    }
}