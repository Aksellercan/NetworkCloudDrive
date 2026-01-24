package com.cloud.NetworkCloudDrive;

import com.cloud.NetworkCloudDrive.Models.Enum.UserRole;
import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import com.cloud.NetworkCloudDrive.Models.UserEntity;
import com.cloud.NetworkCloudDrive.DAO.SQLiteDAO;
import com.cloud.NetworkCloudDrive.Services.UserService;
import com.cloud.NetworkCloudDrive.Sessions.UserSession;
import com.cloud.NetworkCloudDrive.Utilities.EncodingUtility;
import com.cloud.NetworkCloudDrive.Utilities.PathUtility;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.nio.file.FileSystemException;
import java.sql.SQLException;

@SpringBootTest
@TestPropertySource(locations = "classpath:/application-test.properties")
class NetworkCloudDriveApplicationTests {
    private final Logger logger = LoggerFactory.getLogger(NetworkCloudDriveApplicationTests.class);
    @Autowired
    EntityManager entityManager;
    @Autowired
    SQLiteDAO sqLiteDAO;
    @Autowired
    EncodingUtility encodingUtility;
    @Autowired
    UserService userService;
    @Autowired
    UserSession userSession;
    @Autowired
    private PathUtility pathUtility;

    @Test
    void contextLoads() {
        logger.info("Operating System: {}", System.getProperty("os.name"));
    }

    public void setUserSession(UserEntity userEntity) {
        userSession.setName(userEntity.getName());
        userSession.setId(userEntity.getId());
        userSession.setRole(userEntity.getRole());
    }

    public String encodeUserDirectory(long id, String name, String mail) {
        return encodingUtility.encodeBase32UserFolderName(id, name, mail);
    }

    public UserEntity registerUserAndLogDetails(UserEntity userEntity) {
        UserEntity userEntityRegisterDetails = userService.registerUser(userEntity.getName(), userEntity.getMail(), userEntity.getPassword());
        logger.info(
            "Registered UserEntity ID {} details: name {} mail {} and password {}. Extra details: registered at {}, last login {} and role {}",
            userEntityRegisterDetails.getId(),
            userEntityRegisterDetails.getName(),
            userEntityRegisterDetails.getMail(),
            userEntityRegisterDetails.getPassword(),
            userEntityRegisterDetails.getRegisteredAt(),
            userEntityRegisterDetails.getLastLogin(),
            userEntityRegisterDetails.getRole()
        );
        return userEntityRegisterDetails;
    }

    public FolderMetadata setupFolderMetadataObject(String name, long userid) {
        FolderMetadata folderMetadata = new FolderMetadata();
        entityManager.persist(folderMetadata);
        folderMetadata.setUserid(userid);
        folderMetadata.setName(name);
        folderMetadata.setPath("0/" + folderMetadata.getId());
        logger.info("Arranged Folder Metadata: name {} ID path {} and belongs to user {}. Extra details: created at {}",
                folderMetadata.getName(), folderMetadata.getPath(), folderMetadata.getUserid(), folderMetadata.getCreatedAt());
        return folderMetadata;
    }

    public FileMetadata setupFileMetadataObject(String name, String mimeType) {
        FileMetadata fileMetadata = new FileMetadata();
        fileMetadata.setUserid(0L);
        fileMetadata.setName(name);
        fileMetadata.setMimiType(mimeType);
        fileMetadata.setFolderId(0L);
        logger.info("Arranged File Metadata: name {} inside folder ID {} and belongs to user {}. Extra details: mimetype {} and created at {}",
                fileMetadata.getName(),
                fileMetadata.getFolderId(),
                fileMetadata.getUserid(),
                fileMetadata.getMimiType(),
                fileMetadata.getCreatedAt());
        return fileMetadata;
    }

    public UserEntity setupUserObject(String name, String mail, String password, UserRole userRole) {
        UserEntity userEntity = new UserEntity(name, mail.toLowerCase(), password, userRole);
        logger.info("Arranged UserEntity details: name {} mail {} and password {}. Extra details: registered at {}, last login {} and role {}",
                userEntity.getName(),
                userEntity.getMail(),
                userEntity.getPassword(),
                userEntity.getRegisteredAt(),
                userEntity.getLastLogin(),
                userEntity.getRole()
        );
        return userEntity;
    }

    //TODO test to check if "findbyId()" is working after save tests succeed

    // JPA TESTS
    @Test
    @Transactional
    public void Folder_Metadata_Save_Return_Saved_Folder_Metadata() {
        // Arrange
        FolderMetadata folderMetadata = setupFolderMetadataObject("folderMetadata_test", 0L);
        // Act
        FolderMetadata savedFolderMetadata = sqLiteDAO.saveFolder(folderMetadata);

        if (savedFolderMetadata != null)
            logger.info(
                    "Saved Folder Metadata ID {}: name {} ID path {} and belongs to user {}. Extra details: created at {}",
                    savedFolderMetadata.getId(),
                    savedFolderMetadata.getName(),
                    savedFolderMetadata.getPath(),
                    savedFolderMetadata.getUserid(),
                    savedFolderMetadata.getCreatedAt()
            );

        // Assert
        Assertions.assertEquals(folderMetadata, savedFolderMetadata);
    }

    @Test
    @Transactional
    public void File_Metadata_Save_Return_Saved_File_Metadata() {
        // Arrange
        FileMetadata fileMetadata = setupFileMetadataObject("fileMetadata_test.txt", "text/plain");

        // Act
        FileMetadata savedFileMetadata = sqLiteDAO.saveFile(fileMetadata);

        if (savedFileMetadata != null)
            logger.info(
                    "Saved File Metadata ID {}: name {} inside folder ID {} and belongs to user {}. Extra details: mimetype {} and created at {}",
                    savedFileMetadata.getId(),
                    savedFileMetadata.getName(),
                    savedFileMetadata.getFolderId(),
                    savedFileMetadata.getUserid(),
                    savedFileMetadata.getMimiType(),
                    savedFileMetadata.getCreatedAt()
            );

        // Assert
        Assertions.assertEquals(fileMetadata, savedFileMetadata);
    }

    @Test
    @Transactional
    public void User_Save_Return_Saved_User() {
        // Arrange
        UserEntity userEntity = setupUserObject(
                "unit_test_username",
                "unit_test_username@test.com",
                "unhashed_password_for_unit_test",
                UserRole.GUEST
        );

        // Act
        UserEntity savedUserEntity = sqLiteDAO.saveUser(userEntity);

        if (savedUserEntity != null)
            logger.info(
                    "Saved UserEntity ID {} details: name {} mail {} and password {}. Extra details: registered at {}, last login {} and role {}",
                    savedUserEntity.getId(),
                    savedUserEntity.getName(),
                    savedUserEntity.getMail(),
                    savedUserEntity.getPassword(),
                    savedUserEntity.getRegisteredAt(),
                    savedUserEntity.getLastLogin(),
                    savedUserEntity.getRole()
            );

        // Assert
        Assertions.assertEquals(userEntity, savedUserEntity);
    }

    @Test
    @Transactional
    public void File_Utility_Reserve_Path_From_ID_Path_Returns_Path() {
        UserEntity userEntity =
                setupUserObject(
                        "userEntity1-Functions_Unit-Test",
                        "user_Unit-Test@test.com", "super_secret1234*7&", UserRole.GUEST);
        UserEntity userEntityRegisterDetails = registerUserAndLogDetails(userEntity);
        setUserSession(userEntityRegisterDetails);
        String userFolder =
                encodeUserDirectory(
                        userSession.getId(), userSession.getName(), userSession.getMail());
        String folderNameToAssert = "resolvePath_FolderMetadata";
        String filePath = "";
        try {
            // Arrange
            FolderMetadata savedFolderMetadata =
                    sqLiteDAO.saveFolder(setupFolderMetadataObject(folderNameToAssert, userEntityRegisterDetails.getId()));
            // Act
            filePath = pathUtility.resolvePathFromIdString(savedFolderMetadata.getPath());
        } catch (FileSystemException e) {
            logger.error("File_Utility_Reserve_Path_From_ID_Path_Returns_Path {}", e.getMessage());
            Assertions.fail(e.getMessage());
        }
        // Assert
        String expectedPath = userFolder + File.separator + folderNameToAssert;
        logger.info("Expected path {} what it is {}", expectedPath, filePath);
        Assertions.assertEquals(expectedPath, filePath);
    }

    @Test
    @Transactional
    public void File_Utility_generate_ID_Path_From_Path_Returns_ID_Path() {
        // Arrange
        UserEntity userEntity =
                setupUserObject(
                        "userEntity1-Functions_Unit-Test", "user_Unit-Test@test.com", "super_secret1234*7&", UserRole.GUEST);
        UserEntity userEntityRegisterDetails = registerUserAndLogDetails(userEntity);
        setUserSession(userEntityRegisterDetails);
        FolderMetadata savedFolderMetadata =
                sqLiteDAO.saveFolder(setupFolderMetadataObject("generateIdPath_FolderMetadata", userEntityRegisterDetails.getId()));
        File file = new File(encodeUserDirectory(userSession.getId(), userSession.getName(), userSession.getMail())
                + File.separator + savedFolderMetadata.getName());
        // Act
        String IdPath = null;
        try {
            IdPath = pathUtility.generateIdPaths(file.getPath(), "0");
        } catch (Exception e) {
            Assertions.fail(e.getMessage());
        }
        // Assert
        String expectedPath = "0/" + savedFolderMetadata.getId();
        logger.info("Expected Id path {} what it is {}", expectedPath, IdPath);
        Assertions.assertEquals(expectedPath, IdPath);
    }

    @Test
    @Transactional
    public void User_Service_Register_User_Returns_True() {
        // Arrange
        UserEntity userEntity =
                setupUserObject(
                        "userEntity-register_Unit-Test", "user_Unit-Test@test.com", "super_secret1234*7&", UserRole.GUEST);
        // Act
        UserEntity userEntityRegisterDetails = registerUserAndLogDetails(userEntity);
        boolean userExists = sqLiteDAO.checkIfUserExists(userEntityRegisterDetails.getName(), userEntityRegisterDetails.getMail());
        // Assert
        Assertions.assertTrue(userExists);
    }

    @Test
    @Transactional
    public void User_Service_Register_and_Login_User_Returns_True() {
        // Arrange
        UserEntity userEntity =
                setupUserObject(
                        "userEntity-login_Unit-Test", "user_Unit-Test@test.com", "super_secret1234*7&", UserRole.GUEST);
        // Act
        boolean loginStatus = false;
        try {
            UserEntity userEntityRegisterDetails = registerUserAndLogDetails(userEntity);
            if(!sqLiteDAO.checkIfUserExists(userEntityRegisterDetails.getName(), userEntityRegisterDetails.getMail())) {
                throw new SecurityException("Failed to register userEntity");
            }
            loginStatus = userService.loginUser(userEntity.getName(), userEntity.getMail(), userEntity.getPassword());
        } catch (SQLException e) {
            logger.error("User_Service_Register_and_Login_User_Returns_True {}", e.getMessage());
            Assertions.fail(e.getMessage());
        }
        // Assert
        Assertions.assertTrue(loginStatus);
    }

    @Test
    @Transactional
    public void User_Service_Register_Duplicate_User_Returns_False() {
        // Arrange
        UserEntity userEntity1 =
                setupUserObject(
                        "userEntity1-login_Unit-Test", "user_Unit-Test@test.com", "super_secret1234*7&", UserRole.GUEST);
        UserEntity userEntity2 =
                setupUserObject(
                        "userEntity2-login_Unit-Test", "user_Unit-Test@test.com", "super_secret1234*7&", UserRole.GUEST);
        // Act
        boolean success = false;
        try {
            UserEntity userEntity1RegisterDetails = registerUserAndLogDetails(userEntity1);
            UserEntity userEntity2RegisterDetails = registerUserAndLogDetails(userEntity2);

            // Assert
            sqLiteDAO.checkIfUserExists(userEntity1RegisterDetails.getName(), userEntity1RegisterDetails.getMail());
            sqLiteDAO.checkIfUserExists(userEntity2RegisterDetails.getName(), userEntity2RegisterDetails.getMail());
        } catch (Exception e) {
            logger.error("User_Service_Register_Duplicate_User_Returns_False {}", e.getMessage());
            success = true;
        }
        Assertions.assertTrue(success);
    }

    @Test
    @Transactional
    public void File_Utility_Encode_and_Decode_User_Directory_Compare_Returns_True() {
        // Arrange
        UserEntity userEntity =
                setupUserObject(
                        "userEntity1-login_Unit-Test", "user_Unit-Test@test.com", "super_secret1234*7&", UserRole.GUEST);
        UserEntity savedUserEntity = sqLiteDAO.saveUser(userEntity);
        // Act
        String encodeUserDirectory =
                encodingUtility.encodeBase32UserFolderName(savedUserEntity.getId(), savedUserEntity.getName(), savedUserEntity.getMail());
        String decodeUserDirectory = encodingUtility.decodeBase32StringNoPadding(encodeUserDirectory);
        String[] mapProps = decodeUserDirectory.split(":");
        UserEntity decodedUserDetails = new UserEntity();
        decodedUserDetails.setId(Long.parseLong(mapProps[0]));
        decodedUserDetails.setName(mapProps[1]);
        decodedUserDetails.setMail(mapProps[2]);
        // Assert
        logger.info("ID: Expected {} what it is {}", savedUserEntity.getId(), decodedUserDetails.getId());
        Assertions.assertSame(savedUserEntity.getId(), decodedUserDetails.getId());
        logger.info("NAME: Expected {} what it is {}", savedUserEntity.getName(), decodedUserDetails.getName());
        Assertions.assertEquals(savedUserEntity.getName(), decodedUserDetails.getName());
        logger.info("MAIL: Expected {} what it is {}", savedUserEntity.getMail(), decodedUserDetails.getMail());
        Assertions.assertEquals(savedUserEntity.getMail(), decodedUserDetails.getMail());
    }

    @Test
    public void Path_Utility_Validate_Path() {

    }
}
