package com.cloud.NetworkCloudDrive.Utilities;

import com.cloud.NetworkCloudDrive.DAO.SQLiteDAO;
import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import com.cloud.NetworkCloudDrive.Properties.FileStorageProperties;
import com.cloud.NetworkCloudDrive.Sessions.UserSession;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URLConnection;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Predicate;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class FileUtility {
    private final FileStorageProperties fileStorageProperties;
    private final SQLiteDAO sqLiteDAO;
    private final UserSession userSession;
    private final EncodingUtility encodingUtility;
    private final Logger logger = LoggerFactory.getLogger(FileUtility.class);
    private final UserUtility userUtility;

    public FileUtility(
            SQLiteDAO sqLiteDAO,
            FileStorageProperties fileStorageProperties,
            UserSession userSession,
            EncodingUtility encodingUtility, UserUtility userUtility) {
        this.fileStorageProperties = fileStorageProperties;
        this.userSession = userSession;
        this.encodingUtility = encodingUtility;
        this.sqLiteDAO = sqLiteDAO;
        this.userUtility = userUtility;
    }

    /**
     * All Paths from directory
     * @param dir   starting directory
     * @param reverse   reverse order
     * @return  List of Path's starting and including from directory
     * @throws IOException  if path is invalid or does not exist
     */
    public List<Path> walkFsTree(Path dir, boolean reverse) throws IOException {
        try (Stream<Path> fileTree = Files.walk(dir)) {
            return (reverse ? fileTree.sorted(Comparator.reverseOrder()) : fileTree).toList();
        } catch (IOException e) {
            throw new IOException("Failed to walk file tree. " + e.getMessage());
        }
    }

    public List<Path> returnFilesInDirectory(Path dir, boolean reverse, Predicate<Path> pathFilter) throws IOException {
        return walkFsTree(dir, reverse).stream().filter(pathFilter).collect(Collectors.toList());
    }

    //TODO instead of generating Id paths use startsWith from DAO and filter files by found folders id's then delete them both from db and system
    public void deleteFsTree(Path dir, String startingIdPath) throws IOException {
        logger.info("Start File Tree deletion operation");
        long errorCount = 0;
        List<Path> fileTreeStream = walkFsTree(dir, true);
        for (Path path : fileTreeStream) {
            File file = path.toFile();
            if (file.getParentFile().equals(userUtility.returnUserFolder())) {
                logger.debug("Skipped base path");
                continue;
            }
            if (!Files.exists(file.toPath())) {
                errorCount++;
                continue;
            }
            if (file.isFile()) {
                String parentFolderIdPath = generateIdPaths(file.getParentFile().getPath(), startingIdPath);
                logger.debug("generated file path: {}", parentFolderIdPath);
                FolderMetadata folderMetadata =
                        sqLiteDAO.getFolderMetadataFromIdPathAndName(parentFolderIdPath, file.getParentFile().getName(), userSession.getId());
                FileMetadata output = sqLiteDAO.getFileMetadataByFolderIdNameAndUserId(folderMetadata.getId(), file.getName(), userSession.getId());
                if (!Files.deleteIfExists(file.toPath())) {
                    errorCount++;
                    continue;
                }
                sqLiteDAO.deleteFile(sqLiteDAO.getFileMetadataByFolderIdNameAndUserId(folderMetadata.getId(), file.getName(), userSession.getId()));
                logger.debug("File metadata: name {} path {} Id {}", output.getName(), output.getFolderId(), output.getId());
                continue;
            }
            String parentFolderIdPath = generateIdPaths(file.getPath(), startingIdPath);
            logger.debug("generated folder path: {}", parentFolderIdPath);
            FolderMetadata folderMetadata = sqLiteDAO.getFolderMetadataFromIdPathAndName(parentFolderIdPath, file.getName(), userSession.getId());
            // manage folders here
            if (!Files.deleteIfExists(file.toPath())) {
                errorCount++;
                continue;
            }
            sqLiteDAO.deleteFolder(folderMetadata);
            //check if it's correct
            logger.debug("Folder metadata: name {} path {} Id {}", folderMetadata.getName(), folderMetadata.getPath(), folderMetadata.getId());
        }
        if (errorCount == 0)
            logger.info("Completed file tree deletion operation. Error count {}", errorCount);
        else
            logger.warn("Completed file tree deletion operation with some errors. Error count {}", errorCount);
    }

    /**
     * Updates List of Folder Metadata's ID paths with prefix
     * @param folderList    list of Folder Metadata
     * @param oldPrefix old prefix to replace
     * @param newPrefix new prefix to replace old prefix with
     * @return  updated Folder Metadata List
     */
    public List<FolderMetadata> updateFolderIdPaths(List<FolderMetadata> folderList, String oldPrefix, String newPrefix) {
        List<FolderMetadata> result = new ArrayList<>();
        for (FolderMetadata folderMetadata : folderList) {
            folderMetadata.setPath(folderMetadata.getPath().replaceAll(oldPrefix, newPrefix));
        }
//        folderList.forEach(folderMetadata -> folderMetadata.setName(folderMetadata.getPath().replaceAll(oldPrefix, newPrefix)));
        return result;
    }

    /**
     * Returns file if it's not a duplicate
     * @param path  file path to check
     * @return  file if it's not a duplicate
     * @throws FileNotFoundException    if file is a duplicate or not found
     */
    public File returnIfItsNotADuplicate(String path) throws FileNotFoundException {
        File checkDuplicate = new File(path);
        if (Files.exists(checkDuplicate.toPath()))
            throw new FileNotFoundException(String.format("%s with name %s already exists at %s",
                    (checkDuplicate.isFile() ? "File" : "Folder"), checkDuplicate.getName(), checkDuplicate.getPath()));
        return checkDuplicate;
    }

    /**
     * Returns if file exists at path
     * @param path  file path to check
     * @return  file if it exists
     * @throws FileNotFoundException    if file does not exist at path
     */
    public File returnFileIfItExists(String path) throws FileNotFoundException {
        File checkExists = new File(fileStorageProperties.getFullPath(path));
        if (!Files.exists(checkExists.toPath()))
            throw new FileNotFoundException(String.format("%s does not exist at path %s",
                    (checkExists.isFile() ? "File" : "Folder"),checkExists.getPath()));
        return checkExists;
    }

    /**
     * Checks if file is a duplicate
     * @param filePath  filepath to start decoding from
     * @param decodedFileName   decoded filename
     * @return  true if no match found, otherwise false
     * @throws IOException  if filepath is invalid
     */
    public boolean checkIfFileExistsDecodeNames(String filePath, String decodedFileName) throws IOException {
        return getFileAndFolderPathsFromFolder(new File(fileStorageProperties.getFullPath(filePath))).stream().
                anyMatch(file -> encodingUtility.decodedBase32SplitArray(file.toFile().getName())[1].equals(decodedFileName));
    }

    /**
     * Returns if file exists at path uses NIO instead of IO
     * @param path  file path to check
     * @return  file if it exists
     * @throws FileNotFoundException    if file does not exist at path
     */
    public Path returnPathIfItExists(String path) throws FileNotFoundException {
        Path checkExists = Path.of(fileStorageProperties.getFullPath(path));
        if (!Files.exists(checkExists))
            throw new FileNotFoundException(String.format("%s does not exist at path %s",
                    (Files.isRegularFile(checkExists) ? "File" : "Folder"),checkExists));
        return checkExists;
    }

    /**
     * Return path of parent folder from current Folder ID
     * @param folderId  current Folder ID
     * @return  parent folder's path
     * @throws SQLException if Folder ID can't be found or invalid
     * @throws FileSystemException  if path is invalid
     */
    public String returnParentFolderPathFromFolderID(long folderId) throws SQLException, FileSystemException {
        String[] splitPath = sqLiteDAO.queryFolderMetadata(folderId, userSession.getId()).getPath().split("/");
        long parentFolderId = Long.parseLong(splitPath[splitPath.length - 2]);
        return getFolderPath(parentFolderId);
    }

    /**
     * Returns User folder or path to folder using folderId
     * @param folderId  get path to folder with ID passed
     * @return  if 0 returns user folder path else returns path to folder with folderId
     * @throws SQLException if folderId is not found or invalid
     * @throws FileSystemException  if path can't be resolved
     */
    public String getFolderPath(long folderId) throws SQLException, FileSystemException {
        return folderId != 0
                ?
                resolvePathFromIdString(sqLiteDAO.queryFolderMetadata(folderId, userSession.getId()).getPath())
                :
                encodingUtility.encodeBase32UserFolderName(userSession.getId(), userSession.getName(), userSession.getMail());
    }

    /**
     * List of folders and files inside a directory
     * @param file    parent folder path to list
     * @return  List of paths for files and folders
     * @throws IOException  if path is invalid
     */
    public List<Path> getFileAndFolderPathsFromFolder(File file) throws IOException {
        List<Path> fileList;
        logger.info("full path {}", file.getPath());
        try (Stream<Path> stream = Files.list(file.toPath())) {
            fileList = stream.toList();
        }
        return fileList;
    }

    /**
     * Returns MimeType of file uses Apache Tika-Core dependency
     * @param file  File object
     * @return  MimeType of file
     * @throws IOException  If an I/O error occurs
     */
    public String getMimeTypeFromExtensionUsingTikaCore(File file) throws IOException {
        logger.debug("[TIKA-CORE] File at path absolute {}, {}", file.getPath(), file);
        return new Tika().detect(file);
    }

    /**
     * Returns file extension
     * @param fileName  filename with extension
     * @return  Empty string if no extension is found, returns extension including "."
     */
    public String getFileExtension(String fileName) {
        int i = fileName.lastIndexOf(".");
        return i > 0 ? fileName.substring(i) : "";
    }

    /**
     * Checks if filename has extension
     * @param filename  filename to examine
     * @return  true if filename has extension, otherwise false
     */
    public boolean hasFileExtension(String filename) {
        return !getFileExtension(filename).isEmpty();
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
        //HOPEFULLY generate ID path starting from '0/'
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
                    logger.debug("CURRENT STATE OF STRING: {}", idPath.toString());
                }
            }
            depth++;
        }
        idPath.setLength(idPath.length() - 1);
        return idPath.toString();
    }
}
