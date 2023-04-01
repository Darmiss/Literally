package com.cjcj55.literallynot;

import static android.app.Service.START_STICKY;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.cjcj55.literallynot.R;
import com.naman14.androidlame.AndroidLame;
import com.naman14.androidlame.LameBuilder;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

public class ForegroundService extends Service {

    private static final String TAG = "ForegroundService";
    private static final int SAMPLE_RATE = 44100;
    private static final int BUFFER_SIZE = SAMPLE_RATE * 25; // 25 seconds of audio buffer
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final String KEYWORD = "literally";
    private static final int KEYWORD_CONTEXT_TIME = 5000; // 5 seconds before and after the keyword
    private static final String CHANNEL_ID = "test";

    private AudioRecord mAudioRecord;
    private CircularByteBuffer mAudioBuffer;
    private SpeechRecognizer mSpeechRecognizer;
    private HandlerThread mHandlerThread;
    private Handler mHandler;

    @Override
    public void onCreate() {
        super.onCreate();




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
        mHandler.post(new AudioReader());
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mHandlerThread.quitSafely();
        mAudioRecord.stop();
        mAudioRecord.release();
        mSpeechRecognizer.destroy();
    }

    private class AudioReader implements Runnable {
        private final Handler mHandler = new Handler();

        @Override
        public void run() {
            System.out.println("AudioBuffer run()");
            byte[] buffer = new byte[BUFFER_SIZE];
            while (true) {
                int bytesRead = mAudioRecord.read(buffer, 0, buffer.length);
                int bytesWritten = mAudioBuffer.write(buffer, 0, bytesRead);
                System.out.println("Bytes written to buffer: " + bytesWritten);
                mAudioBuffer.write(buffer, 0, bytesRead);
                mHandler.post(new RecognizeSpeechTask(mAudioBuffer.readAll()));
            }
        }
    }

    private class RecognizeSpeechTask implements Runnable {
        private final byte[] mAudioData;
        private String mText;


        RecognizeSpeechTask(byte[] audioData) {
            System.out.println("RECO SPEECH TASK " + audioData.length);
            mAudioData = audioData;
        }

        @Override
        public void run() {
            System.out.println("HERETEST1" + mAudioData.length);
            String text = recognizeSpeech(mAudioData);
            System.out.println("Run called, checking for keyword");
            System.out.println("HERETEST2" + mAudioData.length);
            if (text.contains(KEYWORD)) {
                long keywordTimestamp = getTimestampForKeyword(text, KEYWORD);
                byte[] keywordAudioData = getAudioDataForTimestamp(keywordTimestamp);
                saveAudioToFile(keywordAudioData, getOutputFilePath());
            }
        }


        private String recognizeSpeech(byte[] audioData) {
            mText = "";
            if (audioData.length > 0) {
                mSpeechRecognizer.setRecognitionListener(new RecognitionListener() {
                    @Override
                    public void onReadyForSpeech(Bundle bundle) {
                        System.out.println("OnReady");
                    }

                    @Override
                    public void onBeginningOfSpeech() {
                        System.out.println("OnBegining");

                    }

                    @Override
                    public void onRmsChanged(float v) {
                    }

                    @Override
                    public void onBufferReceived(byte[] bytes) {
                    }

                    @Override
                    public void onEndOfSpeech() {
                        System.out.println("onEnd");

                    }

                    @Override
                    public void onError(int error) {
                        System.out.println("onError " + error);
                    }

                    @Override
                    public void onResults(Bundle results) {
                        ArrayList<String> voiceResults = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
                        if (voiceResults != null && voiceResults.size() > 0) {
                            mText = voiceResults.get(0);
                            System.out.println("onResult: " + mText);
                        }
                    }

                    @Override
                    public void onPartialResults(Bundle partialResults) {
                        System.out.println("OnPartial");
                        for (String key : partialResults.keySet()) {
                            Object value = partialResults.get(key);
                            System.out.println("HERE DUMBASS: " + String.format("%s = %s (%s)", key, value.toString(), value.getClass().getName()));
                        }
                    }

                    @Override
                    public void onEvent(int eventType, Bundle params) {
                        System.out.println("OnEvent");

                    }
                });
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 2000);
                intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 3000);
                intent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 3000);
                mSpeechRecognizer.startListening(intent);
            }
            return mText;
        }
    }

    private long getTimestampForKeyword(String text, String keyword) {
        int startIndex = text.indexOf(keyword);
        long keywordStartTime = getAudioTimestampForTextIndex(text, startIndex);
        long keywordEndTime = keywordStartTime + KEYWORD_CONTEXT_TIME;
        return keywordEndTime;
    }

    private long getAudioTimestampForTextIndex(String text, int index) {
        int numSamples = index * SAMPLE_RATE / 1000;
        return (long) numSamples * 1000 / SAMPLE_RATE;
    }

    private byte[] getAudioDataForTimestamp(long timestamp) {
        byte[] audioData = mAudioBuffer.readAll();
        int startIndex = getIndexForAudioTimestamp(audioData.length, timestamp - KEYWORD_CONTEXT_TIME);
        int endIndex = getIndexForAudioTimestamp(audioData.length, timestamp + KEYWORD_CONTEXT_TIME);
        return Arrays.copyOfRange(audioData, startIndex, endIndex);
    }

    private int getIndexForAudioTimestamp(int audioLength, long timestamp) {
        System.out.println("getIndexForAudioTimestamp");
        int numSamples = (int) (timestamp * SAMPLE_RATE / 1000);
        int numBytes = numSamples * 2;
        int index = audioLength - numBytes;
        if (index < 0) {
            index = 0;
        }
        return index;
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
        return new File(dir, "recorded_audio_" + System.currentTimeMillis() + ".mp3").getAbsolutePath();
    }

    private Notification buildNotification() {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "channel_id")
                .setContentTitle("Foreground Service")
                .setContentText("Recording audio...")
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setOngoing(true);
        Intent stopIntent = new Intent(this, ForegroundService.class);
        stopIntent.setAction("stop");
        PendingIntent pendingIntent = PendingIntent.getService(this, 0, stopIntent, 0);
        builder.addAction(R.drawable.ic_launcher_background, "Stop", pendingIntent);
        return builder.build();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}


