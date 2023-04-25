package com.cjcj55.literallynot;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.cjcj55.literallynot.databinding.MainscreenuiBinding;
import com.cjcj55.literallynot.db.AudioClip;
import com.cjcj55.literallynot.db.AudioListAdapter;
import com.cjcj55.literallynot.db.MySQLHelper;
import com.facebook.FacebookSdk;
import com.facebook.share.model.ShareHashtag;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareButton;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainScreen extends Fragment {

    private MainscreenuiBinding binding;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;
    private boolean isServiceRunning;
    // Declare a SharedPreferences object
    private SharedPreferences sharedPref;
    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = MainscreenuiBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        // Initialize the SharedPreferences object
        sharedPref = getActivity().getSharedPreferences("UserPref",Context.MODE_PRIVATE);


        // Retrieve the saved state of the isServiceRunning variable
        isServiceRunning = sharedPref.getBoolean("isServiceRunning", false);

        // Set the button text based on the saved state
        if (isServiceRunning) {
            binding.startStopForegroundService.setText("Stop Listening");
        } else {
            binding.startStopForegroundService.setText("Start Listening");
        }

        // Initialize the RecyclerView and SwipeRefreshLayout
        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

        // Create an empty AudioListAdapter and set it to the RecyclerView
        List<AudioClip> emptyList = new ArrayList<>();
        AudioListAdapter adapter = new AudioListAdapter(getActivity(), emptyList);
        recyclerView.setAdapter(adapter);

        // Set the SwipeRefreshLayout listener
        swipeRefreshLayout.setOnRefreshListener(() -> {
            MySQLHelper.downloadAndConvertMP3s(getActivity(), recyclerView, swipeRefreshLayout);
        });

        // Trigger the SwipeRefreshLayout to load data initially
        swipeRefreshLayout.post(() -> {
            swipeRefreshLayout.setRefreshing(true);
            MySQLHelper.downloadAndConvertMP3s(getActivity(), recyclerView, swipeRefreshLayout);
        });

        binding.startStopForegroundService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent forIntent = new Intent(getActivity(), ForegroundService.class);

                if (!isServiceRunning) {
                    // Start the foreground service
                    getActivity().startService(forIntent);
                    binding.startStopForegroundService.setText("Stop Listening");
                    isServiceRunning = true;
                } else {
                    // Stop the foreground service
                    getActivity().stopService(forIntent);
                    binding.startStopForegroundService.setText("Start Listening");
                    isServiceRunning = false;
                }

                // Save the state of the isServiceRunning variable
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putBoolean("isServiceRunning", isServiceRunning);
                editor.apply();
            }
        });

        binding.reportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(MainScreen.this)
                        .navigate(R.id.action_MainScreen_to_ReportScreen);
            }
        });

        binding.logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(MainScreen.this)
                        .navigate(R.id.action_MainScreen_to_accountMenu);
            }
        });

        binding.scorebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(MainScreen.this)
                        .navigate(R.id.action_MainScreen_to_scoreboard);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}