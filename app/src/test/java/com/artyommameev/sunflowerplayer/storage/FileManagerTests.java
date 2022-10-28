package com.artyommameev.sunflowerplayer.storage;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import android.os.Environment;

import com.artyommameev.sunflowerplayer.comparator.FileNameComparator;
import com.artyommameev.sunflowerplayer.database.Database;
import com.artyommameev.sunflowerplayer.domain.Tag;
import com.artyommameev.sunflowerplayer.domain.VideoClip;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressWarnings("OptionalGetWithoutIsPresent")
@RunWith(PowerMockRunner.class)
@PrepareForTest({Environment.class})
public class FileManagerTests {

    @Mock
    private Database database;
    @Mock
    private File file;

    private FileManager fileManager;
    private File fileA;
    private File fileB;
    private File fileC;

    @Before
    public void setUp() {
        PowerMockito.mockStatic(Environment.class);
        PowerMockito.when(Environment.getExternalStorageDirectory())
                .thenReturn(file);

        fileA = mock(File.class);
        when(fileA.getName()).thenReturn("aArtist - TitleA.mkv");
        when(fileA.getAbsolutePath()).thenReturn("aArtist - TitleA.mkv");
        fileB = mock(File.class);
        when(fileB.getName()).thenReturn("bArtist - TitleB.mkv");
        when(fileB.getAbsolutePath()).thenReturn("bArtist - TitleB.mkv");
        fileC = mock(File.class);
        when(fileC.getName()).thenReturn("cArtist - TitleC.mkv");
        when(fileC.getAbsolutePath()).thenReturn("cArtist - TitleC.mkv");

        when(file.listFiles()).thenReturn(new File[]{fileC, fileB, fileA});

        fileManager = new FileManager(database);
    }

    @Test(expected = NullPointerException.class)
    public void constructorThrowsNullPointerExceptionIfDatabaseIsNull() {
        new FileManager(null);
    }

    @Test(expected = NullPointerException.class)
    public void getFilesThrowsNullPointerExceptionIfComparatorIsNull() {
        fileManager.getFiles(null);
    }

    @Test
    public void getFilesReturnsSortedFilesInCurrentDirectoryIfFilesArePresent() {
        fileA = mock(File.class);
        when(fileA.getName()).thenReturn("aFile.zip");
        fileB = mock(File.class);
        when(fileB.getName()).thenReturn("bFile.zip");
        fileC = mock(File.class);
        when(fileC.getName()).thenReturn("cFile.zip");

        when(file.listFiles()).thenReturn(new File[]{fileC, fileB, fileA});

        List<File> files = fileManager.getFiles(new FileNameComparator());

        assertSame(files.get(0), fileA);
        assertSame(files.get(1), fileB);
        assertSame(files.get(2), fileC);
    }

    @Test
    public void getFilesReturnsFilesWithTaggedVideoClipsInCurrentDirectoryIfFilesArePresentAndTagsArePresentInDatabase() {
        when(database.findTagByFileName("aArtist - TitleA.mkv"))
                .thenReturn(new Tag("aArtist - TitleA.mkv",
                        "tagArtistA", "tagTitleA", "tagAlbumA"));

        when(database.findTagByFileName("bArtist - TitleB.mkv"))
                .thenReturn(new Tag("bArtist - TitleB.mkv",
                        "tagArtistB", "tagTitleB", "tagAlbumB"));

        when(database.findTagByFileName("cArtist - TitleC.mkv"))
                .thenReturn(new Tag("cArtist - TitleC.mkv",
                        "tagArtistC", "tagTitleC", "tagAlbumC"));

        List<File> files = fileManager.getFiles(new FileNameComparator());

        List<VideoClip> videoClips = new ArrayList<>();

        for (File file : files) {
            videoClips.add((VideoClip) file);
        }

        assertEquals(videoClips.get(0).getArtist(), "tagArtistA");
        assertEquals(videoClips.get(0).getTitle(), "tagTitleA");
        assertEquals(videoClips.get(0).getAlbum().get(), "tagAlbumA");
        assertEquals(videoClips.get(1).getArtist(), "tagArtistB");
        assertEquals(videoClips.get(1).getTitle(), "tagTitleB");
        assertEquals(videoClips.get(1).getAlbum().get(), "tagAlbumB");
        assertEquals(videoClips.get(2).getArtist(), "tagArtistC");
        assertEquals(videoClips.get(2).getTitle(), "tagTitleC");
        assertEquals(videoClips.get(2).getAlbum().get(), "tagAlbumC");
    }

    @Test
    public void getFilesReturnsFilesWithTaggedVideoClipsInCurrentDirectoryIfFilesArePresentAndTagsAreNotPresentInDatabase() {
        when(database.findTagByFileName(any())).thenReturn(null);

        List<File> files = fileManager.getFiles(new FileNameComparator());

        List<VideoClip> videoClips = new ArrayList<>();

        for (File file : files) {
            videoClips.add((VideoClip) file);
        }

        assertEquals(videoClips.get(0).getArtist(), "aArtist");
        assertEquals(videoClips.get(0).getTitle(), "TitleA");
        assertEquals(videoClips.get(1).getArtist(), "bArtist");
        assertEquals(videoClips.get(1).getTitle(), "TitleB");
        assertEquals(videoClips.get(2).getArtist(), "cArtist");
        assertEquals(videoClips.get(2).getTitle(), "TitleC");
    }

    @Test
    public void getFilesReturnsEmptyListIfFilesAreNotPresent() {
        File emptyPath = mock(File.class);
        when(emptyPath.listFiles()).thenReturn(null);

        fileManager.toDirectory(emptyPath);

        assertSame(fileManager.getFiles(new FileNameComparator()),
                Collections.emptyList());
    }

    @Test
    public void isParentDirectoryExistsReturnsTrueIfParentDirectoryExists() {
        File emptyPath = mock(File.class);

        when(file.exists()).thenReturn(true);

        when(emptyPath.getParentFile()).thenReturn(file);

        fileManager.toDirectory(emptyPath);

        assertTrue(fileManager.isParentDirectoryExists());
    }

    @Test
    public void isParentDirectoryExistsReturnsFalseIfParentDirectoryNotExists() {
        File emptyPath = mock(File.class);

        fileManager.toDirectory(emptyPath);

        assertFalse(fileManager.isParentDirectoryExists()); // if file == null

        when(emptyPath.getParentFile()).thenReturn(file);

        assertFalse(fileManager.isParentDirectoryExists()); // if file != null &&
        // !file.exists()
    }

    @Test
    public void toParentDirectorySetsCurrentDirectoryAsParentDirectory() {
        File pathWithParent = mock(File.class);

        File parentDirectory = mock(File.class);

        File fileInParentDirectory = mock(File.class);

        when(fileInParentDirectory.getName()).thenReturn("file.zip");

        when(pathWithParent.getParentFile()).thenReturn(parentDirectory);

        when(parentDirectory.listFiles()).thenReturn(new File[]{
                fileInParentDirectory});

        fileManager.toDirectory(pathWithParent);

        fileManager.toParentDirectory();

        assertSame(fileManager.getFiles(new FileNameComparator()).get(0),
                fileInParentDirectory);
    }

    @Test
    public void toDirectorySetsCurrentDirectory() {
        File path = mock(File.class);

        File fileInPath = mock(File.class);

        when(fileInPath.getName()).thenReturn("file.zip");

        when(path.listFiles()).thenReturn(new File[]{
                fileInPath});

        fileManager.toDirectory(path);

        assertSame(fileManager.getFiles(new FileNameComparator()).get(0),
                fileInPath);
    }
}
