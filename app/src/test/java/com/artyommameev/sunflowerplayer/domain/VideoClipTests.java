package com.artyommameev.sunflowerplayer.domain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;

@SuppressWarnings({"ConstantConditions", "OptionalGetWithoutIsPresent"})
@RunWith(MockitoJUnitRunner.class)
public class VideoClipTests {

    @Mock
    private File file;

    private VideoClip videoClip;

    @Before
    public void setUp() {
        when(file.getAbsolutePath()).thenReturn("/absolute/path");
        when(file.getName()).thenReturn("artist - title.mp4");

        videoClip = new VideoClip(file);
    }

    @Test(expected = NullPointerException.class)
    public void constructorThrowsNullPointerExceptionIfFileIsNull() {
        new VideoClip(null);
    }

    @Test
    public void constructorSetsFileNameFromFile() {
        assertEquals(new VideoClip(file).getFileName(),
                "artist - title.mp4");
    }

    @Test
    public void constructorSetsArtistAndTitleIfTagsAreCreatableFromFileName() {
        assertEquals(videoClip.getArtist(), "artist");
        assertEquals(videoClip.getTitle(), "title");
    }

    @Test
    public void constructorSetsArtistAndTitleIfTagsAreNotCreatableFromFileName() {
        File fileWithNotAutoCreatableTags = mock(File.class);

        when(fileWithNotAutoCreatableTags
                .getAbsolutePath()).thenReturn("/absolute/path");
        when(fileWithNotAutoCreatableTags
                .getName()).thenReturn("artist title.mp4");

        VideoClip videoClip = new VideoClip(fileWithNotAutoCreatableTags);

        assertEquals(videoClip.getArtist(), "Unknown Artist");
        assertEquals(videoClip.getTitle(), "artist title");
    }

    @Test
    public void getNameReturnsStringRepresentationOfObjectWhenAlbumIsNull() {
        assertEquals(videoClip.getName(), "artist - title (Unknown Album)");
    }

    @Test
    public void getNameReturnsStringRepresentationOfObjectWhenAlbumIsNotNull() {
        videoClip.setAlbum("album");

        assertEquals(videoClip.getName(), "artist - title (album)");
    }

    @Test
    public void getAlbumReturnsEmptyOptionalIfAlbumIsNull() {
        assertFalse(videoClip.getAlbum().isPresent());
    }

    @Test
    public void getAlbumReturnsOptionalWithAlbumIfAlbumIsNotNull() {
        videoClip.setAlbum("album");

        assertEquals(videoClip.getAlbum().get(), "album");
    }

    @Test(expected = NullPointerException.class)
    public void setTitleThrowsNullPointerExceptionIfTitleIsNull() {
        videoClip.setTitle(null);
    }

    @Test(expected = NullPointerException.class)
    public void setArtistThrowsNullPointerExceptionIfArtistIsNull() {
        videoClip.setArtist(null);
    }
}
