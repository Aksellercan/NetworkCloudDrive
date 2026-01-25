package com.cloud.NetworkCloudDrive.Services;

import com.cloud.NetworkCloudDrive.DAO.SQLiteDAO;
import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import com.cloud.NetworkCloudDrive.Properties.ThumbnailProperties;
import com.cloud.NetworkCloudDrive.Repositories.FileRepository;
import com.cloud.NetworkCloudDrive.Sessions.UserSession;
import com.cloud.NetworkCloudDrive.Utilities.EncodingUtility;
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
import java.io.OutputStream;
import java.nio.file.*;
import java.util.*;

@Service
public class FileService implements FileRepository {
    private final SQLiteDAO sqLiteDAO;
    private final UserSession userSession;
    private final Path rootPath;
    private final Logger logger = LoggerFactory.getLogger(FileService.class);
    private final FileUtility fileUtility;
    private final EncodingUtility encodingUtility;
    private final PathUtility pathUtility;
    private final ThumbnailProperties thumbnailProperties;
    private final ThumbnailService thumbnailService;

    public FileService(
            SQLiteDAO sqLiteDAO,
            UserSession userSession,
            FileUtility fileUtility,
            EncodingUtility encodingUtility,
            PathUtility pathUtility, ThumbnailProperties thumbnailProperties, ThumbnailService thumbnailService) {
        this.sqLiteDAO = sqLiteDAO;
        this.userSession = userSession;
        this.rootPath = pathUtility.getBasePath();
        this.fileUtility = fileUtility;
        this.encodingUtility = encodingUtility;
        this.pathUtility = pathUtility;
        this.thumbnailProperties = thumbnailProperties;
        this.thumbnailService = thumbnailService;
    }

    @Override
    public Map<String ,?> uploadFiles(MultipartFile[] files, String folderPath, long folderId) throws IOException {
        List<Path> storagePathList = new LinkedList<>();
        List<FileMetadata> uploadedFiles = new LinkedList<>();
        List<Path> filesInside = fileUtility.getFileAndFolderPathsFromFolder(pathUtility.getFullPath(folderPath));
        // sort by size lowest to highest
        List<MultipartFile> sortedBySize = Arrays.stream(files).sorted(Comparator.comparingLong(MultipartFile::getSize)).toList();
        for (MultipartFile file : sortedBySize) {
            String fileName = file.getOriginalFilename();
            if (fileName == null) continue;
            if (fileName.startsWith(".")) {
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
            try (InputStream inputStream = file.getInputStream()) {
                storagePathList.add(storeFile(inputStream, encodedFileName, folderPath));
            }
            metadata.setName(encodedFileName);
            uploadedFiles.add(metadata);
            if (thumbnailProperties.isAllowedFormat(file.getContentType())) {
                thumbnailService.saveThumbnails(thumbnailService.createThumbnailOfAnImage(storagePathList.get(storagePathList.size()-1), 100, 100), file.getOriginalFilename());
            }
        }
        if (storagePathList.isEmpty())
            throw new FileAlreadyExistsException("File(s) already exists at destination");
        return Map.of("files", sqLiteDAO.saveAllFiles(uploadedFiles), "storage_path", storagePathList);
    }

    public Path storeFile(InputStream inputStream, String fileName, String parentPath) throws IOException {
        Path userDirectory = rootPath.resolve(Path.of(parentPath)); /* To be extended */
        Files.createDirectories(userDirectory);
        Path filePath = userDirectory.resolve(fileName);
        if (!pathUtility.isPathAllowed(filePath))
            throw new IOException("Path not allowed");
        try (OutputStream outputStream = Files.newOutputStream(filePath, StandardOpenOption.CREATE_NEW)) {
            StreamUtils.copy(inputStream, outputStream);
        }
        return rootPath.relativize(filePath);
    }

    @Override
    public Resource getFile(FileMetadata file, String path) throws Exception {
        Path filePath = Paths.get(pathUtility.getFullPathToString(path), file.getName());
        logger.info("file service path: {}", filePath);
        Path normalizedRoot = rootPath.normalize().toAbsolutePath();
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
