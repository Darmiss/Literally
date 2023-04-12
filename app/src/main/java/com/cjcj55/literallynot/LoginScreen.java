package com.cjcj55.literallynot;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import android.content.Context;


import com.cjcj55.literallynot.databinding.LoginscreenuiBinding;
import com.cjcj55.literallynot.databinding.MainscreenuiBinding;
import com.cjcj55.literallynot.db.AudioUploadCallback;
import com.cjcj55.literallynot.db.LoginCallback;
import com.cjcj55.literallynot.db.MySQLHelper;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class LoginScreen extends Fragment {

    private LoginscreenuiBinding binding;
    private EditText editUserNameOrEmail;
    private EditText editPassword;
    public int launchprior;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {



        binding = LoginscreenuiBinding.inflate(inflater, container, false);
        return binding.getRoot();



     }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        editUserNameOrEmail = binding.editUser;
        editPassword = binding.editTextTextPassword;


       System.out.println(checkPermissions());



        // Stop the foreground service
        Intent intent = new Intent(getActivity(), ForegroundService.class);
        getActivity().stopService(intent);


        binding.LoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
 //               if (checkInputs(getUsernameOrEmail(), getPassword(), view)) {
//                    MySQLHelper.login(getUsernameOrEmail(), getPassword(), getContext(), getActivity(), new LoginCallback() {
                    MySQLHelper.login("cjcj55", "password", getContext(), getActivity(), new LoginCallback() {
                        @Override
                        public void onSuccess(int userId, String username, String firstName, String lastName) {
                            launchprior = 1;
                            NavHostFragment.findNavController(LoginScreen.this)
                                    .navigate(R.id.action_LoginScreen_to_MainScreen);
                            System.out.println(launchprior);
                            SharedPreferences sharedPreferences = getActivity().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.putBoolean("isLoggedIn", true); // set isLoggedIn as true
                            editor.commit(); // commit the changes
                        }

                        @Override
                        public void onFailure() {

                        }
                    });
//                }
            }
        });


        binding.ToAccCreationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String userId = "000";
                Uri audioFileUri = Uri.fromFile(new File(requireContext().getCacheDir(), "audio_file.mp3"));
                Context context = requireContext();
                AudioUploadCallback audioUploadCallback = new AudioUploadCallback() {
                    @Override
                    public void onSuccess() {
                        // Handle success
                        System.out.println("WWWW");
                    }

                    @Override
                    public void onFailure() {
                        System.out.println("LLL");
                        // Handle failure
                    }
                };

                NavHostFragment.findNavController(LoginScreen.this)
                        .navigate(R.id.action_LoginScreen_to_AccountCreationScreen);
            }
        });
    }

    private boolean checkInputs(String usernameOrEmail, String password, View view) {
        boolean check = true;

        if (usernameOrEmail.isBlank()) {
            binding.usernameErrorText.setText("This Field is Required");
            binding.usernameErrorText.setVisibility(view.VISIBLE);
            check = false;
        } else {
            binding.usernameErrorText.setVisibility(view.INVISIBLE);
        }
        if (password.isBlank()) {
            binding.passwordErrorText.setText("This Field is Required");
            binding.passwordErrorText.setVisibility(view.VISIBLE);
        } else {
            binding.passwordErrorText.setVisibility(view.INVISIBLE);
        }
        return check;
    }

    private String getUsernameOrEmail() {
        return binding.editUser.getText().toString();
    }

    private String getPassword() {
        return binding.editTextTextPassword.getText().toString();
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
    private boolean checkPermissions() {
        // Check if all the necessary permissions are granted
        boolean isPermissionGranted = true;
        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED) {
            isPermissionGranted = false;
        }
        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED) {
            isPermissionGranted = false;
        }
        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            isPermissionGranted = false;
        }
        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            isPermissionGranted = false;
        }
        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.WAKE_LOCK) != PackageManager.PERMISSION_GRANTED) {
            isPermissionGranted = false;
        }
        if (ActivityCompat.checkSelfPermission(requireContext(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            isPermissionGranted = false;
        }
        if (ActivityCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_NOTIFICATION_POLICY) != PackageManager.PERMISSION_GRANTED) {
            isPermissionGranted = false;
        }
        // Add checks for other permissions here...
        return isPermissionGranted;
    }


}