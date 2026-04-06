package com.cloud.NetworkCloudDrive.Services.Tasks;

import com.cloud.NetworkCloudDrive.DAO.SQLiteDAO;
import com.cloud.NetworkCloudDrive.Models.Data.ScanResults;
import com.cloud.NetworkCloudDrive.Models.Data.ThumbnailScanResults;
import com.cloud.NetworkCloudDrive.Models.Enum.ScanOptions;
import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.Data.ScanMetadata;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import com.cloud.NetworkCloudDrive.Models.ThumbnailMetadata;
import com.cloud.NetworkCloudDrive.Properties.ThumbnailProperties;
import com.cloud.NetworkCloudDrive.Repositories.Tasks.MaintenanceRepository;
import com.cloud.NetworkCloudDrive.Sessions.UserSession;
import com.cloud.NetworkCloudDrive.Utilities.Security.EncodingUtility;
import com.cloud.NetworkCloudDrive.Utilities.FileUtility;
import com.cloud.NetworkCloudDrive.Utilities.PathUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Predicate;

@Service
public class MaintenanceService implements MaintenanceRepository {
    private final Logger logger = LoggerFactory.getLogger(MaintenanceService.class);
    private final FileUtility fileUtility;
    private final EncodingUtility encodingUtility;
    private final SQLiteDAO sqLiteDAO;
    private final UserSession userSession;
    private final PathUtility pathUtility;
    private final ThumbnailService thumbnailService;
    private final ThumbnailProperties thumbnailProperties;
    private ScanResults scanResults;

    public MaintenanceService(
            FileUtility fileUtility,
            EncodingUtility encodingUtility,
            SQLiteDAO sqLiteDAO,
            UserSession userSession,
            PathUtility pathUtility,
            ThumbnailService thumbnailService,
            ThumbnailProperties thumbnailProperties) {
        this.fileUtility = fileUtility;
        this.encodingUtility = encodingUtility;
        this.sqLiteDAO = sqLiteDAO;
        this.userSession = userSession;
        this.pathUtility = pathUtility;
        this.thumbnailService = thumbnailService;
        this.thumbnailProperties = thumbnailProperties;
    }

    //controller for scan options
    public ScanMetadata<Object> scanOptionsController(long folderId, ScanOptions scanOptions) throws IOException, SQLException {
        Path startingDirectory = pathUtility.getFullPath(pathUtility.getFolderPath(folderId));
        ScanResults scanResults = new ScanResults();
        setScanResultsSession(scanResults);
        logger.info("Scan options {}", scanOptions);
        switch (scanOptions) {
            case NORMAL, GO_INTO_FOLDERS, CREATE_THUMBNAILS:
                scanDirectory(startingDirectory, Files::exists, true);
                break;
            case ONLY_FILES:
                scanDirectory(startingDirectory, Files::isRegularFile, false);
                break;
            case ONLY_FOLDERS:
                scanDirectory(startingDirectory, Files::isDirectory, true);
                break;
            case DONT_GO_INTO_FOLDERS:
                scanDirectory(startingDirectory, Files::exists, false);
                break;
            case ONLY_THUMBNAILS:
                return new ScanMetadata<>(recursiveThumbnailScanInvoker(folderId));
            case DONT_CREATE_THUMBNAILS:
                scanDirectory(startingDirectory, Files::exists, true, false);
                break;
        }
        logger.info(scanResults.toString());
        return new ScanMetadata<>(scanResults);
    }

    @Override
    public boolean scanDirectory(Path startingPath, Predicate<Path> filter, boolean useRecursion) {
        return scanDirectory(startingPath, filter, useRecursion, true);
    }

    private long getFolderId(File parentFolder) throws SQLException {
        return encodingUtility.isEncodedStringUserDirectory(parentFolder.getName())
                ?
                0L //root folder
                :
                encodingUtility.getFolderMetadataFromEncoding(parentFolder.getName()).getId();
    }

    @Override
    public boolean scanDirectory(Path startingPath, Predicate<Path> filter, boolean useRecursion, boolean createThumbnails) {
        try {
            logger.info("Start better scan function at {}", startingPath);
            List<Path> folders = fileUtility.getFileAndFolderPathsFromFolder(startingPath).stream().filter(filter).toList();
            for (Path files : folders) {
                logger.info("Currently on {}: {}", (Files.isRegularFile(files) ? "FILE" : "FOLDER"), files.getFileName());
                if (fileUtility.isIgnoredFile(files.getFileName().toString())) {
                    logger.info("Skip ignorable file or folder {}", files.getFileName().toString());
                    continue;
                }
                if (Files.isRegularFile(files)) {
                    scanResults.incrementDiscoveredFileCount();
                    handleFileCheck(files.toFile(), getFolderId(files.getParent().toFile()), createThumbnails);
                    continue;
                }
                if (encodingUtility.isBase32Decodable(files.getFileName().toString())) {
                    logger.info("decodable skipping");
                    if (Files.isDirectory(files)) {
                        scanResults.incrementDiscoveredFolderCount();
                        if (useRecursion) {
                            if (!scanDirectory(files, filter, useRecursion))
                                throw new RuntimeException("Failed to enter folder");
                        }
                    }
                    continue;
                }
                long folderId = getFolderId(files.getParent().toFile());
                logger.info("Enter folder handling FolderID {}", folderId);
                scanResults.incrementDiscoveredFolderCount();
                File createdFolder = handleFolderCheck(files.toFile(), folderId);
                if (useRecursion) {
                    if (!scanDirectory(createdFolder.toPath(), filter, useRecursion))
                        throw new RuntimeException("Failed to enter created folder");
                }
            }
            return true;
        } catch (Exception e) {
            logger.error("Exception occurred {}", e.getMessage());
            return false;
        }
    }

    public ThumbnailScanResults recursiveThumbnailScanInvoker(long folderId) throws SQLException {
        ThumbnailScanResults thumbnailScanResults = new ThumbnailScanResults();
        List<FolderMetadata> folderMetadataContainingPath = sqLiteDAO.findAllStartsWithIdPath(sqLiteDAO.getIdPath(folderId) + "/");
        scanAndCreateThumbnailsRecursive(folderMetadataContainingPath, 0, thumbnailScanResults);
        return thumbnailScanResults;
    }

    public boolean scanAndCreateThumbnailsRecursive(List<FolderMetadata> files, int index, ThumbnailScanResults thumbnailScanResults) {
        if (index == files.size())
            return true;

        List<FileMetadata> fileMetadataList = sqLiteDAO.findAllFilesWithoutThumbnailsInFolder(files.get(index).getId(), userSession.getId());

        for (FileMetadata fileMetadata : fileMetadataList) {
            thumbnailScanResults.incrementDiscoveredFileCount();
            boolean result = thumbnailCreationWrapper(fileMetadata);
            fileMetadata.setHasThumbnail(result);
            sqLiteDAO.saveFile(fileMetadata);
            thumbnailScanResults.incrementCreatedOrFailedThumbnailCount(result);
            logger.info("Thumbnail creation attempt: {}", result ? "success" : "failure");
        }
        return scanAndCreateThumbnailsRecursive(files, index + 1, thumbnailScanResults);
    }

    private boolean thumbnailCreationWrapper(FileMetadata fileMetadata) {
        try {
            return handleThumbnailCreation(
                    Path.of(pathUtility.resolvePathFromIdString(sqLiteDAO.getIdPath(fileMetadata.getFolderId())), fileMetadata.getName()),
                    fileMetadata.getName(),
                    fileMetadata.getId(),
                    fileMetadata.getMimiType());
        } catch (Exception e) {
            logger.error("Exception occurred {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void scanAndCreateThumbnails(long startingFolderId) {
        List<FileMetadata> fileMetadataList = sqLiteDAO.findAllFilesWithoutThumbnailsInFolder(startingFolderId, userSession.getId());
        for (int i = 0; i < fileMetadataList.size(); i++) {
            FileMetadata fileMetadata = fileMetadataList.get(i);
            boolean result = thumbnailCreationWrapper(fileMetadata);
            fileMetadata.setHasThumbnail(result);
            fileMetadataList.set(i, fileMetadata);
            logger.info("Thumbnail creation result {}", result ? "success" : "failure");
        }
        sqLiteDAO.saveAllFiles(fileMetadataList);

    }

    private ScanResults getScanResultsSession() {
        return this.scanResults;
    }

    private void setScanResultsSession(ScanResults scanResults) {
        this.scanResults = scanResults;
    }

    private void handleFileCheck(File currentFile, long folderId, boolean createThumbnails) throws IOException {
        logger.debug("Enter file handling. Current Folder ID {}", folderId);
        if (encodingUtility.isBase32Decodable(currentFile.getName())) {
            //if its base32 decodable check if its in db
            // we can also decode Base32 and get id to search by ID index could be more performant
            if (sqLiteDAO.fileMetadataByNameExists(currentFile.getName())) {
                //skip
                logger.warn("File exists {}", encodingUtility.decodedBase32SplitArray(currentFile.getName())[1]);
                return;
            }
            logger.info("File does not exist {}", currentFile.getName());
        }
        long newFileEntry = createNewFileEntry(currentFile, folderId, createThumbnails);
        logger.info("New file entry ID {}", newFileEntry);
        getScanResultsSession().incrementCreatedFileCount();
    }

    private long createNewFileEntry(File currentFile, long folderId, boolean createThumbnails) throws IOException {
        logger.info("-> FILE {}", currentFile.getPath());
        FileMetadata metadata =
                new FileMetadata(
                        currentFile.getName(),
                        folderId,
                        userSession.getId(),
                        fileUtility.getMimeTypeFromExtensionUsingTikaCore(currentFile),
                        currentFile.getTotalSpace());
        sqLiteDAO.persistObjects(metadata);
        // Encode in BASE32
        String encodedFileName = encodingUtility.encodeBase32FileName(metadata.getId(), currentFile.getName(), userSession.getId());
        metadata.setName(encodedFileName);
        metadata.setHasThumbnail(
                createThumbnails && handleThumbnailCreation(
                        pathUtility.getBasePath().relativize(Path.of(currentFile.getPath())),
                        encodedFileName,
                        metadata.getId(),
                        metadata.getMimiType())
                );
        logger.info("setup {}", metadata);
        Files.move(currentFile.toPath(), Path.of(currentFile.getParentFile().getPath() + File.separator + metadata.getName()));
        sqLiteDAO.saveFile(metadata);
        return metadata.getId();
    }

    private File handleFolderCheck(File currentFolder, long currentFolderId) throws SQLException, IOException {
        FolderMetadata createdFolder = new FolderMetadata();
        sqLiteDAO.persistObjects(createdFolder);
        createdFolder.setPath(sqLiteDAO.getIdPath(currentFolderId) + "/" + createdFolder.getId());
        createdFolder.setUserid(userSession.getId());
        createdFolder.setName(encodingUtility.encodeBase32FolderName(createdFolder.getId(), currentFolder.getName(), userSession.getId()));
        sqLiteDAO.saveFolder(createdFolder);
        // changing in loop causes it to fail but rerunning scan makes it work
        //NO LONGER THE CASE
        logger.info("mutated path {}", Path.of(currentFolder.getParentFile().getPath() + File.separator + createdFolder.getName()));
        Path result = Files.move(
                currentFolder.toPath(), Path.of(currentFolder.getParentFile().getPath() + File.separator + createdFolder.getName()));
        logger.info("Created folder metadata ID {} NAME {}", createdFolder.getId(), createdFolder.getName());
        getScanResultsSession().incrementCreatedFolderCount();
        return result.toFile();
    }

    private boolean handleThumbnailCreation(Path originalFolderPath, String originalFilename, long fileId, String mimeType) {
        if (!thumbnailProperties.isAllowedImageFormat(mimeType)) {
            return false;
        }
        ThumbnailMetadata thumbnail;
        try {
            thumbnail = thumbnailService.createAndSaveThumbnailDefaultSettings(originalFolderPath, originalFilename, fileId);
            return thumbnail != null;
        } catch (IOException | NullPointerException  e) {
            logger.error("Failed to create thumbnail {}", e.getMessage());
            return false;
        }
    }
}
