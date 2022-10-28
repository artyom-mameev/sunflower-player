package com.artyommameev.sunflowerplayer.util;

import android.content.Context;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;

import androidx.core.app.NotificationCompat;
import androidx.media.session.MediaButtonReceiver;

import lombok.val;

/**
 * Helper APIs for constructing MediaStyle notifications
 *
 * @author ianhanniballake
 * <a>https://gist.github.com/ianhanniballake/47617ec3488e0257325c</a>
 */
public class MediaStyleHelper {
    /**
     * Build a notification using the information from the given media session.
     * Makes heavy use of {@link MediaMetadataCompat#getDescription()} to extract
     * the appropriate information.
     *
     * @param context      Context used to construct the notification.
     * @param mediaSession Media session to get information.
     * @return A pre-built notification with information from the given media
     * session.
     */
    @SuppressWarnings("deprecation")
    public static NotificationCompat.Builder from(Context context,
                                                  MediaSessionCompat mediaSession) {
        val controller = mediaSession.getController();
        val mediaMetadata = controller.getMetadata();
        val description = mediaMetadata.getDescription();

        val builder = new NotificationCompat.Builder(context);

        builder
                .setContentTitle(description.getTitle())
                .setContentText(description.getSubtitle())
                .setSubText(description.getDescription())
                .setLargeIcon(description.getIconBitmap())
                .setContentIntent(controller.getSessionActivity())
                .setDeleteIntent(
                        MediaButtonReceiver.buildMediaButtonPendingIntent(
                                context, PlaybackStateCompat.ACTION_STOP))
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        return builder;
    }
}

