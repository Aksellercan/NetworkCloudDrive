package com.cloud.NetworkCloudDrive.Utilities;

import com.cloud.NetworkCloudDrive.DAO.SQLiteDAO;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import com.cloud.NetworkCloudDrive.Properties.FileStorageProperties;
import com.cloud.NetworkCloudDrive.Sessions.UserSession;
import com.cloud.NetworkCloudDrive.Utilities.Security.EncodingUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

@Component
public class PathUtility {
    private final Logger logger = LoggerFactory.getLogger(PathUtility.class);
    private final UserUtility userUtility;
    private final FileStorageProperties fileStorageProperties;
    private final SQLiteDAO sqLiteDAO;
    private final UserSession userSession;
    private final EncodingUtility encodingUtility;

    public PathUtility(
            UserUtility userUtility,
            FileStorageProperties fileStorageProperties,
            SQLiteDAO sqLiteDAO,
            UserSession userSession,
            EncodingUtility encodingUtility) {
        this.userUtility = userUtility;
        this.fileStorageProperties = fileStorageProperties;
        this.sqLiteDAO = sqLiteDAO;
        this.userSession = userSession;
        this.encodingUtility = encodingUtility;
    }

    public String getFullPathToString(String path) {
        return fileStorageProperties.getFullPath(path);
    }

    public Path getFullPath(String path) {
        return Path.of(fileStorageProperties.getFullPath(path));
    }

    public Path getBasePath() {
        return Path.of(fileStorageProperties.getBasePath());
    }

    public String getBasePathToString() {
        return fileStorageProperties.getBasePath();
    }

    /**
     * Normalizes path then checks if path starts with user folder path
     * @param path  Path to validate
     * @return  true if path is valid (starts with user folder path "./root/userfolderBase32/...") else returns false
     * @throws IOException When user folder doesn't exist or not found it will create it however will throw IOException if it can't
     */
    public boolean isPathAllowed(Path path) throws IOException {
        return Paths.get(".", path.normalize().toString()).startsWith(userUtility.returnUserFolderasPath());
    }

    public boolean isFilenameAllowed(String filename) {
        if (filename == null || filename.isEmpty()
                || filename.contains("..")
                || filename.contains("/")
                || filename.contains("\\")) {
            return false;
        }
        return !filename.startsWith(".");
    }

    /**
     * Return path of parent folder from current Folder ID
     *
     * @param folderId current Folder ID
     * @return parent folder's path
     * @throws SQLException        if Folder ID can't be found or invalid
     * @throws FileSystemException if path is invalid
     */
    public String returnParentFolderPathFromFolderID(long folderId) throws SQLException, FileSystemException {
        String[] splitPath = sqLiteDAO.queryFolderMetadata(folderId, userSession.getId()).getPath().split("/");
        long parentFolderId = Long.parseLong(splitPath[splitPath.length - 2]);
        return getFolderPath(parentFolderId);
    }

    /**
     * Returns User folder or path to folder using folderId
     *
     * @param folderId get path to folder with ID passed
     * @return if 0 returns user folder path else returns path to folder with folderId
     * @throws SQLException        if folderId is not found or invalid
     * @throws FileSystemException if path can't be resolved
     */
    public String getFolderPath(long folderId) throws SQLException, FileSystemException {
        return folderId != 0
                ?
                resolvePathFromIdString(sqLiteDAO.queryFolderMetadata(folderId, userSession.getId()).getPath())
                :
                encodingUtility.encodeBase32UserFolderName(userSession.getId(), userSession.getName(), userSession.getMail());
    }

    /**
     * Returns Folder Metadata that matches target ID
     * @param list  list to loop
     * @param targetId  target ID of Folder Metadata to return
     * @return  Folder Metadata that matches target ID
     */
    private FolderMetadata getFolderMetadataByIdFromList(List<FolderMetadata> list, long targetId) {
        return list.stream().filter(metadata -> metadata.getId() == targetId).toList().get(0);
    }

    /**
     * Resolves folder path from ID path to system path. Ex. turns 0/1/2 into username/folder1/folder2
     * @param idString  ID Path of the folder
     * @return  full system path of folder
     * @throws FileSystemException  if the path is invalid or the database is out of sync
     */
    public String resolvePathFromIdString(String idString) throws FileSystemException {
        String[] splitLine = idString.split("/");
        List<Long> idList = new ArrayList<>();
        for (String idAsString : splitLine) {
            idList.add(Long.parseLong(idAsString));
        }
        return appendFolderNames(idList);
    }

    /**
     * Appends folder names from List of folder ID's
     * @param folderIdList  List of folder ID's
     * @return  system path
     * @throws FileSystemException  if no match found for one of the ID's in list
     */
    protected String appendFolderNames(List<Long> folderIdList) throws FileSystemException {
        StringBuilder fullPath = new StringBuilder();
        List<FolderMetadata> folderMetadataListById = sqLiteDAO.findAllByIdInSQLFolderMetadata(folderIdList, userSession.getId());
        logger.debug("size {}", folderMetadataListById.size());
        for (int i = 0; i < folderIdList.size(); i++) {
            if (i == 0) {
                fullPath.append(encodingUtility.encodeBase32UserFolderName(userSession.getId(), userSession.getName(), userSession.getMail()))
                        .append(File.separator);
                continue;
            }
            FolderMetadata getMetadataFromList = getFolderMetadataByIdFromList(folderMetadataListById, folderIdList.get(i));
            if (getMetadataFromList == null)
                throw new FileSystemException("No match found for ID " + folderIdList.get(i));
            fullPath.append(getMetadataFromList.getName()).append(File.separator);
        }
        fullPath.setLength(fullPath.length() - 1);
        logger.debug("output {}", fullPath);
        return fullPath.toString();
    }


    /**
     * Return correct file separator (regex compliant)
     * @return  correct file separator
     */
    private String returnCorrectSeparatorRegex() {
        return System.getProperty("os.name").toLowerCase().contains("windows") ? "\\\\" : "/";
    }

    // Generate ID path from System path
    // rewrite
    // TODO can be replaced using StartsWith function in SQLiteDAO just like in moveFolders()
    public String generateIdPaths(String filePath, String startingIdPath) throws IOException {
        String[] folders =
                filePath.replaceAll(Pattern.quote(userUtility.returnUserFolder().getPath() + returnCorrectSeparatorRegex()), "")
                        .split(returnCorrectSeparatorRegex());
        StringBuilder idPath = new StringBuilder();
        // cut beginning of path before to avoid having boolean conditional
        // use replace all pattern : returnUserFolder() replace with: ""
        int depth = startingIdPath.split("/").length;
        idPath.append(startingIdPath).append("/");
        for (String folderName : folders) {
            logger.debug("FOLDER NAME -> {} DEPTH:{}", folderName, depth);
            List<FolderMetadata> folderResults = sqLiteDAO.findAllContainingSectionOfIdPathIgnoreCase(idPath.toString(), userSession.getId());
            for (FolderMetadata folderMetadata : folderResults) {
                String[] splitId = folderMetadata.getPath().split("/");
                logger.debug("ID PATH -> {} SPLIT LENGTH:{}", idPath, splitId.length);
                logger.debug("ITEM: ID: {} NAME: {} PATH: {}", folderMetadata.getId(), folderMetadata.getName(), folderMetadata.getPath());
                if ((splitId.length == depth) && (folderMetadata.getName().equals(folderName))) {
                    logger.debug("APPEND {}", folderMetadata.getId());
                    idPath.append(folderMetadata.getId()).append("/");
                    logger.debug("CURRENT STATE OF STRING: {}", idPath);
                }
            }
            depth++;
        }
        idPath.setLength(idPath.length() - 1);
        return idPath.toString();
    }

    public Path getRecycleBinPath() throws IOException {
        Path userPath = userUtility.returnUserFolderasPath();
        Path recycleBinPath = Path.of(userPath.toString(), ".recycleBin");
        if (Files.exists(recycleBinPath)) {
            return recycleBinPath;
        }
        Files.createDirectory(recycleBinPath);
        return recycleBinPath;
    }
}
