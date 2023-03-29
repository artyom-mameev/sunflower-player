package com.artyommameev.sunflowerplayer.storage;

import android.os.Environment;

import com.artyommameev.sunflowerplayer.database.Database;
import com.artyommameev.sunflowerplayer.domain.VideoClip;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import lombok.NonNull;
import lombok.val;

/**
 * A simple file manager with helper functionality for working with
 * {@link VideoClip}s.
 *
 * @author Artyom Mameev
 */
public class FileManager {

    public static final String[] VIDEO_EXTENSIONS = {"m4v", "mp4", "mkv",
            "webm", "ts", "flv"};

    private final Database database;
    private File directory;

    /**
     * Instantiates a new File Manager with default directory as an external
     * storage directory.
     *
     * @param database the application database.
     * @throws NullPointerException if the database is null.
     */
    public FileManager(@NonNull Database database) {
        directory = Environment.getExternalStorageDirectory();

        this.database = database;
    }

    /**
     * Returns all files in the current directory.
     * <p>
     * If any file is a file with one of the extensions specified in
     * the constant {@link FileManager#VIDEO_EXTENSIONS}, a {@link VideoClip}
     * is created based on it.
     *
     * @param comparator the comparator to sort the files.
     * @return the files in the current directory if they are present,
     * otherwise returns an empty list.
     */
    public List<File> getFiles(@NonNull Comparator<File> comparator) {
        val currentFiles = directory.listFiles();

        if (currentFiles == null) {
            return Collections.emptyList();
        }

        val currentFilesList = Arrays.stream(currentFiles)
                .sorted(comparator)
                .collect(Collectors.toList());

        scanForVideoClips(currentFilesList);

        return currentFilesList;
    }

    /**
     * Checks if the current directory has a parent directory.
     *
     * @return true if the parent directory exists, otherwise false.
     */
    public boolean isParentDirectoryExists() {
        return (directory.getParentFile() != null &&
                directory.getParentFile().exists());
    }

    /**
     * Sets the current directory to a parent directory of the current
     * directory.
     */
    public void toParentDirectory() {
        directory = directory.getParentFile();
    }

    /**
     * Sets a new directory as the current directory.
     *
     * @param newDirectory the new directory that should be set as the current
     *                     directory.
     * @throws NullPointerException if the new directory is null.
     */
    public void toDirectory(@NonNull File newDirectory) {
        directory = newDirectory;
    }

    private void scanForVideoClips(@NonNull List<File> files) {
        for (int i = 0; i < files.size(); i++) {
            if (!isVideoClip(files.get(i).getName())) {
                continue;
            }

            val originalFile = files.get(i);

            val videoClip = new VideoClip(originalFile);

            val tag = database.findTagByFileName(videoClip.getFileName());

            if (tag != null) {
                videoClip.setArtist(tag.getArtist());
                videoClip.setTitle(tag.getTitle());
                videoClip.setAlbum(tag.getAlbum());
            }

            files.set(i, videoClip);
        }
    }

    private boolean isVideoClip(String name) {
        for (String videoExtension : VIDEO_EXTENSIONS) {
            if (name.endsWith(videoExtension)) {
                return true;
            }
        }

        return false;
    }
}
