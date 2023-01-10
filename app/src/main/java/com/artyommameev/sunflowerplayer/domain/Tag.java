package com.artyommameev.sunflowerplayer.domain;

import org.greenrobot.greendao.annotation.Entity;
import org.greenrobot.greendao.annotation.Generated;
import org.greenrobot.greendao.annotation.Id;
import org.greenrobot.greendao.annotation.Keep;

import lombok.Data;
import lombok.NonNull;

/**
 * Encapsulates a {@link VideoClip} tag information.
 *
 * @author Artyom Mameev
 */
@Entity
@Data
public class Tag {

    @Id(autoincrement = true)
    private Long id;

    private String fileName;
    private String artist;
    private String title;
    private String album;

    /**
     * Instantiates a new Tag.
     *
     * @param fileName a file name of the {@link VideoClip}.
     * @param artist   an artist of the {@link VideoClip}.
     * @param title    a title of the {@link VideoClip}.
     * @param album    an album of the {@link VideoClip}.
     * @throws NullPointerException if any parameter is null.
     */
    public Tag(@NonNull String fileName, @NonNull String artist,
               @NonNull String title, @NonNull String album) {
        this.fileName = fileName;
        this.artist = artist;
        this.title = title;
        this.album = album;
    }

    @Generated(hash = 827609346)
    public Tag(Long id, String fileName, String artist, String title,
               String album) {
        this.id = id;
        this.fileName = fileName;
        this.artist = artist;
        this.title = title;
        this.album = album;
    }

    @SuppressWarnings("unused")
    @Keep
    public Tag() {
    }

    public Long getId() {
        return this.id;
    }

    public void setId(@NonNull Long id) {
        this.id = id;
    }

    public String getFileName() {
        return this.fileName;
    }

    public void setFileName(@NonNull String fileName) {
        this.fileName = fileName;
    }

    public String getArtist() {
        return this.artist;
    }

    public void setArtist(@NonNull String artist) {
        this.artist = artist;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(@NonNull String title) {
        this.title = title;
    }

    public String getAlbum() {
        return this.album;
    }

    public void setAlbum(@NonNull String album) {
        this.album = album;
    }

    /**
     * Returns a string representation of the object.
     *
     * @return the string representation of the object in the following format:
     * <p>
     * 'artist' - 'title' - ('album')
     */
    @androidx.annotation.NonNull
    @Override
    public String toString() {
        return artist + " - " + title + " (" + album + ")";
    }
}
