package com.cjcj55.literallynot.db;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
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
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import cz.msebera.android.httpclient.Header;

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

    public static void writeAudioFileForUser(Context context, File audioFile) {
        SharedPreferences sharedPreferences = context.getSharedPreferences("myAppPrefs", Context.MODE_PRIVATE);
        int userId = sharedPreferences.getInt("user_id", -1);

        // Create AsyncHttpClient
        AsyncHttpClient client = new AsyncHttpClient();

        // Create RequestParams
        RequestParams params = new RequestParams();
        try {
            params.put("audio_file", audioFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        params.put("user_id", String.valueOf(userId));

        // Send multipart request
        String url = API_URL + "write-audio-file.php";
        client.post(url, params, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                // Handle successful response
                String responseString = new String(responseBody);
                Log.d("UPLOAD", "Response: " + responseString);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                // Handle error response
                String errorString = error.getMessage();
                Log.e("UPLOAD", "Error: " + errorString);
            }
        });
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
