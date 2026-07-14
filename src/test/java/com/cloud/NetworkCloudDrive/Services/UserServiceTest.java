package com.cloud.NetworkCloudDrive.Services;

import com.cloud.NetworkCloudDrive.Models.DTO.CurrentUserDTO;
import com.cloud.NetworkCloudDrive.Models.Domain.DeletionResults;
import com.cloud.NetworkCloudDrive.Models.Enum.UserRole;
import com.cloud.NetworkCloudDrive.Models.UserEntity;
import com.cloud.NetworkCloudDrive.Persistence.SQLiteDAO;
import com.cloud.NetworkCloudDrive.Repositories.Maintenance.ThumbnailRepository;
import com.cloud.NetworkCloudDrive.Security.EncodingUtility;
import com.cloud.NetworkCloudDrive.Utilities.FileUtility;
import com.cloud.NetworkCloudDrive.Utilities.UserUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private SQLiteDAO sqLiteDAO;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EncodingUtility encodingUtility;
    @Mock
    private UserUtility userUtility;
    @Mock
    private ThumbnailRepository thumbnailRepository;
    @Mock
    private FileUtility fileUtility;

    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService(sqLiteDAO, passwordEncoder, encodingUtility, userUtility, thumbnailRepository, fileUtility);
    }

    private UserEntity createUser(Long id, String name, String mail, String password, UserRole role) {
        UserEntity user = new UserEntity();
        user.setId(id);
        user.setName(name);
        user.setMail(mail);
        user.setPassword(password);
        user.setRole(role);
        user.setRegisteredAt(Instant.now());
        return user;
    }

    @Test
    void currentUserDetails_ReturnsCorrectDTO() {
        UserEntity user = createUser(1L, "testuser", "test@example.com", "encodedpass", UserRole.NORMAL_USER);
        when(sqLiteDAO.findUserByMail("test@example.com")).thenReturn(user);

        CurrentUserDTO result = userService.currentUserDetails("test@example.com");

        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getName());
        assertEquals("test@example.com", result.getMail());
        assertEquals(UserRole.NORMAL_USER, result.getRole());
    }

    @Test
    void registerUser_NewUser_ReturnsSavedUser() {
        String rawPassword = "password123";
        String encodedPassword = "bcrypt_encoded";
        UserEntity userToSave = createUser(null, "newuser", "new@example.com", encodedPassword, UserRole.GUEST);

        when(sqLiteDAO.checkIfUserExistsByMail("new@example.com")).thenReturn(false);
        when(passwordEncoder.encode(rawPassword)).thenReturn(encodedPassword);
        when(sqLiteDAO.saveUser(any(UserEntity.class))).thenReturn(userToSave);

        UserEntity result = userService.registerUser("newuser", "new@example.com", rawPassword);

        assertNotNull(result);
        assertEquals("newuser", result.getName());
        verify(sqLiteDAO).checkIfUserExistsByMail("new@example.com");
        verify(passwordEncoder).encode(rawPassword);
        verify(sqLiteDAO).saveUser(any(UserEntity.class));
    }

    @Test
    void registerUser_DuplicateUser_ThrowsSecurityException() {
        when(sqLiteDAO.checkIfUserExistsByMail("existing@example.com")).thenReturn(true);

        assertThrows(SecurityException.class,
                () -> userService.registerUser("existing", "existing@example.com", "password123"));
        verify(sqLiteDAO, never()).saveUser(any());
    }

    @Test
    void loginUser_CorrectPassword_ReturnsTrue() throws Exception {
        String rawPassword = "mypassword";
        String encodedPassword = "bcrypt_encoded";
        UserEntity user = createUser(1L, "testuser", "test@example.com", encodedPassword, UserRole.GUEST);

        when(sqLiteDAO.findUserByMail("test@example.com")).thenReturn(user);
        when(passwordEncoder.matches(rawPassword, encodedPassword)).thenReturn(true);

        boolean result = userService.loginUser("testuser", "test@example.com", rawPassword);
        assertTrue(result);
    }

    @Test
    void loginUser_WrongPassword_ReturnsFalse() throws Exception {
        String encodedPassword = "bcrypt_encoded";
        UserEntity user = createUser(1L, "testuser", "test@example.com", encodedPassword, UserRole.GUEST);

        when(sqLiteDAO.findUserByMail("test@example.com")).thenReturn(user);
        when(passwordEncoder.matches("wrongpassword", encodedPassword)).thenReturn(false);

        boolean result = userService.loginUser("testuser", "test@example.com", "wrongpassword");
        assertFalse(result);
    }

    @Test
    void updatePassword_EncodesAndSaves() {
        UserEntity user = createUser(1L, "testuser", "test@example.com", "old_hash", UserRole.GUEST);
        when(passwordEncoder.encode("newPassword123")).thenReturn("new_hash");

        CurrentUserDTO result = userService.updatePassword(user, "newPassword123");

        assertEquals("testuser", result.getName());
        verify(passwordEncoder).encode("newPassword123");
        verify(sqLiteDAO).saveUser(user);
        assertEquals("new_hash", user.getPassword());
    }

    @Test
    void updateName_UpdatesDirectoryAndSaves() throws Exception {
        UserEntity user = createUser(1L, "oldname", "test@example.com", "hash", UserRole.GUEST);
        when(encodingUtility.encodeBase32UserFolderName(1L, "oldname", "test@example.com")).thenReturn("old_encoded");
        when(sqLiteDAO.saveUser(any(UserEntity.class))).thenReturn(user);

        CurrentUserDTO result = userService.updateName(user, "newname");

        assertEquals("newname", result.getName());
        verify(userUtility).updateUserDirectoryName(1L, "newname", "test@example.com", "old_encoded");
        verify(sqLiteDAO).saveUser(user);
    }

    @Test
    void updateMail_UpdatesDirectoryAndSaves() throws Exception {
        UserEntity user = createUser(1L, "testuser", "old@example.com", "hash", UserRole.GUEST);
        when(encodingUtility.encodeBase32UserFolderName(1L, "testuser", "old@example.com")).thenReturn("old_encoded");
        when(sqLiteDAO.saveUser(any(UserEntity.class))).thenReturn(user);

        CurrentUserDTO result = userService.updateMail(user, "new@example.com");

        assertEquals("new@example.com", result.getMail());
        verify(userUtility).updateUserDirectoryName(1L, "testuser", "new@example.com", "old_encoded");
        verify(sqLiteDAO).saveUser(user);
    }

    @Test
    void deleteUser_DeletesAllData() throws Exception {
        UserEntity user = createUser(1L, "testuser", "test@example.com", "hash", UserRole.GUEST);
        DeletionResults deletionResults = new DeletionResults();
        when(userUtility.returnUserFolderasPath()).thenReturn(Path.of("/root/userdir"));
        when(fileUtility.deleteFolders(any(Path.class))).thenReturn(deletionResults);

        DeletionResults result = userService.deleteUser(user);

        assertNotNull(result);
        verify(thumbnailRepository).deleteAllThumbnails();
        verify(fileUtility).deleteFolders(any(Path.class));
        verify(sqLiteDAO).deleteAllUserRelatedEntries(1L);
        verify(sqLiteDAO).deleteUser(user);
    }

    @Test
    void loginUser_WhenUserNotFound_ThrowsException() {
        when(sqLiteDAO.findUserByMail("missing@test.com")).thenReturn(null);

        assertThrows(NullPointerException.class,
                () -> userService.loginUser("missing", "missing@test.com", "password"));
    }

    @Test
    void deleteUser_WhenFolderDeletionFails_PropagatesException() throws Exception {
        UserEntity user = createUser(1L, "testuser", "test@example.com", "hash", UserRole.GUEST);
        when(userUtility.returnUserFolderasPath()).thenReturn(Path.of("/root/userdir"));
        when(fileUtility.deleteFolders(any(Path.class))).thenThrow(new IOException("Permission denied"));

        assertThrows(IOException.class,
                () -> userService.deleteUser(user));
        verify(sqLiteDAO, never()).deleteAllUserRelatedEntries(anyLong());
        verify(sqLiteDAO, never()).deleteUser(any());
    }

    @Test
    void elevateUserPrivileges_ReturnsFalse() {
        assertFalse(userService.elevateUserPrivileges());
    }
}
