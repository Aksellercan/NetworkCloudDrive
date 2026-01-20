package com.cloud.NetworkCloudDrive.Utilities;

import com.cloud.NetworkCloudDrive.Properties.FileStorageProperties;
import com.cloud.NetworkCloudDrive.Sessions.UserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class UserUtility {
    private final Logger logger = LoggerFactory.getLogger(UserUtility.class);
    private final EncodingUtility encodingUtility;
    private final FileStorageProperties fileStorageProperties;
    private final UserSession userSession;

    public UserUtility(
            EncodingUtility encodingUtility,
            FileStorageProperties fileStorageProperties,
            UserSession userSession) {
        this.encodingUtility = encodingUtility;
        this.fileStorageProperties = fileStorageProperties;
        this.userSession = userSession;
    }

    /**
     * Creates User directory upon register, encodes folder name with BASE32 made up of userID, username and mail
     * @param userId    currently logged-in user's ID
     * @param username  currently logged-in user's name
     * @param mail  currently logged-in user's MAIL
     * @return  user folder
     * @throws IOException  if there was an error while creating directory
     */
    public File createUserDirectory(long userId, String username, String mail) throws IOException {
        String encodedUserFolder = encodingUtility.encodeBase32UserFolderName(userId, username, mail);
        File userDirectory = new File(fileStorageProperties.getFullPath(encodedUserFolder));
        if (Files.notExists(userDirectory.toPath())) {
            Files.createDirectories(userDirectory.toPath());
            if (!Files.exists(userDirectory.toPath())) {
                throw new FileSystemException("Could not create user directory");
            }
        }
        return userDirectory;
    }

    /**
     * Returns user folder, if it doesn't exist creates it
     * @return  user folder
     * @throws IOException  if there was an error while creating directory
     */
    public File returnUserFolder() throws IOException {
        return createUserDirectory(userSession.getId(), userSession.getName(), userSession.getMail());
    }

    /**
     * Updates User Folder's encoding
     * @param userId    currently logged-in user's ID
     * @param username  currently logged-in user's name
     * @param mail  currently logged-in user's mail
     * @param oldBase32 old BASE32 encoding of user folder
     * @throws IOException  if there was an error while updating the folder name or the folder doesn't exist
     */
    public void updateUserDirectoryName(long userId, String username, String mail, String oldBase32) throws IOException {
        File oldPath = new File(fileStorageProperties.getFullPath(oldBase32));
        logger.debug("Old path user Path: {}", oldPath);
        if (Files.notExists(oldPath.toPath()))
            throw new FileSystemException("User directory does not exist");
        String encodedUserFolder = encodingUtility.encodeBase32UserFolderName(userId, username, mail);
        File userDirectory = new File(fileStorageProperties.getFullPath(encodedUserFolder));
        logger.debug("User Path: {}", userDirectory);
        Path updatedName = Files.move(oldPath.toPath(), userDirectory.toPath());
        if (Files.notExists(updatedName))
            throw new FileSystemException("Failed to update user directory name");
    }
}
