package com.artyommameev.sunflowerplayer.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.session.MediaSession;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.media.session.MediaButtonReceiver;

import com.artyommameev.sunflowerplayer.R;
import com.artyommameev.sunflowerplayer.repository.MusicRepository;
import com.artyommameev.sunflowerplayer.util.MediaStyleHelper;

import lombok.val;

/**
 * A service for updating the {@link MediaSession} with currently playing
 * music track.
 *
 * @author Sergey Vinyarsky
 * <a>https://github.com/SergeyVinyar/AndroidAudioExample</a>
 */
final public class PlayerService extends Service {

    private final static int NOTIFICATION_ID = 404;
    private final MediaMetadataCompat.Builder metadataBuilder =
            new MediaMetadataCompat.Builder();
    private final PlaybackStateCompat.Builder stateBuilder =
            new PlaybackStateCompat.Builder()
                    .setActions(PlaybackStateCompat.ACTION_PLAY |
                            PlaybackStateCompat.ACTION_STOP |
                            PlaybackStateCompat.ACTION_PAUSE |
                            PlaybackStateCompat.ACTION_PLAY_PAUSE
                    );
    private final BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // Disconnecting headphones - stop playback
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(
                    intent.getAction())) {
                mediaSessionCallback.onPause();
            }
        }
    };
    private String NOTIFICATION_DEFAULT_CHANNEL_ID;
    private MediaSessionCompat mediaSession;
    private final MediaSessionCompat.Callback mediaSessionCallback =
            new MediaSessionCompat.Callback() {
                int currentState = PlaybackStateCompat.STATE_STOPPED;

                @Override
                public void onPlay() {
                    startService(new Intent(getApplicationContext(),
                            PlayerService.class));

                    val track = MusicRepository.getCurrent();

                    updateMetadataFromTrack(track);

                    mediaSession.setActive(true);

                    registerReceiver(becomingNoisyReceiver, new IntentFilter(
                            AudioManager.ACTION_AUDIO_BECOMING_NOISY));

                    mediaSession.setPlaybackState(stateBuilder.setState(
                            PlaybackStateCompat.STATE_PLAYING,
                            PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1)
                            .build());

                    currentState = PlaybackStateCompat.STATE_PLAYING;

                    refreshNotificationAndForegroundStatus(currentState);
                }

                @Override
                public void onPause() {
                    mediaSession.setPlaybackState(stateBuilder.setState(
                            PlaybackStateCompat.STATE_PAUSED,
                            PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1)
                            .build());

                    currentState = PlaybackStateCompat.STATE_PAUSED;

                    refreshNotificationAndForegroundStatus(currentState);
                }

                @Override
                public void onStop() {
                    mediaSession.setActive(false);

                    mediaSession.setPlaybackState(stateBuilder.setState(
                            PlaybackStateCompat.STATE_STOPPED,
                            PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1)
                            .build());

                    currentState = PlaybackStateCompat.STATE_STOPPED;

                    refreshNotificationAndForegroundStatus(currentState);

                    stopSelf();
                }

                private void updateMetadataFromTrack(MusicRepository.Track track) {
                    metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_TITLE,
                            track.getTitle());
                    metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM,
                            track.getAlbum());
                    metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST,
                            track.getArtist());
                    metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION,
                            track.getDuration());

                    mediaSession.setMetadata(metadataBuilder.build());
                }
            };

    @Override
    public void onCreate() {
        super.onCreate();

        NOTIFICATION_DEFAULT_CHANNEL_ID = getString(R.string.default_channel_id);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            @SuppressLint("WrongConstant")
            val notificationChannel = new NotificationChannel(
                    NOTIFICATION_DEFAULT_CHANNEL_ID, getString(R.string.channel_name),
                    NotificationManagerCompat.IMPORTANCE_DEFAULT);

            val notificationManager = (NotificationManager) getSystemService(
                    Context.NOTIFICATION_SERVICE);

            notificationManager.createNotificationChannel(notificationChannel);
        }

        mediaSession = new MediaSessionCompat(this, getString(R.string.player_service));

        mediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        mediaSession.setCallback(mediaSessionCallback);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MediaButtonReceiver.handleIntent(mediaSession, intent);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mediaSession.release();

        unregisterReceiver(becomingNoisyReceiver);
    }

    @NonNull
    @Override
    public IBinder onBind(Intent intent) {
        return new PlayerServiceBinder();
    }

    private void refreshNotificationAndForegroundStatus(int playbackState) {
        switch (playbackState) {
            case PlaybackStateCompat.STATE_PLAYING: {
                startForeground(NOTIFICATION_ID, getNotification(playbackState));

                break;
            }

            case PlaybackStateCompat.STATE_PAUSED: {
                NotificationManagerCompat.from(PlayerService.this).notify(
                        NOTIFICATION_ID, getNotification(playbackState));
                stopForeground(false);

                break;
            }

            default: {
                stopForeground(true);

                break;
            }
        }
    }

    private Notification getNotification(int playbackState) {
        NotificationCompat.Builder builder = MediaStyleHelper.from(
                this, mediaSession);

        if (playbackState == PlaybackStateCompat.STATE_PLAYING)
            builder.addAction(new NotificationCompat.Action(
                    android.R.drawable.ic_media_pause,
                    getString(R.string.pause), MediaButtonReceiver.buildMediaButtonPendingIntent(
                    this, PlaybackStateCompat.ACTION_PLAY_PAUSE)));
        else
            builder.addAction(new NotificationCompat.Action(
                    android.R.drawable.ic_media_play,
                    getString(R.string.play), MediaButtonReceiver.buildMediaButtonPendingIntent(
                    this, PlaybackStateCompat.ACTION_PLAY_PAUSE)));

        builder.setStyle(new androidx.media.app.NotificationCompat.MediaStyle()
                .setShowActionsInCompactView(0)
                .setShowCancelButton(true)
                .setCancelButtonIntent(MediaButtonReceiver
                        .buildMediaButtonPendingIntent(this,
                                PlaybackStateCompat.ACTION_STOP))
                .setMediaSession(mediaSession.getSessionToken()));
        builder.setLargeIcon(BitmapFactory.decodeResource(getResources(),
                R.mipmap.ic_launcher));
        builder.setSmallIcon(R.drawable.ic_wb_sunny_black_24dp);
        builder.setColor(ContextCompat.getColor(this,
                R.color.colorPrimaryDark));
        builder.setShowWhen(false);
        builder.setPriority(NotificationCompat.PRIORITY_HIGH);
        builder.setOnlyAlertOnce(true);
        builder.setChannelId(NOTIFICATION_DEFAULT_CHANNEL_ID);

        return builder.build();
    }

    public class PlayerServiceBinder extends Binder {
        public MediaSessionCompat.Token getMediaSessionToken() {
            return mediaSession.getSessionToken();
        }
    }
}
