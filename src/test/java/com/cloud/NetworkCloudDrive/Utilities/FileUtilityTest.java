package com.cloud.NetworkCloudDrive.Utilities;

import com.cloud.NetworkCloudDrive.Properties.IgnoreFileListProperties;
import com.cloud.NetworkCloudDrive.Security.EncodingUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FileUtilityTest {

    @Mock
    private EncodingUtility encodingUtility;
    @Mock
    private PathUtility pathUtility;

    private IgnoreFileListProperties ignoreFileListProperties;
    private FileUtility fileUtility;

    @BeforeEach
    void setUp() {
        ignoreFileListProperties = new IgnoreFileListProperties();
        fileUtility = new FileUtility(encodingUtility, ignoreFileListProperties, pathUtility);
    }

    @Test
    void getFileExtension_withSimpleExtension() {
        assertEquals(".txt", fileUtility.getFileExtension("file.txt"));
    }

    @Test
    void getFileExtension_withMultipleDots() {
        assertEquals(".gz", fileUtility.getFileExtension("archive.tar.gz"));
    }

    @Test
    void getFileExtension_withNoExtension() {
        assertEquals("", fileUtility.getFileExtension("README"));
    }

    @Test
    void getFileExtension_withLeadingDot() {
        assertEquals("", fileUtility.getFileExtension(".hidden"));
    }

    @Test
    void getFileExtension_withEmptyString() {
        assertEquals("", fileUtility.getFileExtension(""));
    }

    @Test
    void hasFileExtension_withExtension_ReturnsTrue() {
        assertTrue(fileUtility.hasFileExtension("document.pdf"));
    }

    @Test
    void hasFileExtension_withoutExtension_ReturnsFalse() {
        assertFalse(fileUtility.hasFileExtension("README"));
    }

    @Test
    void hasFileExtension_withLeadingDot_ReturnsFalse() {
        assertFalse(fileUtility.hasFileExtension(".gitignore"));
    }

    @Test
    void hasFileExtension_withEmptyString_ReturnsFalse() {
        assertFalse(fileUtility.hasFileExtension(""));
    }

    @ParameterizedTest
    @MethodSource("systemIgnoreFiles")
    void isIgnoredFile_systemFile_ReturnsTrue(String filename) {
        assertTrue(fileUtility.isIgnoredFile(filename));
    }

    @ParameterizedTest
    @MethodSource("apiIgnoreFiles")
    void isIgnoredFile_apiFile_ReturnsTrue(String filename) {
        assertTrue(fileUtility.isIgnoredFile(filename));
    }

    @Test
    void isIgnoredFile_dotfileNotInList_ReturnsFalse() {
        assertFalse(fileUtility.isIgnoredFile(".gitignore"));
        assertFalse(fileUtility.isIgnoredFile(".env"));
        assertFalse(fileUtility.isIgnoredFile(".recycleBin"));
    }

    @Test
    void isIgnoredFile_regularFile_ReturnsFalse() {
        assertFalse(fileUtility.isIgnoredFile("document.txt"));
        assertFalse(fileUtility.isIgnoredFile("image.png"));
    }

    @ParameterizedTest
    @MethodSource("systemIgnoreFiles")
    void isIgnoredSystemFile_systemFile_ReturnsTrue(String filename) {
        assertTrue(fileUtility.isIgnoredSystemFile(filename));
    }

    @Test
    void isIgnoredSystemFile_dotfileNotInList_ReturnsFalse() {
        assertFalse(fileUtility.isIgnoredSystemFile(".gitignore"));
        assertFalse(fileUtility.isIgnoredSystemFile(".env"));
        assertFalse(fileUtility.isIgnoredSystemFile(".recycleBin"));
    }

    @Test
    void isIgnoredSystemFile_regularFile_ReturnsFalse() {
        assertFalse(fileUtility.isIgnoredSystemFile("file.txt"));
    }

    @ParameterizedTest
    @MethodSource("apiIgnoreFiles")
    void isIgnoredAPIFile_apiFile_ReturnsTrue(String filename) {
        assertTrue(fileUtility.isIgnoredAPIFile(filename));
    }

    @Test
    void isIgnoredAPIFile_dotfileNotInList_ReturnsFalse() {
        assertFalse(fileUtility.isIgnoredAPIFile(".gitignore"));
        assertFalse(fileUtility.isIgnoredAPIFile(".env"));
        assertFalse(fileUtility.isIgnoredAPIFile(".recycleBin"));
    }

    @Test
    void isIgnoredAPIFile_regularFile_ReturnsFalse() {
        assertFalse(fileUtility.isIgnoredAPIFile("file.txt"));
    }

    private static Stream<String> systemIgnoreFiles() {
        return new IgnoreFileListProperties().getIgnoreFileList().stream();
    }

    private static Stream<String> apiIgnoreFiles() {
        return new IgnoreFileListProperties().getIgnoreAPIFilesList().stream();
    }

    @Test
    void checkDuplicate_withNoFiles_ReturnsFalse() {
        List<Path> emptyList = List.of();
        assertFalse(fileUtility.checkDuplicate(emptyList, "test.txt"));
    }
}
