package com.artyommameev.sunflowerplayer.database;


import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import android.app.Activity;

import com.artyommameev.sunflowerplayer.SunflowerPlayer;
import com.artyommameev.sunflowerplayer.domain.DaoSession;
import com.artyommameev.sunflowerplayer.domain.Tag;
import com.artyommameev.sunflowerplayer.domain.TagDao;

import org.greenrobot.greendao.Property;
import org.greenrobot.greendao.query.QueryBuilder;
import org.greenrobot.greendao.query.WhereCondition;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings({"ConstantConditions", "unchecked", "JavaReflectionMemberAccess"})
public class DatabaseTests {

    @Mock
    private Activity context;
    @Mock
    private SunflowerPlayer sunflowerPlayer;
    @Mock
    private DaoSession daoSession;
    @Mock
    private TagDao tagDao;
    @Mock
    private QueryBuilder<Tag> queryBuilder;
    @Mock
    private Property property;
    @Mock
    private WhereCondition whereCondition;
    @Mock
    private Tag tag;

    private Database database;

    @Before
    public void setUp() {
        when(context.getApplication()).thenReturn(sunflowerPlayer);
        when(sunflowerPlayer.getDaoSession()).thenReturn(daoSession);
        when(daoSession.getTagDao()).thenReturn(tagDao);
        when(tagDao.queryBuilder()).thenReturn(queryBuilder);


        database = new Database(context);
    }

    @Test(expected = NullPointerException.class)
    public void constructorThrowsNullPointerExceptionIfContextIsNull() {
        new Database(null);
    }

    @Test(expected = NullPointerException.class)
    public void findTagByFileNameThrowsNullPointerExceptionIfFileNameIsNull() {
        database.findTagByFileName(null);
    }

    @Test
    public void findTagByFileNameCreatesQuery() throws Exception {
        QueryBuilder<Tag> queryBuilder2 = mock(QueryBuilder.class);
        when(queryBuilder2.unique()).thenReturn(tag);

        Class<?> properties = TagDao.Properties.class;
        Field fileName = properties.getField("FileName");
        setFinalStatic(fileName, property);

        when(property.eq("fileName")).thenReturn(whereCondition);
        when(queryBuilder.where(whereCondition)).thenReturn(queryBuilder2);

        assertSame(database.findTagByFileName("fileName"), tag);
    }

    @Test(expected = NullPointerException.class)
    public void findAlbumsByArtistThrowsNullPointerExceptionIfArtistIsNull() {
        database.findAlbumsByArtist(null);
    }

    @Test
    public void findAlbumsByArtistCreatesQuery() throws Exception {
        List<Tag> tags = new ArrayList<>();
        tags.add(new Tag("fileName1", "artist1", "title1",
                "album1"));
        tags.add(new Tag("fileName2", "artist2", "title2",
                "album2"));
        tags.add(new Tag("fileName3", "artist3", "title3",
                "album3"));

        QueryBuilder<Tag> queryBuilder2 = mock(QueryBuilder.class);
        when(queryBuilder2.list()).thenReturn(tags);

        Class<?> properties = TagDao.Properties.class;
        Field artist = properties.getField("Artist");
        setFinalStatic(artist, property);

        when(property.eq("artist")).thenReturn(whereCondition);
        when(queryBuilder.where(whereCondition)).thenReturn(queryBuilder2);

        assertArrayEquals(database.findAlbumsByArtist("artist"),
                new String[]{"album1", "album2", "album3"});
    }

    @Test(expected = NullPointerException.class)
    public void insertTagThrowsNullPointerExceptionIfTagIsNull() {
        database.insertTag(null);
    }

    @Test
    public void insertTagInsertsTagIntoDatabase() {
        database.insertTag(tag);

        verify(tagDao, times(1)).insert(tag);
    }

    @Test(expected = NullPointerException.class)
    public void updateTagThrowsNullPointerExceptionIfTagIsNull() {
        database.updateTag(null);
    }

    @Test
    public void updateTagUpdatesTagInDatabase() {
        database.updateTag(tag);

        verify(tagDao, times(1)).update(tag);
    }

    @Test
    public void deleteAllTagsRemovesAllTagsInDatabase() {
        database.deleteAllTags();

        verify(tagDao, times(1)).deleteAll();
    }

    @Test
    public void findAllLoadsAllTagFromDatabase() {
        database.findAll();

        verify(tagDao, times(1)).loadAll();
    }

    @Test
    public void saveAllUpdatesTagIntoTheDatabase() throws Exception {
        QueryBuilder<Tag> queryBuilder2 = mock(QueryBuilder.class);
        when(queryBuilder2.unique()).thenReturn(new Tag(
                "fileName1", "artist2", "title2",
                "album2"));

        Class<?> properties = TagDao.Properties.class;
        Field fileName = properties.getField("FileName");
        setFinalStatic(fileName, property);

        when(property.eq("fileName1")).thenReturn(whereCondition);
        when(queryBuilder.where(whereCondition)).thenReturn(queryBuilder2);

        List<Tag> tags = new ArrayList<>();

        Tag tag = new Tag("fileName1", "artist1", "title1",
                "album1");

        tags.add(tag);

        database.saveAll(tags);

        verify(tagDao, times(1)).update(tag);
    }

    @Test
    public void saveAllInsertsNewTagIntoTheDatabase() throws Exception {
        QueryBuilder<Tag> queryBuilder2 = mock(QueryBuilder.class);
        when(queryBuilder2.unique()).thenReturn(null);

        Class<?> properties = TagDao.Properties.class;
        Field fileName = properties.getField("FileName");
        setFinalStatic(fileName, property);

        when(property.eq("fileName1")).thenReturn(whereCondition);
        when(queryBuilder.where(whereCondition)).thenReturn(queryBuilder2);

        List<Tag> tags = new ArrayList<>();

        Tag tag = new Tag("fileName1", "artist1", "title1",
                "album1");

        tags.add(tag);

        database.saveAll(tags);

        verify(tagDao, times(1)).insert(tag);
    }

    private void setFinalStatic(Field field, Object newValue) throws Exception {
        field.setAccessible(true);

        Field modifiersField = Field.class.getDeclaredField("modifiers");
        modifiersField.setAccessible(true);
        modifiersField.setInt(field, field.getModifiers() & ~Modifier.FINAL);

        field.set(null, newValue);
    }
}
