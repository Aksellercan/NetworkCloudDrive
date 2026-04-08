package com.cloud.NetworkCloudDrive.Services;

import com.cloud.NetworkCloudDrive.DAO.SQLiteDAO;
import com.cloud.NetworkCloudDrive.Models.DTO.UploadFileMetadataDTO;
import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import com.cloud.NetworkCloudDrive.Models.ThumbnailMetadata;
import com.cloud.NetworkCloudDrive.Properties.ThumbnailProperties;
import com.cloud.NetworkCloudDrive.Repositories.Services.FileRepository;
import com.cloud.NetworkCloudDrive.Repositories.Tasks.ThumbnailRepository;
import com.cloud.NetworkCloudDrive.Sessions.UserSession;
import com.cloud.NetworkCloudDrive.Utilities.Security.EncodingUtility;
import com.cloud.NetworkCloudDrive.Utilities.FileUtility;
import com.cloud.NetworkCloudDrive.Utilities.PathUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StreamUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.*;

@Service
public class FileService implements FileRepository {
    private final SQLiteDAO sqLiteDAO;
    private final UserSession userSession;
    private final Logger logger = LoggerFactory.getLogger(FileService.class);
    private final FileUtility fileUtility;
    private final EncodingUtility encodingUtility;
    private final PathUtility pathUtility;
    private final ThumbnailProperties thumbnailProperties;
    private final ThumbnailRepository thumbnailRepository;

    public FileService(
            SQLiteDAO sqLiteDAO,
            UserSession userSession,
            FileUtility fileUtility,
            EncodingUtility encodingUtility,
            PathUtility pathUtility,
            ThumbnailProperties thumbnailProperties,
            ThumbnailRepository thumbnailRepository) {
        this.sqLiteDAO = sqLiteDAO;
        this.userSession = userSession;
        this.fileUtility = fileUtility;
        this.encodingUtility = encodingUtility;
        this.pathUtility = pathUtility;
        this.thumbnailProperties = thumbnailProperties;
        this.thumbnailRepository = thumbnailRepository;
    }

    @Override
    public Map<String ,?> uploadFiles(MultipartFile[] files, String folderPath, long folderId) throws IOException {
        List<UploadFileMetadataDTO> uploadedFiles = new ArrayList<>();
        int savedFileCount = 0;
        List<Long> thumbnailIdList = new ArrayList<>();
        List<Path> filesInside = fileUtility.getFileAndFolderPathsFromFolder(pathUtility.getFullPath(folderPath));
        // sort by size lowest to highest
        List<MultipartFile> sortedBySize = Arrays.stream(files).sorted(Comparator.comparingLong(MultipartFile::getSize)).toList();
        for (MultipartFile file : sortedBySize) {
            String fileName = file.getOriginalFilename();
            if (fileName == null) continue;
            if (!pathUtility.isFilenameAllowed(fileName)) {
                logger.warn("Invalid filename {}", fileName);
                continue;
            }
            //check for duplicates at destination
            if (fileUtility.checkDuplicate(filesInside, fileName)) {
                logger.warn("duplicate {}", fileName);
                continue;
            }
            // Construct file metadata
            FileMetadata metadata = new FileMetadata(fileName, folderId, userSession.getId(), file.getContentType(), file.getSize());
            sqLiteDAO.persistObjects(metadata);
            // Encode in BASE32
            String encodedFileName = encodingUtility.encodeBase32FileName(metadata.getId(), fileName, userSession.getId());
            Path storagePath = storeFile(file.getInputStream(), encodedFileName, folderPath);
            logger.debug("storage path {}", storagePath);
            savedFileCount++;
            metadata.setName(encodedFileName);
            if (thumbnailProperties.isAllowedImageFormat(file.getContentType())) {
                ThumbnailMetadata thumbnailMetadata = handleThumbnailCreation(storagePath, encodedFileName, metadata.getId());
                if (thumbnailMetadata != null) {
                    thumbnailIdList.add(thumbnailMetadata.getId());
                    metadata.setHasThumbnail(true);
                }
            }
            uploadedFiles.add(new UploadFileMetadataDTO(metadata));
            sqLiteDAO.saveFile(metadata);
        }
        if (savedFileCount == 0)
            throw new FileAlreadyExistsException(
                    String.format("File%s already exists at destination", (files.length > 1 ? "s" : "")));
        if (!thumbnailIdList.isEmpty())
            return Map
                    .of(
                            "uploaded_file_count", savedFileCount,
                            "files", uploadedFiles,
                            "thumbnail_id_list", thumbnailIdList);
        return Map
                .of(
                        "uploaded_file_count", savedFileCount,
                        "files", uploadedFiles);
    }

    private ThumbnailMetadata handleThumbnailCreation(Path originalFolderPath, String originalFilename, long fileId) {
        ThumbnailMetadata thumbnail;
        try {
            thumbnail = thumbnailRepository.createAndSaveThumbnailDefaultSettings(originalFolderPath, originalFilename, fileId);
        } catch (IOException | NullPointerException  e) {
            logger.error("Failed to create thumbnail {}", e.getMessage());
            return null;
        }
        return thumbnail;
    }

    public Path storeFile(InputStream inputStream, String fileName, String parentPath) throws IOException {
        Path userDirectory = pathUtility.getBasePath().resolve(Path.of(parentPath)); /* To be extended */
        Files.createDirectories(userDirectory);
        Path filePath = userDirectory.resolve(fileName);
        if (!pathUtility.isPathAllowed(filePath))
            throw new IOException("Path not allowed");
        if (!pathUtility.isFilenameAllowed(fileName))
            throw new IOException("Filename is not allowed");
        StreamUtils.copy(inputStream, Files.newOutputStream(filePath, StandardOpenOption.CREATE_NEW));
        return pathUtility.getBasePath().relativize(filePath);
    }

    @Override
    public Resource getFile(FileMetadata file, String path) throws Exception {
        Path filePath = Paths.get(pathUtility.getFullPathToString(path), file.getName());
        Path normalizedRoot = pathUtility.getBasePath().normalize().toAbsolutePath();
        if (filePath.startsWith(normalizedRoot))
            throw new SecurityException("Unauthorized access");
        if (!Files.exists(filePath))
            throw new IOException("File does not exist");
        return new UrlResource(filePath.toAbsolutePath().toUri());
    }

    @Override
    public FolderMetadata createFolder(String folderName, long folderId) throws Exception {
        // Paths
        String idPath = sqLiteDAO.getIdPath(folderId);
        String userFolder = pathUtility.getFolderPath(folderId);
        String fullPath = pathUtility.getFullPathToString(userFolder);
        if (!pathUtility.isFilenameAllowed(folderName))
            throw new SecurityException("Path is not allowed");
        // Folder metadata
        FolderMetadata createdFolder = new FolderMetadata();
        sqLiteDAO.persistObjects(createdFolder);
        String encodedFolderName = encodingUtility.encodeBase32FolderName(createdFolder.getId(), folderName, userSession.getId());
        // ensure encoded name is also a single path component
        if (encodedFolderName.contains("/") || encodedFolderName.contains("\\")) {
            throw new SecurityException("Invalid encoded folder name");
        }
        createdFolder.setPath(idPath + "/" + createdFolder.getId());
        createdFolder.setUserid(userSession.getId());
        createdFolder.setName(encodedFolderName);
        // Check if duplicate
        if (fileUtility.checkIfFileExistsDecodeNames(userFolder, folderName))
            throw new FileAlreadyExistsException(String.format("Folder with name %s already exists at this path %s.", folderName, fullPath));
        // Create directory
        Path folder = Paths.get(fullPath, encodedFolderName);
        if (!pathUtility.isPathAllowed(folder))
            throw new SecurityException("Path out of bounds");
        if (Files.notExists(Files.createDirectory(folder)))
            throw new IOException(String.format("Cannot create directory, with name %s.", folderName));
        // save and return metadata
        return sqLiteDAO.saveFolder(createdFolder);
    }
}
