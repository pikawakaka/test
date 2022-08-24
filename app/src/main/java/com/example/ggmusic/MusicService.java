package com.example.ggmusic;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;

import java.io.IOException;

public class MusicService extends Service {


    MediaPlayer mMediaPlayer;
    private static final int ONGOING_NOTIFICATION_ID = 1001;
    private static final String CHANNEL_ID = "Music channel";
    NotificationManager mNotificationManager;
    private final IBinder mBinder = new MusicServiceBinder();
    public class MusicServiceBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    public MusicService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }
    /** method for clients */
    public void pause() {
        if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
    }

    public void play() {
        if (mMediaPlayer != null) {
            mMediaPlayer.start();
        }
    }


    public int getDuration() {
        int duration = 0;

        if (mMediaPlayer != null) {
            duration = mMediaPlayer.getDuration();
        }

        return duration;
    }

    public int getCurrentPosition() {
        int position = 0;

        if (mMediaPlayer != null) {
            position = mMediaPlayer.getCurrentPosition();
        }

        return position;
    }

    public boolean isPlaying() {

        if (mMediaPlayer != null) {
            return mMediaPlayer.isPlaying();
        }
        return false;
    }

    @Override
    public void onDestroy() {
        mMediaPlayer.stop();
        mMediaPlayer.release();
        mMediaPlayer = null;
        super.onDestroy();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mMediaPlayer = new MediaPlayer();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String data = intent.getStringExtra(MainActivity.DATA_URI);
        String title = intent.getStringExtra(MainActivity.TITLE);
        String artist = intent.getStringExtra(MainActivity.ARTIST);
        Uri dataUri = Uri.parse(data);

        if (mMediaPlayer != null) {
            try {
                if(mMediaPlayer.isPlaying()){
                    mMediaPlayer.stop();
                }
                mMediaPlayer.reset();
                mMediaPlayer.setDataSource(
                        getApplicationContext(),
                        dataUri);
                mMediaPlayer.prepare();
                mMediaPlayer.start();

                Intent musicStartIntent = new Intent(MainActivity.ACTION_MUSIC_START);
                sendBroadcast(musicStartIntent);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mNotificationManager =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID,
                    "Music Channel", NotificationManager.IMPORTANCE_HIGH);

            if (mNotificationManager != null) {
                mNotificationManager.createNotificationChannel(channel);
            }
        }

        Intent notificationIntent =
                new Intent(getApplicationContext(),
                        MainActivity.class);
        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        getApplicationContext(),
                        0, notificationIntent, 0);

        NotificationCompat.Builder builder =
                new NotificationCompat.Builder(
                        getApplicationContext(),
                        CHANNEL_ID);

        Notification notification = builder
                .setContentTitle(title)
                .setContentText(artist)
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentIntent(pendingIntent).build();

        startForeground(ONGOING_NOTIFICATION_ID, notification);

        return super.onStartCommand(intent, flags, startId);
    }






}