package com.cloud.NetworkCloudDrive.Services;

import com.cloud.NetworkCloudDrive.DAO.SQLiteDAO;
import com.cloud.NetworkCloudDrive.Models.Enum.ScanOptions;
import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import com.cloud.NetworkCloudDrive.Properties.FileStorageProperties;
import com.cloud.NetworkCloudDrive.Properties.IgnoreFileListProperties;
import com.cloud.NetworkCloudDrive.Repositories.MaintenanceRepository;
import com.cloud.NetworkCloudDrive.Sessions.UserSession;
import com.cloud.NetworkCloudDrive.Utilities.EncodingUtility;
import com.cloud.NetworkCloudDrive.Utilities.FileUtility;
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
    private final IgnoreFileListProperties ignoreFileListProperties;
    private final FileStorageProperties fileStorageProperties;

    public MaintenanceService(
            FileUtility fileUtility,
            EncodingUtility encodingUtility,
            SQLiteDAO sqLiteDAO,
            UserSession userSession,
            IgnoreFileListProperties ignoreFileListProperties,
            FileStorageProperties fileStorageProperties) {
        this.fileUtility = fileUtility;
        this.encodingUtility = encodingUtility;
        this.sqLiteDAO = sqLiteDAO;
        this.userSession = userSession;
        this.ignoreFileListProperties = ignoreFileListProperties;
        this.fileStorageProperties = fileStorageProperties;
    }

    //controller for scan options
    public void scanOptionsController(long folderId, ScanOptions scanOptions) throws IOException, SQLException {
        File startingDirectory = new File(fileStorageProperties.getFullPath(fileUtility.getFolderPath(folderId)));
        logger.info("Scan options {}", scanOptions);
        switch (scanOptions) {
            case NORMAL, GO_INTO_FOLDERS:
                scanDirectory(startingDirectory, path -> path.toFile().exists(), true);
                break;
            case ONLY_FILES:
                scanDirectory(startingDirectory,path -> path.toFile().isFile(), false);
                break;
            case ONLY_FOLDERS:
                scanDirectory(startingDirectory, path -> !path.toFile().isFile(), true);
                break;
            case DONT_GO_INTO_FOLDERS:
                scanDirectory(startingDirectory, path -> path.toFile().exists(), false);
                break;
        }
    }

    private long getFolderId(File parentFolder) throws SQLException {
        return (encodingUtility.isEncodedStringUserDirectory(parentFolder.getName()) ? 0L : encodingUtility.getFolderMetadataFromEncoding(parentFolder.getName()).getId());
    }

    @Override
    public boolean scanDirectory(File startingPath, Predicate<Path> filter, boolean useRecursion) {
        try {
            logger.info("Start better scan function at {}", startingPath.getPath());
            List<Path> folders = fileUtility.getFileAndFolderPathsFromFolder(startingPath).stream().filter(filter).toList();
            for (Path files : folders) {
                File currentFolder = files.toFile();
                logger.info("Currently on {}: {}", (currentFolder.isFile() ? "FILE" : "FOLDER"), currentFolder.getName());
                if (ignoreFileListProperties.isInIgnoreList(currentFolder.getName())) {
                    logger.info("Skip ignorable file or folder {}", currentFolder.getName());
                    continue;
                }
                if (currentFolder.isFile()) {
                    long folderId = getFolderId(currentFolder.getParentFile());
                    logger.debug("Enter file handling. Current Folder ID {}", folderId);
                    handleFileCheck(currentFolder, folderId);
                    continue;
                }
                if (encodingUtility.isBase32Decodable(currentFolder.getName())) {
                    logger.info("decodable skipping");
                    if (!currentFolder.isFile()) {
                        if (useRecursion) {
                            if (!scanDirectory(currentFolder, filter, useRecursion))
                                throw new RuntimeException("Failed to enter folder");
                        }
                    }
                    continue;
                }
                long folderId = getFolderId(currentFolder.getParentFile());
                logger.info("Enter folder handling FolderID {}", folderId);
                File createdFolder = handleFolderCheck(currentFolder, folderId);
                if (useRecursion) {
                    if (!scanDirectory(createdFolder, filter, useRecursion))
                        throw new RuntimeException("Failed to enter created folder");
                }
            }
            return true;
        } catch (Exception e) {
            logger.error("Exception occurred {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void handleFileCheck(File currentFile, long folderId) throws IOException {
        boolean filenameIsBase32Encoded = false;
        if (encodingUtility.isBase32Decodable(currentFile.getName())) {
            //if its base32 decodable check if its in db
            // we can also decode Base32 and get id to search by ID index could be more performant
            if (sqLiteDAO.fileMetadataByNameExists(currentFile.getName())) {
                //skip
                logger.warn("File exists {}", encodingUtility.decodedBase32SplitArray(currentFile.getName())[1]);
                return;
            }
            logger.info("File does not exist {}", currentFile.getName());
            filenameIsBase32Encoded = true;
        }
        logger.info("-> FILE {}", currentFile.getPath());
        FileMetadata metadata =
                new FileMetadata(
                        currentFile.getName(),
                        folderId,
                        userSession.getId(),
                        fileUtility.getMimeTypeFromExtensionUsingTikaCore(currentFile),
                        currentFile.getTotalSpace());
        sqLiteDAO.persistObjects(metadata);
        if (!filenameIsBase32Encoded) {
            // Encode in BASE32
            String encodedFileName = encodingUtility.encodeBase32FileName(metadata.getId(), currentFile.getName(), userSession.getId());
            metadata.setName(encodedFileName);
        }
        logger.info("setup metadata id {} name {} folderid {} userid {} mimetype {} totalspace {}",
                metadata.getId(), metadata.getName(), metadata.getFolderId(), metadata.getUserid(), metadata.getMimiType(), metadata.getSize());
        sqLiteDAO.saveFile(metadata);
        // changing in loop causes it to fail but rerunning scan makes it work
        Files.move(currentFile.toPath(), Path.of(currentFile.getParentFile().getPath() + File.separator + metadata.getName()));
    }

    @Override
    public File handleFolderCheck(File currentFolder, long currentFolderId) throws SQLException, IOException {
        FolderMetadata createdFolder = new FolderMetadata();
        sqLiteDAO.persistObjects(createdFolder);
        createdFolder.setPath(sqLiteDAO.getIdPath(currentFolderId) + "/" + createdFolder.getId());
        createdFolder.setUserid(userSession.getId());
        createdFolder.setName(encodingUtility.encodeBase32FolderName(createdFolder.getId(), currentFolder.getName(), userSession.getId()));
        sqLiteDAO.saveFolder(createdFolder);
        // changing in loop causes it to fail but rerunning scan makes it work
        logger.info("mutated path {}", Path.of(currentFolder.getParentFile().getPath() + File.separator + createdFolder.getName()));
        Path result = Files.move(
                currentFolder.toPath(), Path.of(currentFolder.getParentFile().getPath() + File.separator + createdFolder.getName()));
        logger.info("Created folder metadata ID {} NAME {}", createdFolder.getId(), createdFolder.getName());
        return result.toFile();
    }
}
