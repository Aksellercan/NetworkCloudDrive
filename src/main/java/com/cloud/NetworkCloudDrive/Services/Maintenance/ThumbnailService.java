package com.cloud.NetworkCloudDrive.Services.Maintenance;

import com.cloud.NetworkCloudDrive.Models.DTO.UserDTO;
import com.cloud.NetworkCloudDrive.Models.Domain.DeletionResults;
import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.ThumbnailMetadata;
import com.cloud.NetworkCloudDrive.Persistence.SQLiteDAO;
import com.cloud.NetworkCloudDrive.Repositories.Maintenance.ThumbnailRepository;
import com.cloud.NetworkCloudDrive.Sessions.UserSession;
import com.cloud.NetworkCloudDrive.Utilities.FileUtility;
import com.cloud.NetworkCloudDrive.Utilities.ImageUtility;
import com.cloud.NetworkCloudDrive.Utilities.PathUtility;
import com.cloud.NetworkCloudDrive.Utilities.UserUtility;
import net.coobird.thumbnailator.Thumbnailator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Service
public class ThumbnailService implements ThumbnailRepository {
    private final Logger logger = LoggerFactory.getLogger(ThumbnailService.class);
    private final UserUtility userUtility;
    private final FileUtility fileUtility;
    private final SQLiteDAO sqLiteDAO;
    private final UserSession userSession;
    private final PathUtility pathUtility;
    private final ImageUtility imageUtility;

    public ThumbnailService(
            UserUtility userUtility,
            UserSession userSession,
            FileUtility fileUtility,
            SQLiteDAO sqLiteDAO,
            PathUtility pathUtility,
            ImageUtility imageUtility) {
        this.userUtility = userUtility;
        this.userSession = userSession;
        this.sqLiteDAO = sqLiteDAO;
        this.fileUtility = fileUtility;
        this.pathUtility = pathUtility;
        this.imageUtility = imageUtility;
    }

    @Async
    @Override
    public CompletableFuture<ThumbnailMetadata> createAndSaveThumbnail(Path filePath, String encodedFileName, long fileId, UserDTO userDTO) throws IOException {
        logger.warn("thumbnail filepath = {}", filePath);
        int[] dimensions = imageUtility.getThumbnailDimensions(filePath);
        boolean isPortrait = imageUtility.isPortrait(dimensions[0], dimensions[1]);
        Path path = saveThumbnails(createThumbnailOfAnImage(filePath, dimensions[0], dimensions[1]), encodedFileName, "jpg", isPortrait, userDTO);
        ThumbnailMetadata metadata;
        // If entry exists save transaction of creating new entry by creating thumbnail immediately
        if (alreadyExists(fileId, userDTO.getUserId())) {
            metadata = sqLiteDAO.queryThumbnailMetadataUsingFileId(fileId, userDTO.getUserId());
        } else {
            metadata = saveThumbnailToDatabaseBackgroundTask(path.getFileName().toString(), path, fileId, isPortrait, userDTO.getUserId());
        }
        logger.info("Created thumbnail entry {}", metadata.toString());
        return CompletableFuture.completedFuture(metadata);
    }

    @Override
    public CompletableFuture<ThumbnailMetadata> createAndSaveThumbnailDefaultSettings(Path filePath, String encodedFileName, long fileId) throws IOException {
        return createAndSaveThumbnail(filePath, encodedFileName, fileId, userSession.returnUserDTO());
    }

    private boolean alreadyExists(long fileId, long userId) {
        return sqLiteDAO.queryThumbnailMetadataUsingFileId(fileId, userId) != null;
    }

    private ThumbnailMetadata saveThumbnailToDatabase(String filename, Path thumbnailPath, long fileId, boolean isPortrait) throws IOException {
        long size = Files.size(thumbnailPath);
        String mimeType = fileUtility.getMimeTypeFromExtensionUsingTikaCore(thumbnailPath.toFile());
        return sqLiteDAO.saveThumbnail(new ThumbnailMetadata(filename, userSession.getId(), mimeType, size, fileId, isPortrait));
    }

    private ThumbnailMetadata saveThumbnailToDatabaseBackgroundTask(String filename, Path thumbnailPath, long fileId, boolean isPortrait, long userId) throws IOException {
        long size = Files.size(thumbnailPath);
        String mimeType = fileUtility.getMimeTypeFromExtensionUsingTikaCore(thumbnailPath.toFile());
        return sqLiteDAO.saveThumbnail(new ThumbnailMetadata(filename, userId, mimeType, size, fileId, isPortrait));
    }

    public BufferedImage createThumbnailOfAnImage(Path source, int width, int height) throws IOException {
        if (source == null)
            throw new IOException("Image source is null");
        return Thumbnailator.createThumbnail(Path.of(pathUtility.getBasePathToString(), source.toString()).toFile(), width, height);
    }

    @SuppressWarnings("SameParameterValue") //Suppress useless warning in IntelliJ
    private Path saveThumbnails(BufferedImage thumbnail, String filename, String format, boolean isPortrait) throws IOException {
        if (thumbnail == null)
            throw new NullPointerException("Buffered Image is null");
        // if thumbnails folder does not exist
        imageUtility.createThumbnailDirectories(userUtility.returnUserFolderasPath());
        Path thumbnailPath = Path.of(imageUtility.getThumbnailPath(isPortrait).toString(), filename + "_thumbnail." + format);
        logger.info("Saving thumbnail to {}", thumbnailPath);
        if (!ImageIO.write(thumbnail, format, thumbnailPath.toFile())) {
            throw new IOException("Failed to write thumbnail to destination");
        }
        return thumbnailPath;
    }

    private Path saveThumbnails(BufferedImage thumbnail, String filename, String format, boolean isPortrait, UserDTO userDTO) throws IOException {
        if (thumbnail == null)
            throw new NullPointerException("Buffered Image is null");
        // if thumbnails folder does not exist
        imageUtility.createThumbnailDirectories(userUtility.returnUserFolderAsPathBackgroundTask(userDTO.getUserId(), userDTO.getUserName(), userDTO.getUserEmail()));
        Path thumbnailPath = Path.of(imageUtility.getThumbnailPathBackgroundTask(isPortrait, userDTO.getUserId(), userDTO.getUserName(), userDTO.getUserEmail()).toString(), filename + "_thumbnail." + format);
        logger.info("Saving thumbnail to {}", thumbnailPath);
        if (!ImageIO.write(thumbnail, format, thumbnailPath.toFile())) {
            throw new IOException("Failed to write thumbnail to destination");
        }
        return thumbnailPath;
    }

    @Override
    public DeletionResults deleteAllThumbnails() throws IOException, SQLException {
        List<ThumbnailMetadata> thumbnailMetadataList = sqLiteDAO.findAllThumbnailsByUserID(userSession.getId());
        List<FileMetadata> fileMetadataList = new ArrayList<>();
        for (ThumbnailMetadata thumbnailMetadata : thumbnailMetadataList) {
            FileMetadata fileMetadata = sqLiteDAO.findFileMetadataById(thumbnailMetadata.getFileId());
            fileMetadata.setHasThumbnail(false);
            fileMetadataList.add(fileMetadata);
        }
        sqLiteDAO.saveAllFiles(fileMetadataList);
        DeletionResults deletionResults = fileUtility.deleteFolders(imageUtility.getThumbnailPath());
        sqLiteDAO.deleteAllThumbnails(thumbnailMetadataList);
        return deletionResults;
    }

    @Override
    public void deleteThumbnailByThumbnailID(long thumbnailId) throws SQLException, IOException {
        ThumbnailMetadata thumbnailMetadata = sqLiteDAO.queryThumbnailMetadata(thumbnailId, userSession.getId());
        sqLiteDAO.deleteThumbnail(thumbnailMetadata);
        deleteThumbnailFile(thumbnailMetadata.getFileName(), thumbnailMetadata.isPortrait());
        FileMetadata fileMetadata = sqLiteDAO.queryFileMetadata(thumbnailMetadata.getFileId(), userSession.getId());
        fileMetadata.setHasThumbnail(false);
        sqLiteDAO.saveFile(fileMetadata);
    }

    @Override
    public void deleteThumbnailByFileID(long fileId) throws SQLException, IOException {
        ThumbnailMetadata thumbnailMetadata = sqLiteDAO.queryThumbnailMetadataUsingFileId(fileId, userSession.getId());
        sqLiteDAO.deleteThumbnail(thumbnailMetadata);
        deleteThumbnailFile(thumbnailMetadata.getFileName(), thumbnailMetadata.isPortrait());
    }

    @Override
    public DeletionResults nuclearDeleteAllThumbnails() throws IOException {
        List<ThumbnailMetadata> thumbnailMetadataList = sqLiteDAO.findAllThumbnailsByUserID(userSession.getId());
        sqLiteDAO.deleteAllThumbnails(thumbnailMetadataList);
        DeletionResults deletionResults = fileUtility.deleteFolders(imageUtility.getThumbnailPath());
        resetHasThumbnailValue();
        return deletionResults;
    }

    @Override
    public DeletionResults deleteOnlyFromIO() throws IOException {
        List<ThumbnailMetadata> thumbnailMetadataList = sqLiteDAO.findAllThumbnailsByUserID(userSession.getId());
        sqLiteDAO.deleteAllThumbnails(thumbnailMetadataList);
        return fileUtility.deleteFolders(imageUtility.getThumbnailPath());
    }

    private void resetHasThumbnailValue() {
        List<FileMetadata> getAllFilesOfUser = sqLiteDAO.getAllFilesBelongingToUser(userSession.getId());
        List<FileMetadata> mutableList = new ArrayList<>();
        for (FileMetadata fileMetadata : getAllFilesOfUser) {
            fileMetadata.setHasThumbnail(false);
            mutableList.add(fileMetadata);
        }
        sqLiteDAO.saveAllFiles(mutableList);
    }

    @Override
    public void deleteThumbnailByFileIDAndSetThumbnailStatus(long fileId) throws SQLException, IOException {
        ThumbnailMetadata thumbnailMetadata = sqLiteDAO.queryThumbnailMetadataUsingFileId(fileId, userSession.getId());
        sqLiteDAO.deleteThumbnail(thumbnailMetadata);
        deleteThumbnailFile(thumbnailMetadata.getFileName(), thumbnailMetadata.isPortrait());
        FileMetadata fileMetadata = sqLiteDAO.queryFileMetadata(fileId, userSession.getId());
        fileMetadata.setHasThumbnail(false);
        sqLiteDAO.saveFile(fileMetadata);
    }

    private void deleteThumbnailFile(String thumbnailFilename, boolean isPortrait) throws IOException {
        Path thumbnailPath = Path.of(imageUtility.getThumbnailPath(isPortrait).toString(), thumbnailFilename);
        if (!Files.deleteIfExists(thumbnailPath))
            throw new FileNotFoundException(String.format("Thumbnail by name %s not found", thumbnailFilename));
        logger.info("Deleted thumbnail by name {}", thumbnailFilename);
    }

    @Override
    public ThumbnailMetadata getThumbnailByID(long thumbnailId) throws SQLException {
        return sqLiteDAO.queryThumbnailMetadata(thumbnailId, userSession.getId());
    }

    @Override
    public ThumbnailMetadata getThumbnailByFileID(long fileId) throws SQLException {
        return sqLiteDAO.queryThumbnailMetadataUsingFileId(fileId, userSession.getId());
    }

    @Override
    public Resource getThumbnail(String thumbnailFilename, boolean isPortrait) throws Exception {
        Path thumbnailPath = Path.of(imageUtility.getThumbnailPath(isPortrait).toString(), thumbnailFilename);
        Path normalizedRoot = pathUtility.getBasePath().normalize().toAbsolutePath();
        if (thumbnailPath.startsWith(normalizedRoot))
            throw new SecurityException("Unauthorized access");
        if (!Files.exists(thumbnailPath))
            throw new IOException("File does not exist");
        return new UrlResource(thumbnailPath.toAbsolutePath().toUri());
    }
}
