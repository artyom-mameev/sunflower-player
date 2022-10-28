package com.artyommameev.sunflowerplayer.comparator;

import static org.junit.Assert.assertEquals;
import static org.powermock.api.mockito.PowerMockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.File;

@SuppressWarnings("ConstantConditions")
@RunWith(MockitoJUnitRunner.class)
public class FileNameComparatorTests {

    @Mock
    private File file1;
    @Mock
    private File file2;

    private FileNameComparator fileNameComparator;

    @Before
    public void setUp() {
        fileNameComparator = new FileNameComparator();
    }

    @Test(expected = NullPointerException.class)
    public void throwsNullPointerExceptionIfFile1IsNull() {
        fileNameComparator.compare(null, file1);
    }

    @Test(expected = NullPointerException.class)
    public void throwsNullPointerExceptionIfFile2IsNull() {
        fileNameComparator.compare(file1, null);
    }

    @Test
    public void returnsMinus1IfFile1IsDirectoryAndFile2IsNot() {
        when(file1.isDirectory()).thenReturn(true);

        assertEquals(fileNameComparator.compare(file1, file2), -1);
    }

    @Test
    public void returns1IfFile1IsNotDirectoryAndFile2Is() {
        when(file2.isDirectory()).thenReturn(true);

        assertEquals(fileNameComparator.compare(file1, file2), 1);
    }

    @Test
    public void returnsResultOfCompareToOfLowercaseNamesIfBothFilesAreDirectories() {
        when(file1.isDirectory()).thenReturn(true);
        when(file1.getName()).thenReturn("a");
        when(file2.isDirectory()).thenReturn(true);
        when(file2.getName()).thenReturn("B");

        assertEquals(fileNameComparator.compare(file1, file2), -1);
    }

    @Test
    public void returnsResultOfCompareToOfLowercaseNamesIfBothFilesAreNormalFiles() {
        when(file1.getName()).thenReturn("a");
        when(file2.getName()).thenReturn("B");

        assertEquals(fileNameComparator.compare(file1, file2), -1);
    }
}
