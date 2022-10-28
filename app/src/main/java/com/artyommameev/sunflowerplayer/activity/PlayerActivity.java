package com.artyommameev.sunflowerplayer.activity;

import android.content.ComponentName;
import android.content.Intent;
import android.content.ServiceConnection;
import android.media.session.MediaSession;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.artyommameev.sunflowerplayer.R;
import com.artyommameev.sunflowerplayer.domain.VideoClip;
import com.artyommameev.sunflowerplayer.repository.MusicRepository;
import com.artyommameev.sunflowerplayer.service.PlayerService;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import java.util.Objects;

import lombok.val;

/**
 * The player activity, which allows user to view {@link VideoClip}s which
 * information automatically sends to {@link MediaSession}.
 *
 * @author Artyom Mameev
 */
public class PlayerActivity extends AppCompatActivity {

    private SimpleExoPlayer simpleExoPlayer;

    private String artist, title, album;

    private ServiceConnection serviceConnection;
    private PlayerService.PlayerServiceBinder playerServiceBinder;
    private MediaControllerCompat mediaController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_player);

        simpleExoPlayer = new SimpleExoPlayer.Builder(this).build();

        PlayerView playerView = findViewById(R.id.player);

        playerView.setPlayer(simpleExoPlayer);

        val dataSourceFactory = new DefaultDataSourceFactory(this,
                Util.getUserAgent(this, getString(R.string.app_name)));

        val extras = getIntent().getExtras();

        val videoClip = (VideoClip) Objects.requireNonNull(extras).get(getString(R.string.clip_key));

        if (videoClip == null) {
            throw new NullPointerException("videoClip cannot be null");
        }

        artist = videoClip.getArtist();
        title = videoClip.getTitle();

        if (videoClip.getAlbum().isPresent()) {
            album = videoClip.getAlbum().get();
        } else {
            album = "";
        }

        val videoSource = new ProgressiveMediaSource
                .Factory(dataSourceFactory)
                .createMediaSource(Uri.fromFile(videoClip));

        simpleExoPlayer.prepare(videoSource);
        simpleExoPlayer.setPlayWhenReady(true);

        playerView.hideController();

        setUpPlayerService();

        setUpPlayerEvents();
    }

    private void setUpPlayerService() {
        serviceConnection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName,
                                           IBinder service) {
                playerServiceBinder = (PlayerService.PlayerServiceBinder) service;

                try {
                    mediaController = new MediaControllerCompat(
                            PlayerActivity.this,
                            playerServiceBinder.getMediaSessionToken());

                    mediaController.registerCallback(
                            new MediaControllerCompat.Callback() {
                                @Override
                                public void onPlaybackStateChanged(
                                        PlaybackStateCompat state) {
                                    if (state == null) {
                                        return;
                                    }

                                    switch (state.getState()) {
                                        case PlaybackStateCompat.STATE_PAUSED:
                                            simpleExoPlayer.setPlayWhenReady(
                                                    false);
                                            break;

                                        case PlaybackStateCompat.STATE_PLAYING:
                                            simpleExoPlayer.setPlayWhenReady(
                                                    true);
                                            break;

                                        case PlaybackStateCompat.STATE_BUFFERING:
                                        case PlaybackStateCompat.STATE_CONNECTING:
                                        case PlaybackStateCompat.STATE_ERROR:
                                        case PlaybackStateCompat.STATE_FAST_FORWARDING:
                                        case PlaybackStateCompat.STATE_NONE:
                                        case PlaybackStateCompat.STATE_REWINDING:
                                        case PlaybackStateCompat.STATE_SKIPPING_TO_NEXT:
                                        case PlaybackStateCompat.STATE_SKIPPING_TO_PREVIOUS:
                                        case PlaybackStateCompat.STATE_SKIPPING_TO_QUEUE_ITEM:
                                        case PlaybackStateCompat.STATE_STOPPED:
                                            break;
                                    }
                                }
                            }
                    );
                } catch (RemoteException e) {
                    mediaController = null;
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                playerServiceBinder = null;
                mediaController = null;
            }
        };

        bindService(new Intent(this, PlayerService.class),
                serviceConnection, BIND_AUTO_CREATE);
    }

    private void setUpPlayerEvents() {
        simpleExoPlayer.addListener(new Player.EventListener() {
            @Override
            public void onPlayerStateChanged(boolean playWhenReady,
                                             int playbackState) {
                if (mediaController == null) {
                    return;
                }

                if (playWhenReady & playbackState == Player.STATE_READY) { //play
                    val track = new MusicRepository.Track(title, artist,
                            album, simpleExoPlayer.getDuration());

                    MusicRepository.setTrack(track);

                    mediaController.getTransportControls().play();
                }

                if (!playWhenReady & playbackState == Player.STATE_READY) { //pause
                    mediaController.getTransportControls().pause();
                }

                if (playbackState == Player.STATE_ENDED) { //stop
                    mediaController.getTransportControls().stop();

                    finish();
                }
            }

            @Override
            public void onPlayerError(@NonNull ExoPlaybackException error) {
                simpleExoPlayer.stop();
                simpleExoPlayer.release();

                if (mediaController != null) {
                    mediaController.getTransportControls().stop();
                }

                finish();
            }
        });
    }

    @Override
    public void onBackPressed() {
        simpleExoPlayer.stop();
        simpleExoPlayer.release();

        if (mediaController != null) {
            mediaController.getTransportControls().stop();
        }

        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();

        simpleExoPlayer.setPlayWhenReady(false);

        if (serviceConnection != null) {
            unbindService(serviceConnection);
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        bindService(new Intent(this, PlayerService.class),
                serviceConnection, BIND_AUTO_CREATE);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        simpleExoPlayer.stop();
        simpleExoPlayer.release();
    }
}
