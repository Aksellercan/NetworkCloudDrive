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
import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

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

    @Test
    void isPathAllowed_WhenPathUnderUserFolder_ReturnsTrue() throws Exception {
        Path userFolder = Paths.get(".", "user_enc");
        when(userUtility.returnUserFolderasPath()).thenReturn(userFolder);

        Path allowedPath = Path.of("user_enc/subdir/file.txt");
        boolean result = pathUtility.isPathAllowed(allowedPath);

        assertTrue(result);
    }

    @Test
    void isPathAllowed_WhenPathOutsideUserFolder_ReturnsFalse() throws Exception {
        when(userUtility.returnUserFolderasPath()).thenReturn(Paths.get(".", "user_enc"));

        Path outsidePath = Path.of("/other/dir/file.txt");
        boolean result = pathUtility.isPathAllowed(outsidePath);

        assertFalse(result);
    }

    @Test
    void getFolderPath_WhenFolderIdIsZero_ReturnsEncodedUserFolder() throws Exception {
        when(userSession.getId()).thenReturn(1L);
        when(userSession.getName()).thenReturn("testuser");
        when(userSession.getMail()).thenReturn("test@example.com");
        when(encodingUtility.encodeBase32UserFolderName(1L, "testuser", "test@example.com")).thenReturn("user_encoded");

        String result = pathUtility.getFolderPath(0L);

        assertEquals("user_encoded", result);
    }

    @Test
    void returnParentFolderPathFromFolderID_ReturnsParentPath() throws Exception {
        com.cloud.NetworkCloudDrive.Models.FolderMetadata folder =
                new com.cloud.NetworkCloudDrive.Models.FolderMetadata("folder", "0/5/3");
        folder.setId(3L);
        when(sqLiteDAO.queryFolderMetadata(3L, 1L)).thenReturn(folder);
        when(userSession.getId()).thenReturn(1L);

        com.cloud.NetworkCloudDrive.Models.FolderMetadata parentFolder =
                new com.cloud.NetworkCloudDrive.Models.FolderMetadata("parent", "0/5");
        parentFolder.setId(5L);
        when(sqLiteDAO.queryFolderMetadata(5L, 1L)).thenReturn(parentFolder);
        when(encodingUtility.encodeBase32UserFolderName(1L, "testuser", "test@example.com")).thenReturn("user_enc");
        when(userSession.getName()).thenReturn("testuser");
        when(userSession.getMail()).thenReturn("test@example.com");

        when(sqLiteDAO.findAllByIdInSQLFolderMetadata(List.of(0L, 5L), 1L)).thenReturn(List.of(parentFolder));

        String result = pathUtility.returnParentFolderPathFromFolderID(3L);

        assertEquals("user_enc" + File.separator + "parent", result);
    }
}
