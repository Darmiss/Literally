package com.cjcj55.literallynot;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;

import android.speech.SpeechRecognizer;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.naman14.androidlame.AndroidLame;
import com.naman14.androidlame.LameBuilder;

import org.vosk.LogLevel;
import org.vosk.Model;
import org.vosk.Recognizer;
import org.vosk.android.StorageService;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.ShortBuffer;
import java.util.Arrays;

public class ForegroundService extends Service {


    private Model mModel;
    private static final String TAG = "ForegroundService";
    private static final int SAMPLE_RATE = 44100;
    private static final int BUFFER_SIZE = SAMPLE_RATE * 10; //25 seconds of audio buffer
    //^^ CHANGE THIS TO CHANGE SIZE OF BUFFER(* 25 = 12 SECOND TOTAL KEYWORD, *10 = 4 SECOND AUDIO FILES ETC)
    private static final int AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private static final String KEYWORD = "literally";
    private static final int KEYWORD_CONTEXT_TIME = 2000; // 2 seconds before and after the keyword
    //^ NOT USED CURRENTLY ;(
    private static final String CHANNEL_ID = "test";

    private AudioRecord mAudioRecord;
    private CircularByteBuffer mAudioBuffer;
    private SpeechRecognizer mSpeechRecognizer;
    private HandlerThread mHandlerThread;
    private Handler mHandler;

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
            while (mAudioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) { //check if mAudioRecord is still recording before attempting to read from it,
                System.out.println("READING...WRITING AUDIO TO BUFFER.");
                int bytesRead = mAudioRecord.read(buffer, 0, buffer.length);
                mAudioBuffer.write(buffer, 0, bytesRead);
                mHandler.post(new RecognizeSpeechTask(mAudioBuffer.readAll()));
            }
        }
    }

    private class RecognizeSpeechTask implements Runnable {
        private final byte[] mAudioData;
        private byte[] mSavedAudioData; //The data that contains the keyword
        private String mText;

        RecognizeSpeechTask(byte[] audioData) {
            mAudioData = audioData;
        }

        @Override
        public void run() {
            String text = null;
            text = recognizeSpeech(mAudioData);
            if (text.contains(KEYWORD) || text.contains("hello")) {
                // Play notification sound
                Uri notificationSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                Ringtone ringtone = RingtoneManager.getRingtone(getApplicationContext(), notificationSound);
                ringtone.play();
               // mSavedAudioData=mAudioData;
               // long keywordTimestamp = getTimestampForKeyword(text, KEYWORD);
              //  System.out.println("TESTINGMAIN1" + keywordTimestamp);
              //  byte[] keywordAudioData = getAudioDataForTimestamp(keywordTimestamp);
              //  System.out.println("TESTINGMAIN2" + keywordAudioData.length);
                saveAudioToFile(mAudioData, getOutputFilePath());
            }
        }


        public String recognizeSpeech(byte[] audioData) {
            StringBuilder result = new StringBuilder();
            System.out.println(audioData.length);
            try (final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(audioData)) {
                final Recognizer rec = new Recognizer(mModel, SAMPLE_RATE);
                final BufferedInputStream bis = new BufferedInputStream(byteArrayInputStream);
                final byte[] buff = new byte[4096];
                int len;
                while ((len = bis.read(buff)) != -1) {
                    if (rec.acceptWaveForm(buff, len)) {
                        final var res = rec.getResult();
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



//TODO: EVERYTHING DOWN HERE IS FUCKED UP SOMEHOW, NOT RETRIEVING CORRECT CONTEXT AND GIVING 0 BYTE FILE
        //THE MP3 CONVERSION WORKS PERFECTLY FINE, USE getOutputFilePath(),
        //VOSK WORKS FINE ITS JUST REAL BAD SINCE ITS ONLY A 50MB MODEL
        //THE PIECES ARE HERE JUST NEED FIXED




    private long getTimestampForKeyword(String text, String keyword) {
        int startIndex = text.indexOf(keyword);
        System.out.println("Testing9" + startIndex);
        long keywordStartTime = getAudioTimestampForTextIndex(text, startIndex);
        long keywordEndTime = keywordStartTime + KEYWORD_CONTEXT_TIME;
        System.out.println("Testgin10" + keywordEndTime);
        return keywordEndTime;
    }

        private long getAudioTimestampForTextIndex(String text, int index) {
            System.out.println("Testing6" + index);
            System.out.println("Testing7" + text);
            double numSamples = index * 1.0 * SAMPLE_RATE / 1000.0;
            System.out.println("Testing8" + numSamples);
            return (long) (numSamples * 1000.0 / SAMPLE_RATE);
        }

        private byte[] getAudioDataForTimestamp(long timestamp) {
            saveAudioToFile(mAudioData,getOutputFilePath());
            byte[] audioData = mAudioData;
            int startIndex = 0;
            int endIndex = audioData.length;

            int contextSize = KEYWORD_CONTEXT_TIME * SAMPLE_RATE / 1000; // convert time to sample count
            int timestampIndex = (int) (timestamp * SAMPLE_RATE / 1000); // convert timestamp to sample index

            // Adjust start/end indices based on context size and requested timestamp
            startIndex = Math.max(0, timestampIndex - contextSize);
            endIndex = Math.min(audioData.length, timestampIndex + contextSize);

            // If requested timestamp is beyond the bounds of the audio data, adjust start/end indices accordingly
            if (startIndex == 0 && endIndex == audioData.length) {
                if (timestampIndex < 0) {
                    endIndex = Math.min(contextSize, audioData.length);
                } else if (timestampIndex >= audioData.length) {
                    startIndex = Math.max(0, audioData.length - contextSize);
                }
            }

            return Arrays.copyOfRange(audioData, startIndex, endIndex);
        }

   /* private int getIndexForAudioTimestamp(int audioLength, long timestamp) {
        System.out.println("getIndexForAudioTimestamp");
        System.out.println("AUDIOLENGTH: " + audioLength);
        System.out.println("TIMESTAMP: " + timestamp);
        int numSamples = (int) (timestamp * SAMPLE_RATE / 1000);
        System.out.println("NUMSAMPLES: " + numSamples);
        int numBytes = numSamples * 2;
        System.out.println("NUMBYTES: " + numBytes);
        int index = audioLength - numBytes;
        if (index < 0) {
            index = 0;
        }
        System.out.println("FINALINDEX: " + index);
        return index;
    }*/

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


    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}


