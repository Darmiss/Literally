package com.cjcj55.literallynot.db;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.cjcj55.literallynot.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MySQLHelper {
    private static final String API_URL = "http://18.223.125.204/";

    public static int fbnum;

    public static void registerAccount(String username, String password, String email, String firstName, String lastName, Context context) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                API_URL + "register.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            String success = jsonObject.getString("success");
                            if (success.equals("1")) {
                                Toast.makeText(context, "User registered successfully", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(context, "User could not register", Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, "error:" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }) {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("username", username);
                params.put("pass", password);
                params.put("email", email);
                params.put("firstName", firstName);
                params.put("lastName", lastName);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(stringRequest);

    }

    public static void login(String userNameOrEmail, String password, Context context, Activity activity, LoginCallback loginCallback) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                API_URL + "login.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);

                            String success = jsonObject.getString("success");
                            if (success.equals("1")) {
                                JSONObject sessionData = jsonObject.getJSONObject("sessionData");
                                int uid = sessionData.getInt("user_id");
                                String un = sessionData.getString("username");
                                String firstName = sessionData.getString("firstName");
                                String lastName = sessionData.getString("lastName");
                                SharedPreferences sharedPreferences = activity.getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE);
                                SharedPreferences.Editor editor = sharedPreferences.edit();
                                editor.putInt("user_id", uid);
                                editor.putString("username", un);
                                editor.putString("firstName", firstName);
                                editor.putString("lastName", lastName);
                                editor.putBoolean("isLoggedIn", true);
                                editor.apply();
                                loginCallback.onSuccess(uid, un, firstName, lastName);
                            } else {
                                Toast.makeText(context, "Invalid username/email or password", Toast.LENGTH_SHORT).show();
                                loginCallback.onFailure();
                            }
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                loginCallback.onFailure();
                Toast.makeText(context, "error:" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        })
        {
            @Nullable
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("username_or_email", userNameOrEmail);
                params.put("password", password);
                return params;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(stringRequest);
    }

    public static List<LBEntry> getLeaderboard(Context context, LeaderboardCallback leaderboardCallback) {
        List<LBEntry> leaderboard = new ArrayList<>();

        String url = API_URL + "leaderboard.php";

        // Request a string response from the provided URL.
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        // Handle the JSON response
                        try {
                            // Iterate over the JSON array and extract the data
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject row = response.getJSONObject(i);
                                int userId = row.getInt("user_id");
                                String username = row.getString("username");
                                String firstName = row.getString("firstName");
                                String lastName = row.getString("lastName");
                                int numFiles = row.getInt("num_files");

                                LBEntry entry = new LBEntry(userId, username, numFiles, firstName, lastName);
                                leaderboard.add(entry);
                            }
                            leaderboardCallback.onSuccess(leaderboard);
                        } catch (JSONException e) {
                            // Handle JSON parsing error
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                // Handle the error
                leaderboardCallback.onFailure();
                error.printStackTrace();
            }
        });

        // Add the request to the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(jsonArrayRequest);

        return leaderboard;
    }

    public static void logout(Context context, Fragment fragment) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                API_URL + "logout.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        SharedPreferences sharedPreferences = context.getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE);
                        boolean isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false);
                        if (isLoggedIn) {
                            SharedPreferences.Editor editor = sharedPreferences.edit();
                            editor.clear();
                            editor.apply();

                            NavHostFragment.findNavController(fragment)
                                    .navigate(R.id.action_logout_to_LoginScreen);
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Toast.makeText(context, "error:" + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(stringRequest);
    }

    public static void writeAudioFile(Context context, File file, String text, String location) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE);
        String userId = String.valueOf(sharedPreferences.getInt("user_id", -1));


        String baseUrl = API_URL + "write-audio-file.php/";

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiInterface apiInterface = retrofit.create(ApiInterface.class);

        RequestBody requestBody = RequestBody.create(MediaType.parse("audio/*"), file);

        MultipartBody.Part filePart = MultipartBody.Part.createFormData("audio", file.getName(), requestBody);

        RequestBody userIdPart = RequestBody.create(MediaType.parse("text/plain"), userId);
        RequestBody textPart = RequestBody.create(MediaType.parse("text/plain"), text);
        RequestBody locationPart = RequestBody.create(MediaType.parse("text/plain"), location);

        Call<ResponseBody> call = apiInterface.uploadAudio(filePart, userIdPart, textPart, locationPart);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                // Handle the response here
                System.out.println(file.getName() + " has been successfully uploaded!");
                System.out.println("Response message: " + response.message());
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // Handle the failure here
                System.out.println(file.getName() + " has failed to upload");
                System.out.println("Error message: " + t.getMessage());
            }
        });
    }

    public static void updateAudioFile(Context context, String filePathName, String location) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE);
        String userId = String.valueOf(sharedPreferences.getInt("user_id", -1));

        String baseUrl = API_URL + "update-audio-file.php/";

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiInterface apiInterface = retrofit.create(ApiInterface.class);

        RequestBody userIdPart = RequestBody.create(MediaType.parse("text/plain"), userId);
        RequestBody filePathNamePart = RequestBody.create(MediaType.parse("text/plain"), filePathName);
        RequestBody locationPart = RequestBody.create(MediaType.parse("text/plain"), location);

        Call<ResponseBody> call = apiInterface.updateAudio(userIdPart, filePathNamePart, locationPart);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, retrofit2.Response<ResponseBody> response) {
                // Handle the response here
                System.out.println(filePathName + " has been successfully updated with location!");
                System.out.println("Response message: " + response.message());

            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                // Handle the failure here
                System.out.println(filePathName + " has failed to update location");
                System.out.println("Error message: " + t.getMessage());
            }
        });

    }

    public static void getWeekData(Activity activity, WeekDataCallback callback) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE);
        int userId = sharedPreferences.getInt("user_id", -1);

        // Define the URL for the PHP file
        String url = API_URL + "week-data.php?user_id=" + userId;

        // Create a request queue for the network operations
        RequestQueue queue = Volley.newRequestQueue(activity);

        // Create JSON request to retrieve time_said values for user
        JsonArrayRequest jsonRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        // Handle the JSON response
                        try {
                            Log.d("JSON Response", response.toString());

                            List<String> datetimeList = new ArrayList<>();

                            // Iterate over the JSON array and extract the data
                            for (int i = 0; i < response.length(); i++) {
                                String dateTime = response.getString(i);
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                                Date date = sdf.parse(dateTime);
                                String dateOnly = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date);
                                datetimeList.add(dateOnly);
                            }

                            callback.onWeekDataReceived(datetimeList);

                        } catch (JSONException e) {
                            // Handle JSON parsing error
                            e.printStackTrace();
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle the error
                        error.printStackTrace();
                    }
                })
        {
            @Override
            protected Response<JSONArray> parseNetworkResponse(NetworkResponse response) {
                try {
                    String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                    return Response.success(new JSONArray(jsonString), HttpHeaderParser.parseCacheHeaders(response));
                } catch (UnsupportedEncodingException | JSONException e) {
                    return Response.error(new ParseError(e));
                }
            }
        };


        // Add the request to the queue
        queue.add(jsonRequest);
    }

    public static void getWeekData(Activity activity, SwipeRefreshLayout swipeRefreshLayout, WeekDataCallback callback) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE);
        int userId = sharedPreferences.getInt("user_id", -1);

        // Define the URL for the PHP file
        String url = API_URL + "week-data.php?user_id=" + userId;

        // Create a request queue for the network operations
        RequestQueue queue = Volley.newRequestQueue(activity);

        // Create JSON request to retrieve time_said values for user
        JsonArrayRequest jsonRequest = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        // Handle the JSON response
                        try {
                            Log.d("JSON Response", response.toString());

                            List<String> datetimeList = new ArrayList<>();

                            // Iterate over the JSON array and extract the data
                            for (int i = 0; i < response.length(); i++) {
                                String dateTime = response.getString(i);
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
                                Date date = sdf.parse(dateTime);
                                String dateOnly = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date);
                                datetimeList.add(dateOnly);
                            }

                            callback.onWeekDataReceived(datetimeList);

                        } catch (JSONException e) {
                            // Handle JSON parsing error
                            e.printStackTrace();
                        } catch (ParseException e) {
                            throw new RuntimeException(e);
                        }

                        swipeRefreshLayout.setRefreshing(false);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // Handle the error
                        error.printStackTrace();
                        swipeRefreshLayout.setRefreshing(false);
                    }
                })
        {
            @Override
            protected Response<JSONArray> parseNetworkResponse(NetworkResponse response) {
                try {
                    String jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                    return Response.success(new JSONArray(jsonString), HttpHeaderParser.parseCacheHeaders(response));
                } catch (UnsupportedEncodingException | JSONException e) {
                    return Response.error(new ParseError(e));
                }
            }
        };


        // Add the request to the queue
        queue.add(jsonRequest);
    }

    public static void downloadAndConvertMP3s(Activity activity, RecyclerView recyclerView, SwipeRefreshLayout swipeRefreshLayout) {
        SharedPreferences sharedPreferences = activity.getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE);
        String userId = String.valueOf(sharedPreferences.getInt("user_id", -1));
        Log.d("AudioClips", "userId is " + userId);

        // Define the URL for the PHP file
        String url = API_URL + "read-audio-files.php?user_id=" + userId;

        // Create a request queue for the network operations
        RequestQueue queue = Volley.newRequestQueue(activity);

        // Create a JSON request to retrieve the audio clip data
        @SuppressLint("StaticFieldLeak") JsonObjectRequest jsonRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    Log.d("AudioClips", "JSON response received");

                    // AsyncTask for file conversion and RecyclerView update
                    new AsyncTask<Void, Void, List<AudioClip>>() {
                        @Override
                        protected List<AudioClip> doInBackground(Void... voids) {
                            List<AudioClip> audioClips = new ArrayList<>();
                            try {
                                // Get the JSON array from the response
                                JSONArray jsonArray = response.getJSONArray("files");

                                // Loop through each file in the array
                                for (int i = 0; i < jsonArray.length(); i++) {
                                    // Get the file object
                                    JSONObject fileObject = jsonArray.getJSONObject(i);

                                    // Get the file name, size, data, and time said from the object
                                    String fileName = fileObject.getString("name");
                                    long fileSize = fileObject.getLong("size");
                                    String fileData = fileObject.getString("data");
                                    String timeSaid = fileObject.getString("time_said");
                                    String text = fileObject.getString("textsaid");
                                    String location = fileObject.getString("location");

                                    // Decode the base64-encoded data into a byte array
                                    byte[] fileBytes = Base64.decode(fileData, Base64.DEFAULT);

                                    File audioDirectory = new File(activity.getCacheDir(), "audio");
                                    if (!audioDirectory.exists()) {
                                        audioDirectory.mkdir();
                                    }
                                    File file = new File(audioDirectory, fileName);
                                    if (file.exists()) {
                                        file.delete();
                                    }
                                    FileOutputStream outputStream = new FileOutputStream(file);
                                    outputStream.write(fileBytes);
                                    outputStream.close();

                                    // Create a new AudioClip object
                                    AudioClip audioClip = new AudioClip(file.getAbsolutePath(), timeSaid, text, location);

                                    // Add the AudioClip object to the list
                                    audioClips.add(audioClip);
                                    fbnum++;
                                }
                                Log.d("AudioClips", "Number of audio clips: " + audioClips.size());
                            } catch (JSONException | IOException e) {
                                e.printStackTrace();
                            }
                            return audioClips;
                        }

                        @Override
                        protected void onPostExecute(List<AudioClip> audioClips) {
                            // Create a new adapter for the RecyclerView
                            AudioListAdapter adapter = new AudioListAdapter(activity, audioClips);

                            // Set the adapter on the RecyclerView
                            recyclerView.setAdapter(adapter);

                            // Stop the SwipeRefreshLayout animation
                            swipeRefreshLayout.setRefreshing(false);
                        }
                    }.execute();
                },
                error -> {
                    Log.e("AudioClips", "Error in JsonObjectRequest: " + error.getMessage());
                    // Handle the error
                    swipeRefreshLayout.setRefreshing(false);
                }
        );

        // Add the request to the queue
        queue.add(jsonRequest);
    }

    public static void getAllUsers(Context context, Response.Listener<String> responseListener) {
        StringRequest stringRequest = new StringRequest(Request.Method.GET,
                API_URL + "get-all-users.php",
                responseListener,
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(context, "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(stringRequest);
    }
}