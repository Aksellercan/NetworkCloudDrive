package com.cloud.NetworkCloudDrive.Controllers;

import com.cloud.NetworkCloudDrive.Models.DTO.UpdateUserDTO;
import com.cloud.NetworkCloudDrive.Models.DTO.UserDTO;
import com.cloud.NetworkCloudDrive.Models.DTO.CurrentUserDTO;
import com.cloud.NetworkCloudDrive.Models.Enum.UserRole;
import com.cloud.NetworkCloudDrive.Models.UserEntity;
import com.cloud.NetworkCloudDrive.Persistence.SQLiteDAO;
import com.cloud.NetworkCloudDrive.Repositories.UserRepository;
import com.cloud.NetworkCloudDrive.Sessions.UserSession;
import com.cloud.NetworkCloudDrive.Utilities.ImageUtility;
import com.cloud.NetworkCloudDrive.Utilities.UserUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import java.nio.file.Path;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private UserSession userSession;
    @Mock
    private SQLiteDAO sqLiteDAO;
    @Mock
    private UserUtility userUtility;
    @Mock
    private ImageUtility imageUtility;

    private UserController controller;

    @BeforeEach
    void setUp() {
        controller = new UserController(userRepository, userSession, sqLiteDAO, userUtility, imageUtility);
    }

    @Test
    void register_NewUser_ReturnsOk() throws Exception {
        UserDTO dto = new UserDTO();
        dto.setName("newuser");
        dto.setMail("new@example.com");
        dto.setPassword("password");

        UserEntity saved = new UserEntity("newuser", "new@example.com", "encoded", UserRole.GUEST);
        saved.setId(1L);
        saved.setRegisteredAt(Instant.now());

        when(userRepository.registerUser("newuser", "new@example.com", "password")).thenReturn(saved);
        when(userUtility.createUserDirectory(saved.getId(), saved.getName(), saved.getMail())).thenReturn(Path.of("/root/userdir"));

        ResponseEntity<?> response = controller.register(dto);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        verify(imageUtility).createThumbnailDirectories(any(Path.class));
    }

    @Test
    void register_DuplicateUser_ReturnsBadRequest() throws Exception {
        UserDTO dto = new UserDTO();
        dto.setName("existing");
        dto.setMail("existing@test.com");
        dto.setPassword("password");

        when(userRepository.registerUser("existing", "existing@test.com", "password"))
                .thenThrow(new SecurityException("User already exists"));

        ResponseEntity<?> response = controller.register(dto);

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void register_DirectoryCreationFails_ReturnsBadRequest() throws Exception {
        UserDTO dto = new UserDTO();
        dto.setName("user");
        dto.setMail("user@test.com");
        dto.setPassword("pass");

        UserEntity saved = new UserEntity("user", "user@test.com", "encoded", UserRole.GUEST);
        saved.setId(1L);
        when(userRepository.registerUser("user", "user@test.com", "pass")).thenReturn(saved);
        when(userUtility.createUserDirectory(saved.getId(), saved.getName(), saved.getMail())).thenReturn(Path.of("/root/userdir"));
        doThrow(new java.io.IOException("Disk full")).when(imageUtility).createThumbnailDirectories(any(Path.class));

        ResponseEntity<?> response = controller.register(dto);

        assertEquals(400, response.getStatusCode().value());
    }

    @Test
    void info_ReturnsCurrentUser() throws Exception {
        when(userSession.getMail()).thenReturn("test@test.com");
        CurrentUserDTO dto = new CurrentUserDTO(1L, "testuser", "test@test.com", UserRole.NORMAL_USER, Instant.now());
        when(userRepository.currentUserDetails("test@test.com")).thenReturn(dto);

        ResponseEntity<?> response = controller.info();

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void updateName_ReturnsOk() throws Exception {
        UpdateUserDTO dto = new UpdateUserDTO();
        dto.setUpdate("newname");

        when(userSession.getMail()).thenReturn("test@test.com");
        UserEntity user = new UserEntity("oldname", "test@test.com", "hash", UserRole.GUEST);
        user.setId(1L);
        when(sqLiteDAO.findUserByMail("test@test.com")).thenReturn(user);
        when(userRepository.updateName(user, "newname")).thenReturn(new CurrentUserDTO(1L, "newname", "test@test.com", UserRole.GUEST));

        ResponseEntity<?> response = controller.updateName(dto);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void updateMail_ReturnsOk() throws Exception {
        UpdateUserDTO dto = new UpdateUserDTO();
        dto.setUpdate("new@test.com");

        when(userSession.getMail()).thenReturn("old@test.com");
        UserEntity user = new UserEntity("testuser", "old@test.com", "hash", UserRole.GUEST);
        user.setId(1L);
        when(sqLiteDAO.findUserByMail("old@test.com")).thenReturn(user);
        when(userRepository.updateMail(user, "new@test.com")).thenReturn(new CurrentUserDTO(1L, "testuser", "new@test.com", UserRole.GUEST));

        ResponseEntity<?> response = controller.updateMail(dto);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void updatePassword_ReturnsOk() throws Exception {
        UpdateUserDTO dto = new UpdateUserDTO();
        dto.setUpdate("newPass123");

        when(userSession.getMail()).thenReturn("test@test.com");
        UserEntity user = new UserEntity("testuser", "test@test.com", "old_hash", UserRole.GUEST);
        user.setId(1L);
        when(sqLiteDAO.findUserByMail("test@test.com")).thenReturn(user);
        when(userRepository.updatePassword(user, "newPass123")).thenReturn(new CurrentUserDTO(1L, "testuser", "test@test.com", UserRole.GUEST));

        ResponseEntity<?> response = controller.updatePassword(dto);

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void deleteUser_ReturnsOk() throws Exception {
        when(userSession.getMail()).thenReturn("test@test.com");
        UserEntity user = new UserEntity("testuser", "test@test.com", "hash", UserRole.GUEST);
        user.setId(1L);
        when(sqLiteDAO.findUserByMail("test@test.com")).thenReturn(user);
        when(userRepository.deleteUser(user)).thenReturn(null);

        ResponseEntity<?> response = controller.deleteUser();

        assertEquals(200, response.getStatusCode().value());
    }

    @Test
    void updateName_Exception_ReturnsBadRequest() throws Exception {
        UpdateUserDTO dto = new UpdateUserDTO();
        dto.setUpdate("newname");

        when(userSession.getMail()).thenReturn("test@test.com");
        when(sqLiteDAO.findUserByMail("test@test.com")).thenThrow(new RuntimeException("DB error"));

        ResponseEntity<?> response = controller.updateName(dto);

        assertEquals(400, response.getStatusCode().value());
    }
}
