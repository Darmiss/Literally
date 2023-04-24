package com.cjcj55.literallynot;

import android.graphics.Color;
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
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

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
    private TextView statTextView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = ReportscreenuiBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        statTextView = getView().findViewById(R.id.statTextView);
        swipeRefreshLayout = getView().findViewById(R.id.swipeRefreshLayout);
        lineChart = getView().findViewById(R.id.lineChart);
        lineChart.getDescription().setEnabled(false);
        lineChart.getLegend().setEnabled(false);
//        lineChart.setDragEnabled(true);
        lineChart.setPinchZoom(false);
        lineChart.setDoubleTapToZoomEnabled(false);

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
    }

    private void updateLineChart(List<String> datetimeList, SimpleDateFormat sdf) {
        // create a list of Entry objects representing the data points
        List<Entry> entries = new ArrayList<>();

        // create an array of day names
        String[] daysOfWeek = new String[7];
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEE");
        for (int i = 6; i >= 0; i--) {
            // create a new Calendar instance
            Calendar calendar = Calendar.getInstance();

            // set it to the date of the corresponding day (subtracting i days from today)
            calendar.add(Calendar.DAY_OF_MONTH, -i);

            // format the date as a string and add it to the array
            daysOfWeek[6 - i] = dayFormat.format(calendar.getTime());

            // calculate the sum of dates for that day
            int sum = 0;
            for (String datetime : datetimeList) {
                try {
                    // parse the date string into a Date object
                    Date date = sdf.parse(datetime);

                    // create a Calendar object and set it to the parsed date
                    Calendar dateCalendar = Calendar.getInstance();
                    dateCalendar.setTime(date);

                    // compare the day, month, and year of the two calendars
                    if (dateCalendar.get(Calendar.DAY_OF_MONTH) == calendar.get(Calendar.DAY_OF_MONTH)
                            && dateCalendar.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)
                            && dateCalendar.get(Calendar.YEAR) == calendar.get(Calendar.YEAR)) {
                        // the date matches the current day, add it to the sum
                        sum++;
                    }
                } catch (ParseException e) {
                    // handle parsing error
                    e.printStackTrace();
                }
            }

            // add a data point for the current day
            entries.add(new Entry(6 - i, sum));
        }

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
        lineChart.getAxisLeft().setGranularity(1f); // set the y-axis granularity to 1
        lineChart.getAxisLeft().setLabelCount(6); // set the number of y-axis labels to 6
        lineChart.getAxisLeft().setAxisMinimum(0f); // set the y-axis minimum to 0
        lineChart.getAxisRight().setEnabled(false); // disable the right y-axis
        lineChart.getXAxis().setGranularity(1f);
        lineChart.getXAxis().setLabelCount(7);
        lineChart.setDoubleTapToZoomEnabled(false);
        lineChart.setPinchZoom(false);

        // set the day names as the x-axis labels
        lineChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(daysOfWeek));

        lineChart.invalidate(); // refresh the chart

        MySQLHelper.getWeekData(getActivity(), new WeekDataCallback() {
            @Override
            public void onWeekDataReceived(List<String> datetimeList) {
                statTextView.setText("You have said 'Literally' a total of " + datetimeList.size() + " times!");
            }
        });
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


