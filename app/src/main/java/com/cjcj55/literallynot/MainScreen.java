package com.cjcj55.literallynot;

import static androidx.core.content.PermissionChecker.checkCallingOrSelfPermission;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.PermissionChecker;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.cjcj55.literallynot.databinding.MainscreenuiBinding;
import com.cjcj55.literallynot.db.AudioFile;
import com.cjcj55.literallynot.db.MySQLHelper;
import com.facebook.FacebookSdk;
import com.facebook.share.model.ShareHashtag;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareButton;

import org.vosk.android.StorageService;

import java.io.File;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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

        // Initialize the Facebook SDK
        FacebookSdk.sdkInitialize(getContext());

        // Start the foreground service
        Intent intent = new Intent(getActivity(), ForegroundService.class);
        getActivity().startService(intent);



        binding.sendNotificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadFile();
            }
        });

        // Create a ShareLinkContent object with the message and hashtag you want to share
        ShareLinkContent content = new ShareLinkContent.Builder()
                .setContentUrl(Uri.parse("https://www.example.com"))
                .setQuote("Check out this audio!")
                .setShareHashtag(new ShareHashtag.Builder()
                        .setHashtag("#LiterallyNot")
                        .build())
                .build();

        // Get a reference to the ShareButton view
        ShareButton shareButton = binding.fbButton;




        // Set the ShareContent on the ShareButton
        shareButton.setShareContent(content);

        // Set a click listener on the ShareButton
        shareButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /// Disable the button
                binding.fbButton.setEnabled(false);

                // Share the message and hashtag
                ShareHashtag hashtag = new ShareHashtag.Builder()
                        .setHashtag("#yourhashtag")
                        .build();
                ShareLinkContent content = new ShareLinkContent.Builder()
                        .setQuote("your message")
                        .setShareHashtag(hashtag)
                        .build();
                ShareButton shareButton = new ShareButton(getContext());
                shareButton.setShareContent(content);
                shareButton.performClick();

                // Re-enable the button after a short delay
                view.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        binding.fbButton.setEnabled(true);
                    }
                }, 1000); // Delay time in milliseconds
            }
        });

        binding.reportBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(MainScreen.this)
                        .navigate(R.id.action_MainScreen_to_ReportScreen);
            }
        });

        binding.profileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(MainScreen.this)
                        .navigate(R.id.action_MainScreen_to_profileScreen);
            }
        });

        /*binding.logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MySQLHelper.logout(getContext(), MainScreen.this);
            }
        });*/

//        MySQLHelper.readAudioFiles(getContext(), new Callback<List<AudioFile>>() {
//            @Override
//            public void onResponse(Call<List<AudioFile>> call, retrofit2.Response<List<AudioFile>> response) {
//
//            }
//
//            @Override
//            public void onFailure(Call<List<AudioFile>> call, Throwable t) {
//                // handle error
//            }
//        });
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
        File file = new File(getActivity().getCacheDir(), "bean.mp3");
        MySQLHelper.writeAudioFile(getContext(), file);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}