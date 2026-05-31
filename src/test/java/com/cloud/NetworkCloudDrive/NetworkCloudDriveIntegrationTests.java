package com.cloud.NetworkCloudDrive;

import com.cloud.NetworkCloudDrive.Models.Enum.UserRole;
import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import com.cloud.NetworkCloudDrive.Models.ThumbnailMetadata;
import com.cloud.NetworkCloudDrive.Models.UserEntity;
import com.cloud.NetworkCloudDrive.Persistence.SQLiteDAO;
import com.cloud.NetworkCloudDrive.Repositories.UserRepository;
import com.cloud.NetworkCloudDrive.Security.EncodingUtility;
import com.cloud.NetworkCloudDrive.Sessions.UserSession;
import com.cloud.NetworkCloudDrive.Utilities.PathUtility;
import com.cloud.NetworkCloudDrive.Utilities.UserUtility;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(locations = "classpath:/application-test.properties")
class NetworkCloudDriveIntegrationTests {

    private final Logger logger = LoggerFactory.getLogger(NetworkCloudDriveIntegrationTests.class);

    @Autowired
    EntityManager entityManager;
    @Autowired
    SQLiteDAO sqLiteDAO;
    @Autowired
    EncodingUtility encodingUtility;
    @Autowired
    UserRepository userRepository;
    @Autowired
    UserSession userSession;
    @Autowired
    PathUtility pathUtility;
    @Autowired
    UserUtility userUtility;

    @Test
    @Transactional
    void registerUser_And_Verify_Persistence() {
        UserEntity user = new UserEntity("integration_user", "integration@test.com", "password", UserRole.GUEST);
        UserEntity saved = userRepository.registerUser("integration_user", "integration@test.com", "password");

        assertNotNull(saved.getId());
        assertTrue(sqLiteDAO.checkIfUserExists("integration_user", "integration@test.com"));
    }

    @Test
    @Transactional
    void saveAndRetrieveFolder_MultipleFolders() {
        UserEntity user = new UserEntity("folder_user", "folder@test.com", "pass", UserRole.GUEST);
        UserEntity savedUser = sqLiteDAO.saveUser(user);
        userSession.setId(savedUser.getId());
        userSession.setName(savedUser.getName());
        userSession.setRole(savedUser.getRole());

        FolderMetadata folder1 = new FolderMetadata();
        sqLiteDAO.persistObjects(folder1);
        folder1.setUserid(savedUser.getId());
        folder1.setName("f1");
        folder1.setPath("0/" + folder1.getId());
        sqLiteDAO.saveFolder(folder1);

        FolderMetadata folder2 = new FolderMetadata();
        sqLiteDAO.persistObjects(folder2);
        folder2.setUserid(savedUser.getId());
        folder2.setName("f2");
        folder2.setPath("0/" + folder2.getId());
        sqLiteDAO.saveFolder(folder2);

        List<FolderMetadata> results = sqLiteDAO.findAllByIdInSQLFolderMetadata(List.of(folder1.getId(), folder2.getId()), savedUser.getId());
        assertEquals(2, results.size());
    }

    @Test
    @Transactional
    void saveFile_And_UpdateHasThumbnail() throws SQLException {
        FileMetadata file = new FileMetadata("thumb_test.txt", 0L, 0L, "text/plain", 100L);
        FileMetadata saved = sqLiteDAO.saveFile(file);
        assertNotNull(saved.getId());
        assertFalse(saved.isHasThumbnail());

        saved.setHasThumbnail(true);
        sqLiteDAO.saveFile(saved);

        FileMetadata retrieved = sqLiteDAO.findFileMetadataById(saved.getId());
        assertTrue(retrieved.isHasThumbnail());
    }

    @Test
    @Transactional
    void deleteFile_And_VerifyRemoval() throws SQLException {
        FileMetadata file = new FileMetadata("delete_me.txt", 0L, 0L, "text/plain", 50L);
        FileMetadata saved = sqLiteDAO.saveFile(file);
        long id = saved.getId();

        boolean removed = false;
        sqLiteDAO.deleteFile(saved);
        try {
            sqLiteDAO.findFileMetadataById(id);
        } catch (Exception e) {
            removed = true;
        }
        assertTrue(removed);
    }

    @Test
    @Transactional
    void deleteFolder_And_VerifyRemoval() {
        FolderMetadata folder = new FolderMetadata();
        sqLiteDAO.persistObjects(folder);
        folder.setUserid(0L);
        folder.setName("delete_folder");
        folder.setPath("0/" + folder.getId());
        FolderMetadata saved = sqLiteDAO.saveFolder(folder);
        long id = saved.getId();

        sqLiteDAO.deleteFolder(saved);
    }

    @Test
    @Transactional
    void checkDuplicateUserByMail_ReturnsTrueForExisting() {
        UserEntity user = new UserEntity("dup_mail_user", "dupmail@test.com", "pass", UserRole.GUEST);
        sqLiteDAO.saveUser(user);

        assertTrue(sqLiteDAO.checkIfUserExistsByMail("dupmail@test.com"));
    }

    @Test
    @Transactional
    void checkDuplicateUserByMail_ReturnsFalseForNonExisting() {
        assertFalse(sqLiteDAO.checkIfUserExistsByMail("nonexistent@test.com"));
    }

    @Test
    @Transactional
    void findUserByMail_ReturnsCorrectUser() {
        UserEntity user = new UserEntity("find_by_mail", "findmail@test.com", "pass", UserRole.NORMAL_USER);
        sqLiteDAO.saveUser(user);

        UserEntity found = sqLiteDAO.findUserByMail("findmail@test.com");
        assertNotNull(found);
        assertEquals("find_by_mail", found.getName());
        assertEquals(UserRole.NORMAL_USER, found.getRole());
    }

    @Test
    @Transactional
    void saveThumbnail_And_FindByFileId() throws SQLException {
        FileMetadata file = new FileMetadata("img_for_thumb.jpg", 0L, 0L, "image/jpeg", 500L);
        FileMetadata savedFile = sqLiteDAO.saveFile(file);

        ThumbnailMetadata thumb = new ThumbnailMetadata();
        thumb.setFileName(savedFile.getName());
        thumb.setFileId(savedFile.getId());
        thumb.setUserId(0L);
        thumb.setPortrait(true);
        ThumbnailMetadata savedThumb = sqLiteDAO.saveThumbnail(thumb);
        assertNotNull(savedThumb.getId());

        ThumbnailMetadata found = sqLiteDAO.queryThumbnailMetadataUsingFileId(savedFile.getId(), 0L);
        assertNotNull(found);
        assertTrue(found.isPortrait());
    }

    @Test
    @Transactional
    void getAllFilesBelongingToUser_ReturnsCorrectFiles() {
        FileMetadata f1 = new FileMetadata("user_file_1.txt", 0L, 99L, "text/plain", 10L);
        FileMetadata f2 = new FileMetadata("user_file_2.txt", 0L, 99L, "text/plain", 20L);
        sqLiteDAO.saveFile(f1);
        sqLiteDAO.saveFile(f2);

        List<FileMetadata> files = sqLiteDAO.getAllFilesBelongingToUser(99L);
        assertEquals(2, files.size());
    }
}
