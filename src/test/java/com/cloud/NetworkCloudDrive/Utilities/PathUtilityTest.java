package com.cloud.NetworkCloudDrive.Utilities;

import com.cloud.NetworkCloudDrive.Persistence.SQLiteDAO;
import com.cloud.NetworkCloudDrive.Properties.FileStorageProperties;
import com.cloud.NetworkCloudDrive.Security.EncodingUtility;
import com.cloud.NetworkCloudDrive.Sessions.UserSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PathUtilityTest {

    @Mock
    private UserUtility userUtility;
    @Mock
    private FileStorageProperties fileStorageProperties;
    @Mock
    private SQLiteDAO sqLiteDAO;
    @Mock
    private UserSession userSession;
    @Mock
    private EncodingUtility encodingUtility;

    private PathUtility pathUtility;

    @BeforeEach
    void setUp() {
        pathUtility = new PathUtility(userUtility, fileStorageProperties, sqLiteDAO, userSession, encodingUtility);
    }

    @Test
    void isFilenameAllowed_withValidName_ReturnsTrue() {
        assertTrue(pathUtility.isFilenameAllowed("hello.txt"));
    }

    @Test
    void isFilenameAllowed_withValidNameNoExtension_ReturnsTrue() {
        assertTrue(pathUtility.isFilenameAllowed("readme"));
    }

    @Test
    void isFilenameAllowed_withValidComplexName_ReturnsTrue() {
        assertTrue(pathUtility.isFilenameAllowed("my-file.name_v2.ext"));
    }

    @Test
    void isFilenameAllowed_withNull_ReturnsFalse() {
        assertFalse(pathUtility.isFilenameAllowed(null));
    }

    @Test
    void isFilenameAllowed_withEmptyString_ReturnsFalse() {
        assertFalse(pathUtility.isFilenameAllowed(""));
    }

    @Test
    void isFilenameAllowed_withPathTraversal_ReturnsFalse() {
        assertFalse(pathUtility.isFilenameAllowed("../hello.txt"));
    }

    @Test
    void isFilenameAllowed_withForwardSlash_ReturnsFalse() {
        assertFalse(pathUtility.isFilenameAllowed("dir/file.txt"));
    }

    @Test
    void isFilenameAllowed_withBackslash_ReturnsFalse() {
        assertFalse(pathUtility.isFilenameAllowed("dir\\file.txt"));
    }

    @Test
    void isFilenameAllowed_withLeadingDot_ReturnsFalse() {
        assertFalse(pathUtility.isFilenameAllowed(".hidden"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"..", "..file.txt", "a/../b.txt", ".config.swp"})
    void isFilenameAllowed_withVariousInvalidNames_ReturnsFalse(String invalid) {
        assertFalse(pathUtility.isFilenameAllowed(invalid));
    }

    @ParameterizedTest
    @ValueSource(strings = {".gitignore", ".env", ".recycleBin", ".hidden", ".config"})
    void isFilenameAllowed_dotfiles_ReturnsFalse(String dotfile) {
        assertFalse(pathUtility.isFilenameAllowed(dotfile));
    }

    @Test
    void getBasePath_ReturnsPathFromProperties() {
        when(fileStorageProperties.getBasePath()).thenReturn("/root");
        Path result = pathUtility.getBasePath();
        assertEquals(Path.of("/root"), result);
    }

    @Test
    void getBasePathToString_ReturnsStringFromProperties() {
        when(fileStorageProperties.getBasePath()).thenReturn("/root");
        assertEquals("/root", pathUtility.getBasePathToString());
    }

    @Test
    void getFullPathToString_ReturnsFullPath() {
        when(fileStorageProperties.getFullPath("some/path")).thenReturn("/root/some/path");
        assertEquals("/root/some/path", pathUtility.getFullPathToString("some/path"));
    }

    @Test
    void getFullPath_ReturnsPath() {
        when(fileStorageProperties.getFullPath("some/path")).thenReturn("/root/some/path");
        assertEquals(Path.of("/root/some/path"), pathUtility.getFullPath("some/path"));
    }
}
