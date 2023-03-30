package com.cjcj55.literallynot;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import java.util.concurrent.TimeUnit;

import androidx.core.app.NotificationCompat;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Locale;

public class ForegroundService extends Service implements Runnable{

    private static final int NOTIFICATION_ID = 1;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Create a notification for the foreground service
        final String CHANNEL_ID = "Foreground Service";
        NotificationChannel channel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_LOW);

            getSystemService(NotificationManager.class).createNotificationChannel(channel);
            Notification.Builder notification = new Notification.Builder(this, CHANNEL_ID)
                    .setContentText("Hello")
                    .setContentTitle("Blah");


            // Start the foreground service with the notification
            startForeground(1001, notification.build());
        }
        // Do your work here, such as recording audio
        SpeechRecognizer speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        final Intent spIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        spIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        spIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        spIntent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, this.getPackageName());

        //Initializes the MediaRecorder for audio recording
        PackageManager m = getPackageManager();
        String s = getPackageName();
        PackageInfo p = null;
        try {
            p = m.getPackageInfo(s, 0);
        } catch (PackageManager.NameNotFoundException e) {
            throw new RuntimeException(e);
        }
        s = p.applicationInfo.dataDir;
        System.out.println(s);
        System.out.println(m);
        System.out.println(p);

        AudioRecorder recorder = new AudioRecorder();
        try {
            recorder.startRecording();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        recorder.stopRecording();
        //speechRecognizer.startListening(spIntent);
        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle bundle) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float v) {

            }

            @Override
            public void onBufferReceived(byte[] bytes) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int i) {

            }

            @Override
            public void onResults(Bundle results) {
                //Inside of here make sure to to add the circular buffer functionality
                ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                if (matches != null && matches.size() > 0) {
                    String spokenText = matches.get(0);
                    System.out.println(spokenText);
                }
            }

            @Override
            public void onPartialResults(Bundle bundle) {

            }

            @Override
            public void onEvent(int i, Bundle bundle) {

            }
        });
        // Stop the foreground service when your work is done
        //stopForeground(true);
       // stopSelf();

        return START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        // This service does not support binding, so return null
        return null;
    }

    @Override
    public void run() {

    }
    //In order to get context for file location was required to
    public class AudioRecorder{

        private MediaRecorder recorder;
        private ByteArrayOutputStream output;
        private Handler handler;
        private Runnable callback;
        //Constructor  for the Audio Recorder.  Thank you sleep deprivation
        public AudioRecorder() {
            //Initialize the AudioRecorder variables
            recorder = new MediaRecorder();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setAudioSamplingRate(44100);
            recorder.setAudioChannels(AudioFormat.ENCODING_DEFAULT);  //Doesn't crash program when set to DEFAUKT
            recorder.setAudioEncodingBitRate(AudioFormat.ENCODING_MP3);
            //Create the output stream for the thing
            output = new ByteArrayOutputStream();
            handler = new Handler();

        }

        public void startRecording() throws IOException{
            //outputFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/AudioRecording_" + System.currentTimeMillis() + ".mp3");
            //If we can get setOutputFile Working this will be completed
            String audioFileName = "myaudiofile.mp3";
            File outputFile = new File(getCacheDir(), audioFileName);
            recorder.setOutputFile(outputFile.getAbsolutePath());
            recorder.prepare();
            recorder.start();
            callback = new Runnable() {
                @Override
                public void run() {
                    byte[] recordBytes = output.toByteArray();
                    String recordedText = new String(recordBytes, StandardCharsets.UTF_8);
                    System.out.println(recordedText);
                    handler.postDelayed(this, 50000);
                }
            };
            handler.postDelayed(callback, 50000);

        }
        public void stopRecording(){
            if(null != recorder){
                    recorder.stop();
                    recorder.release();
                    recorder = null;
                    handler.removeCallbacks(callback);

            }
        }
        public void recordLoop(){

        }
    }
}
