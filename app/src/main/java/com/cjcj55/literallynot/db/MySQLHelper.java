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
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.cjcj55.literallynot.R;

import org.apache.http.HttpEntity;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MySQLHelper {
    private static final String API_URL = "http://18.223.125.204/";
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

    public static void sendAudioFile(String userId, Uri audioFileUri, Context context, final AudioUploadCallback audioUploadCallback) {
        File audioFile = new File(audioFileUri.getPath());
        HttpEntity httpEntity = MultipartEntityBuilder.create()
                .addBinaryBody("audio_file", audioFile, ContentType.create("audio/mpeg"), audioFile.getName())
                .build();

        StringRequest stringRequest = new StringRequest(Request.Method.POST,
                API_URL + "audio-upload.php",
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        if (response.equals("1")) {
                            audioUploadCallback.onSuccess();
                        } else {
                            audioUploadCallback.onFailure();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                audioUploadCallback.onFailure();
            }
        }) {
            private final HttpEntity mHttpEntity = httpEntity;

            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                Map<String, String> params = new HashMap<>();
                params.put("user_id", userId);
                return params;
            }

            @Override
            public String getBodyContentType() {
                return mHttpEntity.getContentType().getValue();
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    mHttpEntity.writeTo(bos);
                    return bos.toByteArray();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        };

        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(stringRequest);
    }
}
