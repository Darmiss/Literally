package com.cjcj55.literallynot;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Response;
import com.cjcj55.literallynot.databinding.ReportscreenuiBinding;
import com.cjcj55.literallynot.db.MySQLHelper;
import com.cjcj55.literallynot.db.WeekDataCallback;
import com.facebook.FacebookSdk;
import com.facebook.share.model.ShareContent;
import com.facebook.share.model.ShareHashtag;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareButton;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ReportScreen extends Fragment {
    private ReportscreenuiBinding binding;
    private SwipeRefreshLayout swipeRefreshLayout;
    private LineChart lineChart;

    public int totalsum = 0;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = ReportscreenuiBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        swipeRefreshLayout = getView().findViewById(R.id.swipeRefreshLayout);
        lineChart = getView().findViewById(R.id.lineChart);

        // populate the line chart with data
        populateLineChart();

        // set an OnRefreshListener on the SwipeRefreshLayout to refresh the chart when the user swipes down
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                populateLineChart();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        binding.imageButton3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(ReportScreen.this)
                        .navigate(R.id.action_ReportScreen_to_MainScreen);
            }
        });
        binding.logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(ReportScreen.this)
                        .navigate(R.id.action_ReportScreen_to_accountMenu);
            }
        });

        binding.scorebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(ReportScreen.this)
                        .navigate(R.id.action_ReportScreen_to_scoreboard);
            }
        });


        FacebookSdk.sdkInitialize(getContext());


        // Create a ShareLinkContent object with the message and hashtag you want to share
        ShareLinkContent content = new ShareLinkContent.Builder()
                .setContentUrl(Uri.parse("www.example.com"))
                .setQuote("I said literally " + totalsum + " times all time"  )
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

                ShareLinkContent content = new ShareLinkContent.Builder()
                        .setContentUrl(Uri.parse("www.example.com"))
                        .setQuote("I said literally " + totalsum + " times this week"   )
                        .setShareHashtag(new ShareHashtag.Builder()
                                .setHashtag("#LiterallyNot")
                                .build())
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

        // Make the network request to get all users
//        showLoadingIndicator();
//        MySQLHelper.getAllUsers(getContext(), new Response.Listener<String>() {
//            @Override
//            public void onResponse(String response) {
//                try {
//                    hideLoadingIndicator();
//                    JSONArray jsonArray = new JSONArray(response);
//                    // Iterate over the array and extract user data
//                    for (int i = 0; i < jsonArray.length(); i++) {
//                        JSONObject jsonObject = jsonArray.getJSONObject(i);
//                        String username = jsonObject.getString("username");
//                        String firstName = jsonObject.getString("firstName");
//                        String lastName = jsonObject.getString("lastName");
//                        String email = jsonObject.getString("email");
//                        // Display the user data on the UI
//                        binding.userTextView.append(username + ", " + firstName + " " + lastName + ", " + email + "\n");
//                    }
//                } catch (JSONException e) {
//                    e.printStackTrace();
//                }
//            }
//        });
    }

    private void updateLineChart(List<String> datetimeList, SimpleDateFormat sdf) {
        // create a map to store the sum of dates for each day of the week
        Map<Integer, Integer> sumMap = new HashMap<>();

        // iterate over the date strings and calculate the sum of dates for each day of the week
        for (String datetime : datetimeList) {
            try {
                // parse the date string into a Date object
                Date date = sdf.parse(datetime);

                // create a Calendar object and set it to the parsed date
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(date);

                // get the day of the week for the date (1-7, where 1 is Sunday)
                int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

                // add the date to the sum for the corresponding day of the week
                if (sumMap.containsKey(dayOfWeek)) {
                    int sum = sumMap.get(dayOfWeek);
                    sumMap.put(dayOfWeek, sum + 1);


                } else {
                    sumMap.put(dayOfWeek, 1);
                }


            } catch (ParseException e) {
                // handle parsing error
                e.printStackTrace();
            }
        }

        // create a list of Entry objects representing the data points
        List<Entry> entries = new ArrayList<>();

        // iterate over the days of the week and add a data point for each day
        for (int i = 1; i <= 7; i++) {
            // get the sum of dates for the day of the week, or 0 if there are no dates
            int sum = sumMap.containsKey(i) ? sumMap.get(i) : 0;

            // create an entry with the day of the week and the sum of dates
            entries.add(new Entry(i, sum));
            totalsum = totalsum + sum;

        }
        System.out.println("Totalsum = " + totalsum);

        // create a LineDataSet from the entries
        LineDataSet dataSet = new LineDataSet(entries, "Data Set");

        // customize the appearance of the LineDataSet
        dataSet.setColor(Color.RED);
        dataSet.setLineWidth(2f);
        dataSet.setCircleColor(Color.RED);
        dataSet.setCircleRadius(5f);
        dataSet.setDrawValues(false);

        // create a LineData object from the LineDataSet
        LineData lineData = new LineData(dataSet);

        // set the LineData object to the LineChart
        lineChart.setData(lineData);
        lineChart.invalidate(); // refresh the chart
    }



    private void populateLineChart() {
        // Make the network request to get week data
        MySQLHelper.getWeekData(getActivity(), swipeRefreshLayout, new WeekDataCallback() {
            @Override
            public void onWeekDataReceived(List<String> datetimeList) {
                // update the line chart with the received data
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
                updateLineChart(datetimeList, sdf);
                Log.d("REPORT SCREEN WEEK DATA", datetimeList.toString());
            }
        });
    }

//    private void showLoadingIndicator() {
//        binding.progressBar.setVisibility(View.VISIBLE);
//        binding.userTextView.setVisibility(View.INVISIBLE);
//    }
//
//    private void hideLoadingIndicator() {
//        binding.progressBar.setVisibility(View.GONE);
//        binding.userTextView.setVisibility(View.VISIBLE);
//    }

}


