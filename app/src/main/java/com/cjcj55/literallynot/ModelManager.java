package com.cjcj55.literallynot;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import edu.cmu.pocketsphinx.Assets;
import edu.cmu.pocketsphinx.Config;
import edu.cmu.pocketsphinx.Decoder;
import edu.cmu.pocketsphinx.SpeechRecognizerSetup;

public class ModelManager {

    private static ModelManager instance;
    private String modelPath;

    private ModelManager() {
        // private constructor to prevent direct instantiation
    }

    public static synchronized ModelManager getInstance() {
        if (instance == null) {
            instance = new ModelManager();
        }
        return instance;
    }

    public void initModel(Context context, Callback callback) {
        try {
            Assets assets = new Assets(context);
            File modelsDir = new File(context.getFilesDir(), "models");
            if (!modelsDir.exists()) {
                modelsDir.mkdirs();
            }
            String[] files = assets.syncAssets(modelsDir.getAbsolutePath());
            modelPath = modelsDir.getAbsolutePath();

            callback.onSuccess();
        } catch (IOException e) {
            callback.onFailure(e);
        }
    }

    public Decoder createDecoder() {
        Config config = new Config();
        config.setString("-hmm", modelPath + "/en-us");
        config.setString("-dict", modelPath + "/cmudict-en-us.dict");
        config.setString("-lm", modelPath + "/en-us.lm.bin");
        config.setBoolean("-allphone_ci", true);

        return SpeechRecognizerSetup.defaultSetup()
                .setDecoder(Decoder.class)
                .setRawLogDir(modelPath)
                .setConfig(config)
                .getRecognizer();
    }

    public interface Callback {
        void onSuccess();
        void onFailure(Exception exception);
    }
}