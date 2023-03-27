package com.cjcj55.literallynot;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.cjcj55.literallynot.databinding.AccountcreationscreenuiBinding;
import com.cjcj55.literallynot.db.MySQLHelper;


public class AccountCreationScreen extends Fragment {
    private AccountcreationscreenuiBinding binding;
    private EditText editUserName;
    private EditText editEmail;
    private EditText editPassword;
    private EditText editFirstName;
    private EditText editLastName;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {

        binding = AccountcreationscreenuiBinding.inflate(inflater, container, false);
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        editUserName = binding.editTextTextUsername;
        editEmail = binding.editTextTextNewEmailAddress;
        editPassword = binding.editTextTextNewPassword;
        editFirstName = binding.editTextTextFirstName;
        editLastName = binding.editTextTextLastName;

        binding.createNewAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (checkInputs(getUsername(), getNewPassword(), getNewEmail(), getFirstName(), getLastName(), view)) {
                    MySQLHelper.registerAccount(getUsername(), getNewPassword(), getNewEmail(), getFirstName(), getLastName(), getContext());
                    NavHostFragment.findNavController(AccountCreationScreen.this)
                            .navigate(R.id.action_AccountCreationScreen_to_LoginScreen);
                }
            }
        });

        binding.backtologbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(AccountCreationScreen.this)
                        .navigate(R.id.action_AccountCreationScreen_to_LoginScreen);
            }
        });

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private String getUsername() {
        return binding.editTextTextUsername.getText().toString();
    }
    private String getNewEmail () {
        return binding.editTextTextNewEmailAddress.getText().toString();
    }
    private String getNewPassword () {
        return binding.editTextTextNewPassword.getText().toString();
    }
    private String getFirstName () {
        return binding.editTextTextFirstName.getText().toString();
    }
    private String getLastName () {
        return binding.editTextTextLastName.getText().toString();
    }

    private boolean checkInputs(String username, String password, String email, String firstName, String lastName, View view) {
        boolean check = true;

        if (username.isBlank()) {
            binding.usernameErrorText.setText("This Field is Required");
            binding.usernameErrorText.setVisibility(view.VISIBLE);
            check = false;
        } else {
            binding.usernameErrorText.setVisibility(view.INVISIBLE);
        }
        if (password.isBlank()) {
            binding.passwordErrorText.setText("This Field is Required");
            binding.passwordErrorText.setVisibility(view.VISIBLE);
            check = false;
        } else {
            binding.passwordErrorText.setVisibility(view.INVISIBLE);
        }
        if (email.isBlank()) {
            binding.emailErrorText.setText("This Field is Required");
            binding.emailErrorText.setVisibility(view.VISIBLE);
            check = false;
        } else if (!email.contains("@") || !email.contains(".")) {
            binding.emailErrorText.setText("Invalid Email Format");
            binding.emailErrorText.setVisibility(view.VISIBLE);
            check = false;
        } else {
            binding.emailErrorText.setVisibility(view.INVISIBLE);
        }
        if (firstName.isBlank()) {
            binding.firstnameErrorText.setText("This Field is Required");
            binding.firstnameErrorText.setVisibility(view.VISIBLE);
            check = false;
        } else {
            binding.firstnameErrorText.setVisibility(view.INVISIBLE);
        }
        if (lastName.isBlank()) {
            binding.lastnameErrorText.setText("This Field is Required");
            binding.lastnameErrorText.setVisibility(view.VISIBLE);
            check = false;
        } else {
            binding.lastnameErrorText.setVisibility(view.INVISIBLE);
        }
        return check;
    }

}


