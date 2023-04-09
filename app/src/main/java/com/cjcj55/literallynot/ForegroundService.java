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
import android.content.res.Configuration;
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
import java.util.List;

import edu.cmu.pocketsphinx.Config;
import edu.cmu.pocketsphinx.Decoder;
import edu.cmu.pocketsphinx.Segment;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

public class ForegroundService extends Service {


    private Model mModel;
    private static final String TAG = "ForegroundService";
    private static final int SAMPLE_RATE = 44100;
    
    private static final int BUFFER_SIZE = SAMPLE_RATE * 15; //25 seconds of audio buffer
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
        /*Model model = ModelManager.getInstance().getModel();
        if (model != null) {
            // use the model
            mModel = model;
        } else {
            // handle error
            System.out.println("No model loaded..?");
        }
*/

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
            //UNCOMMENT@@@ THIS TO START SPEECH PROCESS 98-104, 122-123
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
        if(mHandlerThread!=null) {
            mHandlerThread.quitSafely();
        }
        if(mAudioRecord!=null) {
            mAudioRecord.stop();
            mAudioRecord.release();
        }
        if(mSpeechRecognizer!=null) {
            mSpeechRecognizer.destroy();
        }
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
            if (text.contains(KEYWORD)) {
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
            Config c = Decoder.defaultConfig();
            c.setString("-hmm", "../../model/en-us/en-us");
            c.setString("-lm", "../../model/en-us/en-us.lm.dmp");
            c.setString("-dict", "../../model/en-us/cmudict-en-us.dict");
            Decoder d = new Decoder(c);

            d.startUtt();
            ByteBuffer bb = ByteBuffer.wrap(audioData);
            bb.order(ByteOrder.LITTLE_ENDIAN);
            short[] s = new short[audioData.length / 2];
            bb.asShortBuffer().get(s);
            d.startUtt();
            d.processRaw(s, s.length, false, false);
            d.endUtt();
            StringBuilder transcriptBuilder = new StringBuilder();
            for (Segment seg : d.seg()) {
                transcriptBuilder.append(seg.getWord()).append(" ");
            }
            String transcript = transcriptBuilder.toString().trim();
            return transcript;
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

    }
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}


