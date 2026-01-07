package com.cloud.NetworkCloudDrive.Services;

import com.cloud.NetworkCloudDrive.DAO.SQLiteDAO;
import com.cloud.NetworkCloudDrive.Enum.ScanOptions;
import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
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

    public MaintenanceService(
            FileUtility fileUtility,
            EncodingUtility encodingUtility,
            SQLiteDAO sqLiteDAO,
            UserSession userSession) {
        this.fileUtility = fileUtility;
        this.encodingUtility = encodingUtility;
        this.sqLiteDAO = sqLiteDAO;
        this.userSession = userSession;
    }

    public Path handleFileMetadata(File file, long folderId) throws IOException {
        logger.info("CURRENT FILE -> {}", file.getName());
        if (encodingUtility.isBase32Decodable(file.getName())) {
            //if its base32 decodable check if its in db
            // we can also decode Base32 and get id to search by ID index could be more performant
            if (sqLiteDAO.fileMetadataByNameExists(file.getName())) {
                //skip
                logger.info("File exists {}", encodingUtility.decodedBase32SplitArray(file.getName())[1]);
                return null;
            }
            logger.info("File does not exist {}", file.getName());
        }
        logger.info("-> FILE {}", file.getPath());
        FileMetadata metadata =
                new FileMetadata(
                        file.getName(),
                        //try
                        folderId,
                        userSession.getId(),
                        //BROKEN
                        fileUtility.guessMimeTypeFromExtension(file),
                        file.getTotalSpace());
        sqLiteDAO.persistObjects(metadata);
        String encodedFileName = encodingUtility.encodeBase32FileName(metadata.getId(), file.getName(), userSession.getId());
        metadata.setName(encodedFileName);
        sqLiteDAO.saveFile(metadata);
        logger.info("setup metadata id {} name {} folderid {} userid {} mimetype {} totalspace {}",
                metadata.getId(), metadata.getName(), metadata.getFolderId(), metadata.getUserid(), metadata.getMimiType(), metadata.getSize());
        return Files.move(file.toPath(), Path.of(file.getParentFile().getPath() + File.separator + metadata.getName()));
    }

    // a recursive way maybe???
    public void scanFoldersAndFiles(long startingDirectoryId, ScanOptions scanOptions) throws IOException, SQLException {
        // alternative algorithm to walk file tree
        // for maintenance features
        File startingPath = fileUtility.returnFileIfItExists(fileUtility.getFolderPath(startingDirectoryId));
        logger.info("STARTING FOLDER -> {}", startingPath);
        File lastFolder = new File("");
        List<Path> fileList = fileUtility.walkFsTree(startingPath.toPath(), false);
        for (Path orgPath : fileList) {
            File currentFile = orgPath.toFile();
            if (scanOptions == ScanOptions.ONLY_FILES) {
                scanFilesInDirectory(fileUtility.returnFilesInDirectory(orgPath, false,
                        file -> file.toFile().isFile()), startingDirectoryId);
                break;
            }
            if (currentFile.isFile() || lastFolder.equals(currentFile)) {
                continue;
            }
            if (currentFile.equals(startingPath)) {
                logger.info("Skip starting path");
                continue;
            }
            if (scanOptions == ScanOptions.DONT_GO_INTO_FOLDERS) {
                if (currentFile.getParentFile().equals(startingPath)) {
                    logger.info("Exit loop because {}", ScanOptions.DONT_GO_INTO_FOLDERS);
                    break;
                }
            }
            FolderMetadata currentFolderMetadata;
            long currentFolderId;
            if (encodingUtility.isBase32Decodable(currentFile.getName())) {
                currentFolderMetadata = sqLiteDAO.queryFolderMetadata(
                        Long.parseLong(encodingUtility.decodedBase32SplitArray(currentFile.getName())[0]), userSession.getId());
                logger.info("Found folder metadata ID {} NAME {}",
                        currentFolderMetadata.getId(), encodingUtility.decodedBase32SplitArray(currentFolderMetadata.getName())[1]);
            } else {
                if (currentFile.getParentFile().equals(startingPath)) {
                    currentFolderId = 0;
                } else {
                    currentFolderId = Long.parseLong(encodingUtility.decodedBase32SplitArray(currentFile.getParentFile().getName())[0]);
                }
                currentFolderMetadata = handleFolderScan(currentFolderId, currentFile.getName(), currentFile);
                logger.info("Created folder metadata ID {} NAME {}", currentFolderMetadata.getId(), currentFolderMetadata.getName());
                fileList = fileUtility.walkFsTree(orgPath, false); //update entries
            }
            currentFolderId = currentFolderMetadata.getId();
            logger.info("CURRENT FOLDER -> {}", orgPath);
            logger.info("CURRENT ID -> {}", currentFolderId);
            // get current folder Id
            scanFilesInDirectory(fileUtility.returnFilesInDirectory(orgPath, false,
                    file -> file.toFile().isFile()), currentFolderMetadata.getId());
            fileList = fileUtility.walkFsTree(orgPath, false);
            lastFolder = currentFile;
        }
    }

    public boolean callRecursive(long folderId, ScanOptions scanOptions) throws IOException, SQLException {
        File startingPath = fileUtility.returnFileIfItExists(fileUtility.getFolderPath(folderId));
        return recursiveImplementation(
                0,
                fileUtility.walkFsTree(startingPath.toPath(), false),
                folderId,
                startingPath.getPath(),
                scanOptions,
                new File("")
        );
    }

    public boolean recursiveImplementation(int index, List<Path> fileList, long currentDirectory, String startingPath, ScanOptions scanOptions, File lastFile) throws IOException, SQLException {
        if (index >= fileList.size()) return true;
        logger.info("index: {}", index);
        File currentFile = fileList.get(index).toFile();
        if (scanOptions == ScanOptions.ONLY_FILES) {
            scanFilesInDirectory(fileUtility.returnFilesInDirectory(fileList.get(index), false,
                    file -> file.toFile().isFile()), currentDirectory);
            return true;
        }
        if (currentFile.isFile() || lastFile.equals(currentFile)) {
            index++;
            return recursiveImplementation(index, fileList, currentDirectory, startingPath, scanOptions, lastFile);
        }
        if (currentFile.equals(startingPath)) {
            logger.info("Skip starting path");
            index++;
            return recursiveImplementation(index, fileList, currentDirectory, startingPath, scanOptions, lastFile);
        }
        if (scanOptions == ScanOptions.DONT_GO_INTO_FOLDERS) {
            if (currentFile.getParentFile().equals(startingPath)) {
                logger.info("Exit loop because {}", ScanOptions.DONT_GO_INTO_FOLDERS);
                return true;
            }
        }
        FolderMetadata currentFolderMetadata;
        long currentFolderId;
        if (encodingUtility.isBase32Decodable(currentFile.getName())) {
            currentFolderMetadata = sqLiteDAO.queryFolderMetadata(
                    Long.parseLong(encodingUtility.decodedBase32SplitArray(currentFile.getName())[0]), userSession.getId());
            logger.info("Found folder metadata ID {} NAME {}",
                    currentFolderMetadata.getId(), encodingUtility.decodedBase32SplitArray(currentFolderMetadata.getName())[1]);
        } else {
            if (currentFile.getParentFile().equals(new File(startingPath))) {
                currentFolderId = 0;
            } else {
                currentFolderId = Long.parseLong(encodingUtility.decodedBase32SplitArray(currentFile.getParentFile().getName())[0]);
            }
            currentFolderMetadata = handleFolderScan(currentFolderId, currentFile.getName(), currentFile);
            logger.info("Created folder metadata ID {} NAME {}", currentFolderMetadata.getId(), currentFolderMetadata.getName());
            fileList = fileUtility.walkFsTree(fileList.get(index), false); //update entries
        }
        currentFolderId = currentFolderMetadata.getId();
        logger.info("CURRENT FOLDER -> {}", fileList.get(index));
        logger.info("CURRENT ID -> {}", currentFolderId);
        // get current folder Id
        scanFilesInDirectory(fileUtility.returnFilesInDirectory(fileList.get(index), false,
                file -> file.toFile().isFile()), currentFolderMetadata.getId());
        index++;
        return recursiveImplementation(index, fileUtility.walkFsTree(fileList.get(index), false)
                , currentDirectory, startingPath, scanOptions, currentFile);
    }

    public void scanFilesInDirectory(List<Path> currentDir, long folderId) throws IOException {
        for (Path paths : currentDir) {
            boolean filenameIsBase32Encoded = false;
            File currentFile = paths.toFile();
            logger.info("CURRENT FILE -> {}", currentFile.getName());
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
                            //BROKEN
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
        logger.info("mutated path {}", Path.of(Path.of(currentFolder.getPath()).getParent() + File.separator + createdFolder.getName()));
        Files.move(
                currentFolder.toPath(), Path.of(Path.of(currentFolder.getPath()).getParent() + File.separator + createdFolder.getName()));
        return createdFolder;
    }
}
