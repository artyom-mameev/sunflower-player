package com.artyommameev.sunflowerplayer.repository;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;


/**
 * Represents a repository for currently playing music track.
 *
 * @author Artyom Mameev
 */
public final class MusicRepository {

    @Setter
    @NonNull
    private static Track track;

    /**
     * Returns a current playing music track.
     *
     * @return the current playing music {@link Track}.
     */
    public static Track getCurrent() {
        return track;
    }

    /**
     * Represents a currently playing music track.
     */
    public static class Track {

        @Getter
        private final String title;
        @Getter
        private final String artist;
        @Getter
        private final String album;
        @Getter
        private final long duration; // in ms

        /**
         * Instantiates a new Track.
         *
         * @param title    a title of the the track.
         * @param artist   an artist the track.
         * @param album    an album the track.
         * @param duration duration of the track in milliseconds.
         * @throws NullPointerException if any parameter is null
         */
        public Track(@NonNull String title, @NonNull String artist,
                     @NonNull String album, long duration) {
            this.title = title;
            this.artist = artist;
            this.album = album;
            this.duration = duration;
        }
    }
}
