package com.artyommameev.sunflowerplayer.domain;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

public class TagTests {

    private Tag tagWithFirstConstructor;
    private Tag tagWithSecondConstructor;

    @Before
    public void setUp() {
        tagWithFirstConstructor = new Tag("fileName", "artist",
                "title", "album");

        tagWithSecondConstructor = new Tag(1L, "fileName", "artist",
                "title", "album");
    }

    @Test(expected = NullPointerException.class)
    public void firstConstructorThrowsNullPointerExceptionIfFileNameIsNull() {
        new Tag(null, "artist", "title", "album");
    }

    @Test(expected = NullPointerException.class)
    public void firstConstructorThrowsNullPointerExceptionIfArtistNameIsNull() {
        new Tag("fileName", null, "title", "album");
    }

    @Test(expected = NullPointerException.class)
    public void firstConstructorThrowsNullPointerExceptionIfTitleIsNull() {
        new Tag("fileName", "artist", null, "album");
    }

    @Test(expected = NullPointerException.class)
    public void firstConstructorThrowsNullPointerExceptionIfAlbumIsNull() {
        new Tag("fileName", "artist", "title", null);
    }

    @Test
    public void firstConstructorProperlyConstructsObject() {
        assertEquals(tagWithFirstConstructor.getFileName(), "fileName");
        assertEquals(tagWithFirstConstructor.getArtist(), "artist");
        assertEquals(tagWithFirstConstructor.getTitle(), "title");
        assertEquals(tagWithFirstConstructor.getAlbum(), "album");
    }

    @Test
    public void secondConstructorProperlyConstructsObject() {
        assertEquals(tagWithSecondConstructor.getId(), (Long) 1L);
        assertEquals(tagWithSecondConstructor.getFileName(), "fileName");
        assertEquals(tagWithSecondConstructor.getArtist(), "artist");
        assertEquals(tagWithSecondConstructor.getTitle(), "title");
        assertEquals(tagWithSecondConstructor.getAlbum(), "album");
    }

    @Test(expected = NullPointerException.class)
    public void setFileNameThrowsNullPointerExceptionIfFileNameIsNull() {
        tagWithFirstConstructor.setFileName(null);
    }

    @Test(expected = NullPointerException.class)
    public void setArtistThrowsNullPointerExceptionIfArtistIsNull() {
        tagWithFirstConstructor.setArtist(null);
    }

    @Test(expected = NullPointerException.class)
    public void setTitleThrowsNullPointerExceptionIfTitleIsNull() {
        tagWithFirstConstructor.setTitle(null);
    }

    @Test(expected = NullPointerException.class)
    public void setAlbumThrowsNullPointerExceptionIfAlbumIsNull() {
        tagWithFirstConstructor.setAlbum(null);
    }

    @Test
    public void toStringReturnsStringRepresentationOfObject() {
        assertEquals(tagWithFirstConstructor.toString(),
                "artist - title (album)");
    }
}
