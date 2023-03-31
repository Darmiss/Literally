package com.cjcj55.literallynot.db;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.cjcj55.literallynot.R;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.Header;
import cz.msebera.android.httpclient.entity.mime.content.ByteArrayBody;
import cz.msebera.android.httpclient.entity.mime.content.FileBody;
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
    private static final String TAG = "MySQLHelper";

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

    public static void writeAudioFile(Context context, File file) {
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

        Call<ResponseBody> call = apiInterface.uploadAudio(filePart, userIdPart);
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

    public static void uploadMp3File(File file, Context context) {
        try {
            // Open a connection to the PHP script
            URL url = new URL(API_URL + "write-audio-file.php");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            SharedPreferences sharedPreferences = context.getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE);
            int userId = sharedPreferences.getInt("user_id", -1);

            // Set the request body to the mp3 file
            FileInputStream fileInputStream = new FileInputStream(file);
            OutputStream outputStream = connection.getOutputStream();
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }

            // Add the user_id parameter to the request
            String parameters = "user_id=" + userId;
            outputStream.write(parameters.getBytes());

            // Check the response code
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                // Handle a successful upload
                InputStream inputStream = connection.getInputStream();
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                String response = bufferedReader.readLine();
                // ...
            } else {
                // Handle an error
                // ...
            }

            // Clean up
            outputStream.close();
            fileInputStream.close();
            connection.disconnect();
        } catch (Exception e) {
            // Handle an exception
            // ...
        }
    }


    public static void readAudioFilesForUser(Context context, Response.Listener<AudioFile[]> responseListener, Response.ErrorListener errorListener) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE);
        int userId = sharedPreferences.getInt("user_id", -1);

        // Create request URL with user ID as a parameter
        String url = API_URL + "read-audio-files.php?user_id=" + userId;

        // Create JsonRequest using Volley
        JsonRequest<JSONArray> jsonRequest = new JsonRequest<JSONArray>(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            ArrayList<AudioFile> audioFilesList = new ArrayList<>();
                            for (int i = 0; i < response.length(); i++) {
                                JSONObject audioFileObject = response.getJSONObject(i);
                                int audioFileId = audioFileObject.getInt("id");
                                int audioFileUserId = audioFileObject.getInt("user_id");
                                String dateTime = audioFileObject.getString("time_said");
                                String audioFilePath = audioFileObject.getString("file_path");
                                AudioFile audioFile = new AudioFile(audioFileId, audioFileUserId, dateTime, audioFilePath);
                                audioFilesList.add(audioFile);
                            }
                            AudioFile[] audioFilesArray = new AudioFile[audioFilesList.size()];
                            audioFilesList.toArray(audioFilesArray);
                            responseListener.onResponse(audioFilesArray);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                },
                errorListener
        ) {
            @Override
            protected Response<JSONArray> parseNetworkResponse(NetworkResponse response) {
                String jsonString;
                try {
                    jsonString = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                } catch (UnsupportedEncodingException e) {
                    jsonString = new String(response.data);
                }
                try {
                    return Response.success(new JSONArray(jsonString), HttpHeaderParser.parseCacheHeaders(response));
                } catch (JSONException e) {
                    return Response.error(new ParseError(e));
                }
            }
        };

        // Add request to request queue
        Volley.newRequestQueue(context).add(jsonRequest);
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