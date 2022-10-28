package com.artyommameev.sunflowerplayer.comparator;

import java.io.File;
import java.util.Comparator;

import lombok.NonNull;

/**
 * A comparator that sorts files by name (directories before files).
 *
 * @author Artyom Mameev
 */
public final class FileNameComparator implements Comparator<File> {

    /**
     * The compare method.
     *
     * @return -1 if file1 is directory, and file2 is not;<br>
     * 1 if file1 is not directory, and file2 is;<br>
     * a result of {@link String#compareTo(String)} of file1 and file2 names
     * converted to lower case, if both files are directory or both files are
     * normal file.
     * @throws NullPointerException if any parameter is null.
     */
    public int compare(@NonNull File file1, @NonNull File f2) {
        if (file1.isDirectory() && !f2.isDirectory()) {
            return -1;
        } else if (!file1.isDirectory() && f2.isDirectory()) {
            return 1;
        } else {
            return file1.getName().toLowerCase()
                    .compareTo(f2.getName().toLowerCase());
        }
    }
}