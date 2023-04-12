package com.cjcj55.literallynot;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.Manifest;
import androidx.core.app.ActivityCompat;

import androidx.appcompat.app.AppCompatActivity;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.cjcj55.literallynot.databinding.ActivityMainBinding;

import android.os.Environment;
import android.provider.Settings;
import android.view.Menu;
import android.view.MenuItem;

import org.vosk.android.StorageService;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration appBarConfiguration;
    private ActivityMainBinding binding;
    private static final int PERMISSION_REQUEST_CODE = 1369420;
    private static boolean CAN_MODIFY_STORAGE = false;
    LoginScreen launch = new LoginScreen();

    public int loginstuff;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        SharedPreferences sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
        boolean isLoggedIn = SharedPreferencesManager.getLoginState(MainActivity.this, false); // default value is false

// Check if user is logged in
        if (isLoggedIn == false) {
            // Redirect to MainScreen.java

           startActivity(new Intent(MainActivity.this, LoginScreen.class));
        }
        System.out.println(loginstuff);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbar);

        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        appBarConfiguration = new AppBarConfiguration.Builder(navController.getGraph()).build();
        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);

        if (!checkPermissions()) {
            requestPermissions();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            NotificationChannel channel = new NotificationChannel("1369420", "Literally Not", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
//                && Environment.isExternalStorageManager()) {
//            // The app has permission to access external storage
//            CAN_MODIFY_STORAGE = true;
//        } else {
//            // Request permission from the user
//            Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
//            Uri uri = Uri.fromParts("package", getPackageName(), null);
//            intent.setData(uri);
//            startActivity(intent);
//        }

    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        ActivityCompat.requestPermissions(this,
                new String[]{android.Manifest.permission.RECORD_AUDIO},
                1);
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, appBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void createNotification() {
        // Create a pending intent that will open the app when the notification is clicked
        Intent intent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);

        Uri soundUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE+ "://" + getPackageName() + "/" + R.raw.notification_sound);

        // Create a notification builder and set the custom sound
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "1369420")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("LITERALLY?!")
                .setContentText("You said it again >:(")
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setSound(soundUri)
                .setAutoCancel(true);

        // Check if all the necessary permissions are granted
        if (checkPermissions()) {
            // Show the notification
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(1369420, builder.build());
        } else {
            // Request the necessary permissions
            requestPermissions();
        }
    }

    private boolean checkPermissions() {
        // Check if all the necessary permissions are granted
        boolean isPermissionGranted = true;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED) {
            isPermissionGranted = false;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED) {
            isPermissionGranted = false;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            isPermissionGranted = false;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
            isPermissionGranted = false;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WAKE_LOCK) != PackageManager.PERMISSION_GRANTED) {
            isPermissionGranted = false;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            isPermissionGranted = false;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NOTIFICATION_POLICY) != PackageManager.PERMISSION_GRANTED) {
            isPermissionGranted = false;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            isPermissionGranted = false;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            isPermissionGranted = false;
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.MANAGE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            isPermissionGranted = false;
        }
        return isPermissionGranted;
    }

    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> permissionsToRequest = new ArrayList<>();
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.FOREGROUND_SERVICE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.FOREGROUND_SERVICE);
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.POST_NOTIFICATIONS);
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NOTIFICATION_POLICY) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.ACCESS_NOTIFICATION_POLICY);
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.RECORD_AUDIO);
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.INTERNET) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.INTERNET);
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECEIVE_BOOT_COMPLETED) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.RECEIVE_BOOT_COMPLETED);
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.VIBRATE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.VIBRATE);
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WAKE_LOCK) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.WAKE_LOCK);
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.MANAGE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.MANAGE_EXTERNAL_STORAGE);
            }
            if (!permissionsToRequest.isEmpty()) {
                ActivityCompat.requestPermissions(this, permissionsToRequest.toArray(new String[0]), PERMISSION_REQUEST_CODE);
            }
        }
    }


}