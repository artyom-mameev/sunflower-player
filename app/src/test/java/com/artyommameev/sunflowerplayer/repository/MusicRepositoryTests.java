package com.artyommameev.sunflowerplayer.repository;

import static org.junit.Assert.assertSame;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class MusicRepositoryTests {

    @Mock
    private MusicRepository.Track track;

    @Test(expected = NullPointerException.class)
    public void setTrackThrowsNullPointerExceptionIfTrackIsNull() {
        MusicRepository.setTrack(null);
    }

    @Test
    public void getCurrentReturnsTrackThatWasSet() {
        MusicRepository.setTrack(track);

        assertSame(MusicRepository.getCurrent(), track);
    }

    @Test(expected = NullPointerException.class)
    public void trackConstructorThrowsNullPointerExceptionIfTitleIsNull() {
        new MusicRepository.Track(null, "artist", "album",
                0);
    }

    @Test(expected = NullPointerException.class)
    public void trackConstructorThrowsNullPointerExceptionIfArtistIsNull() {
        new MusicRepository.Track("title", null, "album",
                0);
    }

    @Test(expected = NullPointerException.class)
    public void trackConstructorThrowsNullPointerExceptionIfAlbumIsNull() {
        new MusicRepository.Track("title", "artist", null,
                0);
    }
}
