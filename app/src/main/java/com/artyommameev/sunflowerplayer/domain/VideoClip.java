package com.artyommameev.sunflowerplayer.domain;

import java.io.File;
import java.util.Optional;
import java.util.regex.Pattern;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import lombok.val;


/**
 * Extends {@link File} by adding a music tag information.
 *
 * @author Artyom Mameev
 */
public class VideoClip extends File {

    @Getter
    @NonNull
    private final String fileName;
    @Getter
    @Setter
    @NonNull
    private String title;
    @Getter
    @Setter
    @NonNull
    private String artist;
    @Setter
    private String album;

    /**
     * Instantiates a new Video Clip.
     * <p>
     * Sets the fileName field to the result of {@link File#getName()}.
     * If the file name matches the 'artist - title' pattern, sets the artist
     * and title fields accordingly, otherwise sets artist as 'Unknown Artist'
     * and title as file name without extension.
     *
     * @param file the file from which new Video Clip should be created.
     */
    public VideoClip(@NonNull File file) {
        super(file.getAbsolutePath());

        this.fileName = file.getName();

        if (isTagsCreatableFromFileName(fileName)) {
            this.artist = getArtistFromFileName(fileName);
            this.title = getTitleFromFileName(fileName);
        } else {
            this.artist = "Unknown Artist";
            this.title = removeExtension(fileName);
        }
    }

    /**
     * Returns a string representation of the object.
     *
     * @return the string representation of the object in the following
     * format:
     * <p>
     * 'artist' - 'title' - ('album')
     * <p>
     * If the album is null, album states as 'Unknown Album'.
     */
    @androidx.annotation.NonNull
    @Override
    public String getName() {
        return artist + " - " + title +
                " (" + (album == null ? "Unknown Album" : album) + ")";
    }

    /**
     * Returns the album name of the video clip.
     *
     * @return an {@link Optional} of the album name of the video clip.
     */
    public Optional<String> getAlbum() {
        return Optional.ofNullable(album);
    }

    private String getArtistFromFileName(String fileName) {
        fileName = removeExtension(fileName);

        val split = fileName.split(" - ", 2);

        return split[0].trim();
    }

    private String getTitleFromFileName(String fileName) {
        fileName = removeExtension(fileName);

        val split = fileName.split(" - ", 2);

        return split[1].trim();
    }

    private boolean isTagsCreatableFromFileName(String fileName) {
        fileName = removeExtension(fileName);

        val pattern = Pattern.compile("^(.*?)\\s-\\s(.*?)$");

        val matcher = pattern.matcher(fileName);

        return matcher.matches();
    }

    private String removeExtension(String string) {
        return string.replaceFirst("[.][^.]+$", "");
    }
}
