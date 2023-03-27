package com.cjcj55.literallynot;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;


import com.cjcj55.literallynot.databinding.LoginscreenuiBinding;
import com.cjcj55.literallynot.db.LoginCallback;
import com.cjcj55.literallynot.db.MySQLHelper;

public class LoginScreen extends Fragment {

    private LoginscreenuiBinding binding;
    private EditText editUserNameOrEmail;
    private EditText editPassword;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = LoginscreenuiBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        editUserNameOrEmail = binding.editUser;
        editPassword = binding.editTextTextPassword;



        // Stop the foreground service
        Intent intent = new Intent(getActivity(), ForegroundService.class);
        getActivity().stopService(intent);


        binding.LoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkInputs(getUsernameOrEmail(), getPassword(), view)) {
                    MySQLHelper.login(getUsernameOrEmail(), getPassword(), getContext(), getActivity(), new LoginCallback() {
                        @Override
                        public void onSuccess(int userId, String username, String firstName, String lastName) {
                            NavHostFragment.findNavController(LoginScreen.this)
                                    .navigate(R.id.action_LoginScreen_to_MainScreen);
                        }

                        @Override
                        public void onFailure() {

                        }
                    });
                }
            }
        });


        binding.ToAccCreationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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

}