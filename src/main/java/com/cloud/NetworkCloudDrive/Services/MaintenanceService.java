package com.cloud.NetworkCloudDrive.Services;

import com.cloud.NetworkCloudDrive.DAO.SQLiteDAO;
import com.cloud.NetworkCloudDrive.Models.DTO.ScanMetadata;
import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import com.cloud.NetworkCloudDrive.Properties.IgnoreFileListProperties;
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

@Service
public class MaintenanceService {
    private final Logger logger = LoggerFactory.getLogger(MaintenanceService.class);
    private final FileUtility fileUtility;
    private final EncodingUtility encodingUtility;
    private final SQLiteDAO sqLiteDAO;
    private final UserSession userSession;
    private final IgnoreFileListProperties ignoreFileListProperties;

    public MaintenanceService(
            FileUtility fileUtility,
            EncodingUtility encodingUtility,
            SQLiteDAO sqLiteDAO,
            UserSession userSession, IgnoreFileListProperties ignoreFileListProperties) {
        this.fileUtility = fileUtility;
        this.encodingUtility = encodingUtility;
        this.sqLiteDAO = sqLiteDAO;
        this.userSession = userSession;
        this.ignoreFileListProperties = ignoreFileListProperties;
    }

    // Global search
    public void scanFoldersAndFiles(long startingDirectoryId) throws IOException, SQLException {
        // alternative algorithm to walk file tree
        logger.info("STARTING IMPERATIVE");
        File startingPath = fileUtility.returnFileIfItExists(fileUtility.getFolderPath(startingDirectoryId));
        logger.info("STARTING FOLDER -> {}", startingPath);
        File lastFolder = new File("");
        int count = 0;
        List<Path> fileList = fileUtility.walkFsTree(startingPath.toPath(), false);
        for (int i = 0; i < fileList.size(); i++) {
            count++;
            File currentFile = fileList.get(i).toFile();
            if (currentFile.isFile() || lastFolder.equals(currentFile)) {
                logger.info("Is a file or same as last folder");
                continue;
            }
            if (ignoreFileListProperties.isInIgnoreList(currentFile.getName())) {
                logger.info("In ignore list skipping...");
                continue;
            }
            if (currentFile.equals(startingPath)) {
                logger.info("Skip starting path");
                continue;
            }
            logger.info("CURRENT FOLDER -> {}", fileList.get(i));
            ScanMetadata<FolderMetadata> progress = getCurrentFolderId(currentFile, startingPath);
            logger.info("CURRENT ID -> {}", progress.getMetadata().getId());
            if (progress.isUpdated()) {
                // get current folder Id
                fileList = fileUtility.walkFsTree(fileList.get(i), false);
            }
            scanFilesInDirectory(fileUtility.returnFilesInDirectory(fileList.get(i), false,
                    file -> file.toFile().isFile()), progress.getMetadata().getId());
            lastFolder = currentFile;
        }
        logger.info("Count {}", count);
    }

    public ScanMetadata<FolderMetadata> getCurrentFolderId(File currentFolder, File startingPath) throws SQLException, IOException {
        long currentFolderId;
        ScanMetadata<FolderMetadata> scanMetadata = new ScanMetadata<>();
        if (encodingUtility.isBase32Decodable(currentFolder.getName())) {
            scanMetadata.setMetadata(sqLiteDAO.queryFolderMetadata(
                    Long.parseLong(encodingUtility.decodedBase32SplitArray(currentFolder.getName())[0]), userSession.getId()));
            logger.info("Found folder metadata ID {} NAME {}",
                    scanMetadata.getMetadata().getId(), encodingUtility.decodedBase32SplitArray(scanMetadata.getMetadata().getName())[1]);
        } else {
            logger.info("ELSE");
            if (currentFolder.getParentFile().equals(startingPath)) {
                currentFolderId = 0;
                logger.info("ELSE 1");
            } else {
                logger.info("ELSE 2");
                currentFolderId = Long.parseLong(encodingUtility.decodedBase32SplitArray(currentFolder.getParentFile().getName())[0]);
            }
            logger.info("ELSE 3");
            scanMetadata.setMetadata(handleFolderScan(currentFolderId, currentFolder.getName(), currentFolder));
            scanMetadata.setUpdated(true);
        }
        logger.info("Metadata name {} is updated? {}", scanMetadata.getMetadata().getName(), scanMetadata.isUpdated());
        return scanMetadata;
    }

    public void scanFilesInDirectory(List<Path> currentDir, long folderId) throws IOException {
        for (Path paths : currentDir) {
            boolean filenameIsBase32Encoded = false;
            File currentFile = paths.toFile();
            logger.info("CURRENT FILE -> {}", currentFile.getName());
            if (ignoreFileListProperties.isInIgnoreList(currentFile.getName())) continue;
            if (encodingUtility.isBase32Decodable(currentFile.getName())) {
                //if its base32 decodable check if its in db
                // we can also decode Base32 and get id to search by ID index could be more performant
                if (sqLiteDAO.fileMetadataByNameExists(currentFile.getName())) {
                    //skip
                    logger.info("File exists {}", encodingUtility.decodedBase32SplitArray(currentFile.getName())[1]);
                    continue;
                }
                logger.info("File does not exist {}", currentFile.getName());
                filenameIsBase32Encoded = true;
            }
            logger.info("-> FILE {}", paths);
            FileMetadata metadata =
                    new FileMetadata(
                            currentFile.getName(),
                            folderId,
                            userSession.getId(),
                            fileUtility.useTikaCoreMimeTypeFromExtension(currentFile),
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
    }

    public FolderMetadata handleFolderScan(long currentFolderId, String folderName, File currentFolder)
            throws SQLException, IOException {
        FolderMetadata createdFolder = new FolderMetadata();
        sqLiteDAO.persistObjects(createdFolder);
        createdFolder.setPath(fileUtility.getIdPath(currentFolderId) + "/" + createdFolder.getId());
        createdFolder.setUserid(userSession.getId());
        createdFolder.setName(encodingUtility.encodeBase32FolderName(createdFolder.getId(), folderName, userSession.getId()));
        sqLiteDAO.saveFolder(createdFolder);
        // changing in loop causes it to fail but rerunning scan makes it work
        logger.info("mutated path {}", Path.of(currentFolder.getParentFile().getPath() + File.separator + createdFolder.getName()));
        Files.move(
                currentFolder.toPath(), Path.of(currentFolder.getParentFile().getPath() + File.separator + createdFolder.getName()));
        logger.info("Created folder metadata ID {} NAME {}", createdFolder.getId(), createdFolder.getName());
        return createdFolder;
    }

    public boolean betterScan(File startingPath) {
        try {
            logger.info("Start better scan function at {}", startingPath.getPath());
            List<Path> folders = fileUtility.getFileAndFolderPathsFromFolder(startingPath);
            for (Path files : folders) {
                File currentFolder = files.toFile();
                logger.info("currently on {}: {}", (currentFolder.isFile() ? "FILE" : "FOLDER"), currentFolder.getName());
                if (ignoreFileListProperties.isInIgnoreList(currentFolder.getName())) {
                    logger.info("Skip ignorable file or folder {}", currentFolder.getName());
                    continue;
                }
                if (currentFolder.isFile()) {
                    long folderid = (encodingUtility.isEncodedStringUserDirectory(currentFolder.getParentFile().getName()) ? 0L : fileUtility.getFolderMetadataFromEncoding(currentFolder.getParentFile().getName()).getId());
                    logger.info("enter file handling folderid {}", folderid);
                    handleFileCheck(currentFolder, folderid);
                    continue;
                }
                if (encodingUtility.isBase32Decodable(currentFolder.getName())) {
                    logger.info("decodable skip");
                    if (!currentFolder.isFile()) {
                        betterScan(currentFolder);
                    }
                    continue;
                }
                long folderid = (encodingUtility.isEncodedStringUserDirectory(currentFolder.getParentFile().getName()) ? 0L : fileUtility.getFolderMetadataFromEncoding(currentFolder.getParentFile().getName()).getId());
                logger.info("enter folder handling folderid {}", folderid);
                betterScan(handleFolderCheck(currentFolder, folderid));
            }
            return true;
        } catch (Exception e) {
            logger.error("Exception occurred {}", e.getMessage());
            return false;
        }
    }

    public void handleFileCheck(File currentFile, long folderId) throws IOException {
        boolean filenameIsBase32Encoded = false;
        if (encodingUtility.isBase32Decodable(currentFile.getName())) {
            //if its base32 decodable check if its in db
            // we can also decode Base32 and get id to search by ID index could be more performant
            if (sqLiteDAO.fileMetadataByNameExists(currentFile.getName())) {
                //skip
                logger.info("File exists {}", encodingUtility.decodedBase32SplitArray(currentFile.getName())[1]);
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
                        fileUtility.useTikaCoreMimeTypeFromExtension(currentFile),
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

    public File handleFolderCheck(File currentFolder, long currentFolderId) throws SQLException, IOException {
        FolderMetadata createdFolder = new FolderMetadata();
        sqLiteDAO.persistObjects(createdFolder);
        createdFolder.setPath(fileUtility.getIdPath(currentFolderId) + "/" + createdFolder.getId());
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
