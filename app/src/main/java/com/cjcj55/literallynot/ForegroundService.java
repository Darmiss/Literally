package com.cjcj55.literallynot;

import static android.app.PendingIntent.getActivity;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.speech.SpeechRecognizer;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.cjcj55.literallynot.db.LocCallback;
import com.cjcj55.literallynot.db.MySQLHelper;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.naman14.androidlame.AndroidLame;
import com.naman14.androidlame.LameBuilder;

import org.vosk.Model;
import org.vosk.Recognizer;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;
import java.util.Locale;

public class ForegroundService extends Service {


    private Model mModel;
    private static final String TAG = "ForegroundService";
    private static final int SAMPLE_RATE = 44100;

    private static final int BUFFER_SIZE = SAMPLE_RATE * 15; //25 seconds of audio buffer
    //^^ CHANGE THIS TO CHANGE SIZE OF BUFFER(* 25 = 12 SECOND TOTAL KEYWORD, *10 = 4 SECOND AUDIO FILES ETC)
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final String KEYWORD = "literally";
    private static final String CHANNEL_ID = "test";

    private AudioRecord mAudioRecord;
    private CircularByteBuffer mAudioBuffer;
    private SpeechRecognizer mSpeechRecognizer;
    private HandlerThread mHandlerThread;
    private Handler mHandler;
    private String textSaid;


    @Override
    public void onCreate() {
        super.onCreate();


        // Initialize the Vosk model
        Model model = ModelManager.getInstance().getModel();
        if (model != null) {
            // use the model
            mModel = model;
        } else {
            // handle error
            System.out.println("No model loaded..?");
        }


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_FORMAT, BUFFER_SIZE);
        mAudioBuffer = new CircularByteBuffer(BUFFER_SIZE);
        mSpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mHandlerThread = new HandlerThread(TAG);
        mHandlerThread.start();
        mHandler = new Handler(mHandlerThread.getLooper());

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_LOW);

            getSystemService(NotificationManager.class).createNotificationChannel(channel);
            Notification.Builder notification = new Notification.Builder(this, CHANNEL_ID)
                    .setContentText("Hello")
                    .setContentTitle("Blah");

            // Start the foreground service with the notification
            startForeground(1001, notification.build());
        }
        mAudioRecord.startRecording();
        // Run the AudioReader on a background thread
        new AudioReader().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mHandlerThread != null) {
            mHandlerThread.quitSafely();
        }
        if (mAudioRecord != null) {
            mAudioRecord.stop();
            mAudioRecord.release();
        }
        if (mSpeechRecognizer != null) {
            mSpeechRecognizer.destroy();
        }
    }


    private class AudioReader extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {
            System.out.println("AudioBuffer run()");
            byte[] buffer = new byte[BUFFER_SIZE];
            while (mAudioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) { //check if mAudioRecord is still recording before attempting to read from it,
                System.out.println("READING...WRITING AUDIO TO BUFFER.");
                int bytesRead = mAudioRecord.read(buffer, 0, buffer.length);
                System.out.println("BYTEREAD" + bytesRead);
                mAudioBuffer.write(buffer, 0, bytesRead);
                mHandler.post(new RecognizeSpeechTask(mAudioBuffer.readAll()));
            }
            return null;
        }
    }

    private class RecognizeSpeechTask implements Runnable {
        private final byte[] mAudioData;

        RecognizeSpeechTask(byte[] audioData) {
            mAudioData = audioData;
            System.out.println("audiodata given length" + audioData.length);
        }

        @Override
        public void run() {
            String text = null;

            text = recognizeSpeech(mAudioData);
            if (text.contains(KEYWORD)) {

                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                    //^ TO MAKE IT SHUT THE HELL UP
                }
                textSaid = text;
                textSaid = textSaid.replace("{", "")
                        .replace("}", "")
                        .replace("\"", "")
                        .replace("text", "")
                        .replace(":", "")
                        .trim();

                // To start the location task
                LocationTask locationTask = new LocationTask();
                locationTask.execute(); //async->This will get long and lat.
                //Inside this is a geocode async, which is where address is gotten

                // Get instance of Vibrator class
                Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);

                // Vibrate for 500 milliseconds
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE));
                }

                // Play notification sound
                Uri notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), notificationSound);
                ringtone.play();
                //UNCOMMENT THIS TO SAVE YOUR LITERALLY SNIPPED TO THE DB
                saveAudioToFile(mAudioData, getOutputFilePath());
                File file = new File(getCacheDir(), "recordedAudio.mp3");
            }
        }

        public String recognizeSpeech(byte[] audioData) {
            System.out.println("AUDIODATA RECIEVED FOR SPEECH");
            StringBuilder result = new StringBuilder();
            System.out.println(audioData.length);
            try (final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(audioData)) {
                final Recognizer rec = new Recognizer(mModel, SAMPLE_RATE);
                final BufferedInputStream bis = new BufferedInputStream(byteArrayInputStream);
                final byte[] buff = new byte[4096];

                int len;
                while ((len = bis.read(buff)) != -1) {
                    if (rec.acceptWaveForm(buff, len)) {
                        final var res = rec.getFinalResult();
                        //??>>getFinalResult() OR getResult() OR getPartialResults()
                        if (res != null) {
                            result.append(res.toLowerCase()).append(" ");
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println("VOICE RESULT: " + result.toString().trim());
            return result.toString().trim();
        }
    }

    private void saveAudioToFile(byte[] audioData, String filePath) {
        System.out.println("audiodata" + audioData.length);
        int sampleRate = 44100; // audio sample rate
        int bitRate = 128; // output MP3 bit rate
        try {
            FileOutputStream fos = new FileOutputStream(filePath);
            AndroidLame androidLame = new LameBuilder()
                    .setInSampleRate(sampleRate)
                    .setOutChannels(1)
                    .setOutBitrate(bitRate)
                    .setOutSampleRate(sampleRate)
                    .build();
            // convert the audio data from byte[] to short[]
            short[] audioShorts = new short[audioData.length / 2];
            ByteBuffer.wrap(audioData).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(audioShorts);
            byte[] mp3Buffer = new byte[audioData.length];
            // encode the audio data to MP3 format using TAndroidLame
            int encodedBytes = androidLame.encode(audioShorts, audioShorts, audioShorts.length, mp3Buffer);
            // finalize the encoding process
            androidLame.close();
            // write the encoded MP3 data to file
            System.out.println("Finish saving audio");
            fos.write(mp3Buffer, 0, encodedBytes);
            fos.close();
        } catch (Exception e) {
            Log.e(TAG, "Error saving audio to file: " + e.getMessage());
        }
    }

    private String getOutputFilePath() {
        System.out.println("getting outputfile path");
        File dir = getCacheDir();
        return new File(dir, "recordedAudio.mp3").getAbsolutePath();
    }


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private class LocationTask extends AsyncTask<Void, Void, Void> {

        private FusedLocationProviderClient fusedLocationProviderClient;
        private LocationCallback locationCallback;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            // Set up fused location provider client
            fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getApplicationContext());

            // Set up location request
            LocationRequest locationRequest = LocationRequest.create();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

            // Set up location callback
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult != null && locationResult.getLastLocation() != null) {
                        Location location = locationResult.getLastLocation();
                        double latitude = location.getLatitude();
                        double longitude = location.getLongitude();
                        Log.d("TAG", "Latitude: " + latitude + ", Longitude: " + longitude);

                        getAddressFromLocation(latitude, longitude);
                    }
                }
            };

            // Request location updates
            try {
                fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, null);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            // Perform background tasks (if any)
            return null;
        }

        private void getAddressFromLocation(double latitude, double longitude) {
            Geocoder geocoder = new Geocoder(ForegroundService.this, Locale.getDefault());
            System.out.println("HITGEOCODE");
            List<Address> addresses = null;
            try {
                addresses = geocoder.getFromLocation(latitude, longitude, 1);
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (addresses != null && addresses.size() > 0) {
                Address address = addresses.get(0);
                // extract the address components and display them
                String addressLine = address.getAddressLine(0);
                String city = address.getLocality();
                String state = address.getAdminArea();
                String country = address.getCountryName();
                String postalCode = address.getPostalCode();
                String knownName = address.getFeatureName();
                String addr = addressLine + ".  " + city + ", " + state + " " + postalCode;
                // do something with the address information here
                Log.d("TAG", address.toString());
                Toast.makeText(ForegroundService.this, address.toString(), Toast.LENGTH_LONG).show();

                //TODO: SEND TO DATABASE
                File file = new File(getCacheDir(), "recordedAudio.mp3");
                MySQLHelper.writeAudioFile(getApplicationContext(), file, textSaid, addr);
            }
        }
    }
}

