package com.artyommameev.sunflowerplayer.database;

import android.app.Activity;

import com.artyommameev.sunflowerplayer.SunflowerPlayer;
import com.artyommameev.sunflowerplayer.domain.Tag;
import com.artyommameev.sunflowerplayer.domain.TagDao;

import java.util.LinkedHashSet;
import java.util.List;

import lombok.val;
import lombok.NonNull;

/**
 * An abstraction for querying, saving and updating {@link Tag}s of
 * {@link VideoClip}s saved in the database.
 *
 * @author Artyom Mameev
 */
public class Database {

    private final TagDao tagDao;

    /**
     * Instantiates a new Database object.
     *
     * @param context the application context.
     * @throws NullPointerException if the context is null.
     */
    public Database(@NonNull Activity context) {
        val daoSession = ((SunflowerPlayer) context.getApplication())
                .getDaoSession();

        tagDao = daoSession.getTagDao();
    }

    /**
     * Queries and returns all {@link Tag}s saved in the database.
     *
     * @return the list of all {@link Tag}s saved in the database.
     */
    public List<Tag> findAll() {
        return tagDao.loadAll();
    }

    /**
     * Queries and returns {@link Tag} for a certain {@link VideoClip} by
     * file name.
     *
     * @param fileName the file name of the {@link VideoClip} which {@link Tag}
     *                 should be returned.
     * @return the {@link Tag} for the {@link VideoClip} with the given file
     * name if it was found into the database, otherwise null.
     * @throws NullPointerException if the file name is null.
     */
    public Tag findTagByFileName(@NonNull String fileName) {
        return tagDao.queryBuilder()
                .where(TagDao.Properties.FileName
                        .eq(fileName))
                .unique();
    }

    /**
     * Queries and returns albums of specific artist using information in
     * {@link Tag}s stored in the database.
     *
     * @param artist the artist which albums should be returned.
     * @return the string array with albums of the given artist if they were
     * found, otherwise returns an empty array.
     * @throws NullPointerException if the artist is null.
     */
    public String[] findAlbumsByArtist(@NonNull String artist) {
        val tags = tagDao.queryBuilder()
                .where(TagDao.Properties.Artist
                        .eq(artist))
                .list();

        val albums = new LinkedHashSet<>();

        for (Tag tag : tags) {
            albums.add(tag.getAlbum());
        }

        val albumsArray = new String[albums.size()];

        albums.toArray(albumsArray);

        return albumsArray;
    }

    /**
     * Stores {@link Tag} of a certain {@link VideoClip} in the database.
     *
     * @param tag the {@link Tag} of a certain {@link VideoClip} that should be
     *            added to the database.
     * @throws NullPointerException if the tag is null.
     */
    public void insertTag(@NonNull Tag tag) {
        tagDao.insert(tag);
    }

    /**
     * Updates {@link Tag} of a certain {@link VideoClip} in the database.
     *
     * @param tag the {@link Tag} of a certain {@link VideoClip} that should be
     *            updated in the database.
     * @throws NullPointerException if the tag is null.
     */
    public void updateTag(@NonNull Tag tag) {
        tagDao.update(tag);
    }

    /**
     * Saves all {@link Tag}s into the database.
     *
     * @param tags the list of {@link Tag}s which should be
     *             saved into the database.
     * @throws NullPointerException if the tags list is null.
     */
    public void saveAll(@NonNull List<Tag> tags) {
        for (val tag : tags) {
            val tagInDatabase = findTagByFileName(tag.getFileName());

            if (tagInDatabase != null) {
                tagInDatabase.setArtist(tag.getArtist());
                tagInDatabase.setTitle(tag.getTitle());
                tagInDatabase.setAlbum(tag.getAlbum());

                updateTag(tagInDatabase);
            } else {
                insertTag(tag);
            }
        }

    }

    /**
     * Removes all {@link Tag}s from the database.
     */
    public void deleteAllTags() {
        tagDao.deleteAll();
    }
}