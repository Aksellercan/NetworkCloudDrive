package com.cloud.NetworkCloudDrive.Services.Maintenance;

import com.cloud.NetworkCloudDrive.Models.DTO.UserDTO;
import com.cloud.NetworkCloudDrive.Models.Domain.ScanResults;
import com.cloud.NetworkCloudDrive.Models.Domain.ThumbnailScanResults;
import com.cloud.NetworkCloudDrive.Models.Enum.ScanOptions;
import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import com.cloud.NetworkCloudDrive.Models.Jobs.Job;
import com.cloud.NetworkCloudDrive.Models.Jobs.MaintenanceJob;
import com.cloud.NetworkCloudDrive.Models.Response.ScanTaskResponse;
import com.cloud.NetworkCloudDrive.Models.ThumbnailMetadata;
import com.cloud.NetworkCloudDrive.Persistence.SQLiteDAO;
import com.cloud.NetworkCloudDrive.Properties.ThumbnailProperties;
import com.cloud.NetworkCloudDrive.Repositories.Maintenance.MaintenanceRepository;
import com.cloud.NetworkCloudDrive.Repositories.Maintenance.ThumbnailRepository;
import com.cloud.NetworkCloudDrive.Repositories.Tasks.JobExecutorInterface;
import com.cloud.NetworkCloudDrive.Security.EncodingUtility;
import com.cloud.NetworkCloudDrive.Sessions.UserSession;
import com.cloud.NetworkCloudDrive.Utilities.FileUtility;
import com.cloud.NetworkCloudDrive.Utilities.PathUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.function.Predicate;

@Service
public class MaintenanceService implements MaintenanceRepository {
    private final Logger logger = LoggerFactory.getLogger(MaintenanceService.class);
    private final FileUtility fileUtility;
    private final EncodingUtility encodingUtility;
    private final SQLiteDAO sqLiteDAO;
    private final UserSession userSession;
    private final PathUtility pathUtility;
    private final ThumbnailRepository thumbnailRepository;
    private final ThumbnailProperties thumbnailProperties;
    private final JobExecutorInterface jobExecutor;
    private ScanResults scanResults;

    public MaintenanceService(
            FileUtility fileUtility,
            EncodingUtility encodingUtility,
            SQLiteDAO sqLiteDAO,
            UserSession userSession,
            PathUtility pathUtility,
            ThumbnailRepository thumbnailRepository,
            ThumbnailProperties thumbnailProperties,
            @Lazy
            JobExecutorInterface jobExecutorInterface) {
        this.fileUtility = fileUtility;
        this.encodingUtility = encodingUtility;
        this.sqLiteDAO = sqLiteDAO;
        this.userSession = userSession;
        this.pathUtility = pathUtility;
        this.thumbnailRepository = thumbnailRepository;
        this.thumbnailProperties = thumbnailProperties;
        this.jobExecutor = jobExecutorInterface;
    }

    private Path startingDirectory(long folderId) throws FileSystemException, SQLException {
        return pathUtility.getFullPath(pathUtility.getFolderPath(folderId));
    }

    @Override
    public Object scanOptionsControllerConverter(Job job) throws SQLException {
        MaintenanceJob maintenanceJob = (MaintenanceJob) job;
        return scanOptionsController(maintenanceJob.getUserDTO(), maintenanceJob.getStartingDirectory(), maintenanceJob.getStartingFolderId(), maintenanceJob.getScanOptions());
    }

    //controller for scan options
    @Override
    public Object scanOptionsController(UserDTO userDTO, Path startingDirectory, long folderId, ScanOptions scanOptions) throws SQLException {
        ScanResults scanResults = new ScanResults();
        setScanResultsSession(scanResults);
        logger.info("Scan options {}", scanOptions);
        switch (scanOptions) {
            case NORMAL, GO_INTO_FOLDERS, CREATE_THUMBNAILS:
                scanDirectory(userDTO, startingDirectory, Files::exists, true, true);
                break;
            case ONLY_FILES:
                scanDirectory(userDTO, startingDirectory, Files::isRegularFile, false, true);
                break;
            case ONLY_FOLDERS:
                scanDirectory(userDTO, startingDirectory, Files::isDirectory, true, true);
                break;
            case DONT_GO_INTO_FOLDERS:
                scanDirectory(userDTO, startingDirectory, Files::exists, false, true);
                break;
            case ONLY_THUMBNAILS:
                return recursiveThumbnailScanInvoker(userDTO, folderId);
            case DONT_CREATE_THUMBNAILS:
                scanDirectory(userDTO, startingDirectory, Files::exists, true, false);
                break;
        }
        scanResults.stopTimerAndGetTimeTaken();
        logger.info(scanResults.toString());
        return scanResults;
    }

    @Override
    public ScanTaskResponse queueScan(long folderId, ScanOptions scanOptions) throws FileSystemException, SQLException {
        MaintenanceJob maintenanceJob = new MaintenanceJob(startingDirectory(folderId), folderId, scanOptions);
        jobExecutor.queueJobs(maintenanceJob);
        return new ScanTaskResponse(maintenanceJob.getJobName(), maintenanceJob.getId(), maintenanceJob.getAddedOn(), maintenanceJob.getJobStatus());
    }

    //    @Override
//    public boolean scanDirectory(Path startingPath, Predicate<Path> filter, boolean useRecursion) {
//        return scanDirectory(userSession.returnUserDTO(), startingPath, filter, useRecursion, true);
//    }
//
    private long getFolderId(File parentFolder, long userId) throws SQLException {
        return encodingUtility.isEncodedStringUserDirectory(parentFolder.getName())
                ?
                0L //root folder
                :
                encodingUtility.getFolderMetadataFromEncoding(parentFolder.getName(), userId).getId();
    }

    @Override
    public boolean scanDirectory(UserDTO userDTO, Path startingPath, Predicate<Path> filter, boolean useRecursion, boolean createThumbnails) {
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
                    handleFileCheck(files.toFile(), userDTO, getFolderId(files.getParent().toFile(), userDTO.getUserId()), createThumbnails);
                    continue;
                }
                if (encodingUtility.isBase32Decodable(files.getFileName().toString())) {
                    logger.info("decodable skipping");
                    if (Files.isDirectory(files)) {
                        scanResults.incrementDiscoveredFolderCount();
                        if (useRecursion) {
                            if (!scanDirectory(userDTO, files, filter, true, createThumbnails))
                                throw new RuntimeException("Failed to enter folder");
                        }
                    }
                    continue;
                }
                long folderId = getFolderId(files.getParent().toFile(), userDTO.getUserId());
                logger.info("Enter folder handling FolderID {}", folderId);
                scanResults.incrementDiscoveredFolderCount();
                File createdFolder = handleFolderCheck(files.toFile(), folderId, userDTO);
                if (useRecursion) {
                    if (!scanDirectory(userDTO, createdFolder.toPath(), filter, true, createThumbnails))
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
    public ThumbnailScanResults recursiveThumbnailScanInvoker(UserDTO userDTO, long folderId) throws SQLException {
        ThumbnailScanResults thumbnailScanResults = new ThumbnailScanResults();
        List<Long> folderMetadataContainingPath = sqLiteDAO.findAllStartsWithIdPathReturnsLongList(sqLiteDAO.getIdPath(folderId, userDTO.getUserId()), userDTO.getUserId());
        logger.info("size {}", folderMetadataContainingPath.size());
        folderMetadataContainingPath.forEach(l -> logger.info("item {}", l));
        //temp
        if (folderId <= 0) {
            folderMetadataContainingPath.add(0L);
        }
        scanAndCreateThumbnailsRecursive(userDTO, folderMetadataContainingPath, 0, thumbnailScanResults);
        thumbnailScanResults.stopTimerAndGetTimeTaken();
        return thumbnailScanResults;
    }

    public boolean scanAndCreateThumbnailsRecursive(UserDTO userDTO, List<Long> files, int index, ThumbnailScanResults thumbnailScanResults) {
        if (index == files.size())
            return true;

        List<FileMetadata> fileMetadataList = sqLiteDAO.findAllFilesWithoutThumbnailsInFolder(files.get(index), userDTO.getUserId());

        for (FileMetadata fileMetadata : fileMetadataList) {
            thumbnailScanResults.incrementDiscoveredFileCount();
            boolean result = thumbnailCreationWrapper(fileMetadata, userDTO);
            fileMetadata.setHasThumbnail(result);
            sqLiteDAO.saveFile(fileMetadata);
            thumbnailScanResults.incrementCreatedOrFailedThumbnailCount(result);
            logger.info("Thumbnail creation attempt: {}", result ? "success" : "failure");
        }
        return scanAndCreateThumbnailsRecursive(userDTO, files, index + 1, thumbnailScanResults);
    }

    private boolean thumbnailCreationWrapper(FileMetadata fileMetadata, UserDTO userDTO) {
        try {
            return handleThumbnailCreation(
                    Path.of(pathUtility.resolvePathFromIdString(userDTO, sqLiteDAO.getIdPath(fileMetadata.getFolderId(), userDTO.getUserId())), fileMetadata.getName()),
                    fileMetadata.getName(),
                    fileMetadata.getId(),
                    fileMetadata.getMimiType(), userDTO);
        } catch (Exception e) {
            logger.error("Exception occurred {}", e.getMessage());
            return false;
        }
    }

    @Override
    public void scanAndCreateThumbnails(long startingFolderId, UserDTO userDTO) {
        List<FileMetadata> fileMetadataList = sqLiteDAO.findAllFilesWithoutThumbnailsInFolder(startingFolderId, userDTO.getUserId());
        for (int i = 0; i < fileMetadataList.size(); i++) {
            FileMetadata fileMetadata = fileMetadataList.get(i);
            boolean result = thumbnailCreationWrapper(fileMetadata, userDTO);
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

    private void handleFileCheck(File currentFile, UserDTO userDTO, long folderId, boolean createThumbnails) throws IOException {
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
        long newFileEntry = createNewFileEntry(currentFile, userDTO, folderId, createThumbnails);
        logger.info("New file entry ID {}", newFileEntry);
        getScanResultsSession().incrementCreatedFileCount();
    }

    private long createNewFileEntry(File currentFile, UserDTO userDTO, long folderId, boolean createThumbnails) throws IOException {
        logger.info("-> FILE {}", currentFile.getPath());
        FileMetadata metadata =
                new FileMetadata(
                        currentFile.getName(),
                        folderId,
//                        userSession.getId(),
                        userDTO.getUserId(),
                        fileUtility.getMimeTypeFromExtensionUsingTikaCore(currentFile),
                        currentFile.getTotalSpace());
        sqLiteDAO.persistObjects(metadata);
        // Encode in BASE32
        String encodedFileName = encodingUtility.encodeBase32FileName(metadata.getId(), currentFile.getName(), userDTO.getUserId());
        metadata.setName(encodedFileName);
        metadata.setHasThumbnail(
                createThumbnails && handleThumbnailCreation(
                        pathUtility.getBasePath().relativize(Path.of(currentFile.getPath())),
                        encodedFileName,
                        metadata.getId(),
                        metadata.getMimiType(), userDTO)
        );
        logger.info("setup {}", metadata);
        Files.move(currentFile.toPath(), Path.of(currentFile.getParentFile().getPath() + File.separator + metadata.getName()));
        sqLiteDAO.saveFile(metadata);
        return metadata.getId();
    }

    private File handleFolderCheck(File currentFolder, long currentFolderId, UserDTO userDTO) throws SQLException, IOException {
        FolderMetadata createdFolder = new FolderMetadata();
        sqLiteDAO.persistObjects(createdFolder);
        createdFolder.setPath(sqLiteDAO.getIdPath(currentFolderId, userDTO.getUserId()) + "/" + createdFolder.getId());
        createdFolder.setUserid(userDTO.getUserId());
        createdFolder.setName(encodingUtility.encodeBase32FolderName(createdFolder.getId(), currentFolder.getName(), userDTO.getUserId()));
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

    private boolean handleThumbnailCreation(Path originalFolderPath, String originalFilename, long fileId, String mimeType, UserDTO userDTO) {
        if (!thumbnailProperties.isAllowedImageFormat(mimeType)) {
            return false;
        }
        ThumbnailMetadata thumbnail;
        try {
            thumbnail = thumbnailRepository.createAndSaveThumbnail(originalFolderPath, originalFilename, fileId, userDTO).get();
            return thumbnail != null;
        } catch (IOException | NullPointerException | ExecutionException | InterruptedException e) {
            logger.error("Failed to create thumbnail {}", e.getMessage());
            return false;
        }
    }
}
