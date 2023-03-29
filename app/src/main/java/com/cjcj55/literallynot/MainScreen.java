package com.cjcj55.literallynot;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;


import com.cjcj55.literallynot.databinding.MainscreenuiBinding;
import com.cjcj55.literallynot.db.MySQLHelper;

import java.io.File;

public class MainScreen extends Fragment {

    private MainscreenuiBinding binding;

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


        // Start the foreground service
        Intent intent = new Intent(getActivity(), ForegroundService.class);
        getActivity().startService(intent);

        binding.sendNotificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadFile();
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
                MySQLHelper.logout(getContext(), MainScreen.this);
            }
        });


    }

    private void sendPushNotification() {
        // Get a reference to the parent activity
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            // Call the createNotification() method on the activity reference
            activity.createNotification();
        }
    }

    private void uploadFile() {
        // Get the resource ID of the audio file
        int audioResourceId = getResources().getIdentifier("test_sound", "raw", getActivity().getPackageName());

        // Load the audio file as a Uri object
        Uri audioUri = Uri.parse("android.resource://" + getActivity().getPackageName() + "/" + audioResourceId);

        File audioFile = new File(audioUri.getPath());
        MySQLHelper.writeAudioFileForUser(getContext(), audioFile);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }



}