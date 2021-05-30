package com.example.dell.readsong1;

import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

public class NotificationBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        if(intent.getAction().equals(NotificationGenerator.NOTIFY_PLAY));
        {
            Toast.makeText(context,"notify play",Toast.LENGTH_LONG);
        }
         if(intent.getAction().equals(NotificationGenerator.NOTIFY_PAUSE));
        {
            Toast.makeText(context,"notify pause",Toast.LENGTH_LONG);
        }
    }
}
