package com.cjcj55.literallynot.messaging;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;

import com.cjcj55.literallynot.R;

public class CustomReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Play the custom sound here
        Uri soundUri = Uri.parse("android.resource://" + context.getPackageName() + "/" + R.raw.notification_sound);
        Ringtone ringtone = RingtoneManager.getRingtone(context, soundUri);
        ringtone.play();
    }
}