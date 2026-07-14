package com.cloud.NetworkCloudDrive.Utilities;

import com.cloud.NetworkCloudDrive.Properties.FileStorageProperties;
import com.cloud.NetworkCloudDrive.Security.EncodingUtility;
import com.cloud.NetworkCloudDrive.Sessions.UserSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserUtilityTest {

    @Mock
    private EncodingUtility encodingUtility;
    @Mock
    private UserSession userSession;

    private FileStorageProperties fileStorageProperties;
    private UserUtility userUtility;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        fileStorageProperties = new FileStorageProperties();
        fileStorageProperties.setBasePath(tempDir.toString() + File.separator);
        userUtility = new UserUtility(encodingUtility, fileStorageProperties, userSession);
    }

    @Test
    void createUserDirectory_CreatesDirectory() throws Exception {
        String encodedName = "test_user_encoded";
        when(encodingUtility.encodeBase32UserFolderName(1L, "testuser", "test@test.com")).thenReturn(encodedName);

        Path result = userUtility.createUserDirectory(1L, "testuser", "test@test.com");

        assertTrue(Files.exists(result));
        assertEquals(tempDir.resolve(encodedName), result);
    }

    @Test
    void createUserDirectory_WhenAlreadyExists_ReturnsExisting() throws Exception {
        String encodedName = "existing_user";
        Path existingDir = Files.createDirectory(tempDir.resolve(encodedName));
        when(encodingUtility.encodeBase32UserFolderName(1L, "existing", "existing@test.com")).thenReturn(encodedName);

        Path result = userUtility.createUserDirectory(1L, "existing", "existing@test.com");

        assertTrue(Files.exists(result));
        assertEquals(existingDir, result);
    }

    @Test
    void updateUserDirectoryName_WhenOldPathExists_RenamesDirectory() throws Exception {
        String oldEncoded = "old_encoded_folder";
        String newEncoded = "new_encoded_folder";
        Path oldDir = Files.createDirectory(tempDir.resolve(oldEncoded));
        when(encodingUtility.encodeBase32UserFolderName(1L, "newname", "new@test.com")).thenReturn(newEncoded);

        userUtility.updateUserDirectoryName(1L, "newname", "new@test.com", oldEncoded);

        assertTrue(Files.notExists(oldDir));
        assertTrue(Files.exists(tempDir.resolve(newEncoded)));
    }

    @Test
    void updateUserDirectoryName_WhenOldPathDoesNotExist_ThrowsException() {
        assertThrows(FileSystemException.class,
                () -> userUtility.updateUserDirectoryName(1L, "testuser", "test@test.com", "nonexistent_old"));
    }
}
