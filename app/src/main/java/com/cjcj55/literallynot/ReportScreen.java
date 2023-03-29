package com.cjcj55.literallynot;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.android.volley.Response;
import com.cjcj55.literallynot.databinding.MainscreenuiBinding;
import com.cjcj55.literallynot.databinding.ReportscreenuiBinding;
import com.cjcj55.literallynot.db.MySQLHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ReportScreen extends Fragment {
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
                NavHostFragment.findNavController(ReportScreen.this)
                        .navigate(R.id.action_ReportScreen_to_MainScreen);
            }
        });


        // Make the network request to get all users
        showLoadingIndicator();
        MySQLHelper.getAllUsers(getContext(), new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
                    hideLoadingIndicator();
                    JSONArray jsonArray = new JSONArray(response);
                    // Iterate over the array and extract user data
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        String username = jsonObject.getString("username");
                        String firstName = jsonObject.getString("firstName");
                        String lastName = jsonObject.getString("lastName");
                        String email = jsonObject.getString("email");
                        // Display the user data on the UI
                        binding.userTextView.append(username + ", " + firstName + " " + lastName + ", " + email + "\n");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    private void showLoadingIndicator() {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.userTextView.setVisibility(View.INVISIBLE);
    }

    private void hideLoadingIndicator() {
        binding.progressBar.setVisibility(View.GONE);
        binding.userTextView.setVisibility(View.VISIBLE);
    }

}


