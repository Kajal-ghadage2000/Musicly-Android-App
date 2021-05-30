package com.example.dell.readsong1;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.widget.RemoteViews;

class NotificationGenerator {

    private static final int NOTIFICATION_ID_OPEN_ACTIVITY=9;

    private static final String NOTIFY_NEXT="com.example.dell.readsong1.next";
    private static final String NOTIFY_PREVIOUS="com.example.dell.readsong1.previous";
    private static final String NOTIFY_DELETE="com.example.dell.readsong1.delete";
    public static final String NOTIFY_PAUSE="com.example.dell.readsong1.pause";
    public static final String NOTIFY_PLAY="com.example.dell.readsong1.play";

    public static void openActivityNotification(Context applicationContext) {
        NotificationCompat.Builder nc=new NotificationCompat.Builder(applicationContext);
        NotificationManager nm=(NotificationManager)applicationContext.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent=new Intent(applicationContext,MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent=PendingIntent.getActivity(applicationContext,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        nc.setContentIntent(pendingIntent);
        nc.setSmallIcon(R.drawable.list);
        nc.setAutoCancel(true);
        nc.setContentTitle("title");
        nc.setContentText("text");

        nm.notify(NOTIFICATION_ID_OPEN_ACTIVITY,nc.build());
    }

    @SuppressLint("RestrictedApi")
    public static void customBigNotification(Context context) {
        RemoteViews extendedView=new RemoteViews(context.getPackageName(),R.layout.notification_layout_big);
        NotificationCompat.Builder nc=new NotificationCompat.Builder(context);
        NotificationManager nm=(NotificationManager)context.getSystemService(Context.NOTIFICATION_SERVICE);

        Intent intent=new Intent(context,MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK |Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent pendingIntent=PendingIntent.getActivity(context,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        nc.setContentIntent(pendingIntent);
        nc.setSmallIcon(R.drawable.list);
        nc.setAutoCancel(true);
        nc.setContentTitle("Music player");
        nc.setContentText("control audio");
        nc.setCustomBigContentView(extendedView);
        nc.getBigContentView().setTextViewText(R.id.textView5,"my title");

        nm.notify(NOTIFICATION_ID_OPEN_ACTIVITY,nc.build());

        setListners(extendedView,context);
    }

    private static void setListners(RemoteViews extendedView, Context context) {

        Intent previous = new Intent(NOTIFY_PREVIOUS);
        Intent play = new Intent(NOTIFY_PLAY);
        Intent pause = new Intent(NOTIFY_PAUSE);
        Intent next = new Intent(NOTIFY_NEXT);

        PendingIntent pplay = PendingIntent.getBroadcast(context, 0, play, PendingIntent.FLAG_UPDATE_CURRENT);
        extendedView.setOnClickPendingIntent(R.id.button2,pplay);
        PendingIntent ppause = PendingIntent.getBroadcast(context, 0, pause, PendingIntent.FLAG_UPDATE_CURRENT);
        extendedView.setOnClickPendingIntent(R.id.button,ppause);
    }
}
