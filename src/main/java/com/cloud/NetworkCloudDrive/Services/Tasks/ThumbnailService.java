package com.cloud.NetworkCloudDrive.Services.Tasks;

import com.cloud.NetworkCloudDrive.DAO.SQLiteDAO;
import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.ThumbnailMetadata;
import com.cloud.NetworkCloudDrive.Repositories.Tasks.ThumbnailRepository;
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
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

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

    @Override
    public String createAndSaveThumbnailDefaultSettings(Path filePath, String encodedFileName, long fileId) throws IOException, NullPointerException {
        logger.warn("thumbnail filepath = {}", filePath);
        int[] dimensions = imageUtility.getThumbnailDimensions(filePath);
        boolean isPortrait = imageUtility.isPortrait(dimensions[0], dimensions[1]);
        Path path = saveThumbnails(createThumbnailOfAnImage(filePath, dimensions[0], dimensions[1]), encodedFileName, "jpg", isPortrait);
        ThumbnailMetadata metadata = saveThumbnailToDatabase(path.getFileName().toString(), path, fileId, isPortrait);
        logger.info("Created thumbnail entry {}", metadata.toString());
        return path.toString();
    }

    private ThumbnailMetadata saveThumbnailToDatabase(String filename, Path thumbnailPath, long fileId, boolean isPortrait) throws IOException {
        long size = Files.size(thumbnailPath);
        String mimeType = fileUtility.getMimeTypeFromExtensionUsingTikaCore(thumbnailPath.toFile());
        return sqLiteDAO.saveThumbnail(new ThumbnailMetadata(filename, userSession.getId(), mimeType, size, fileId, isPortrait));
    }

    @Override
    public BufferedImage createThumbnailOfAnImage(Path source, int width, int height) throws IOException {
        if (source == null)
            throw new IOException("Image source is null");
        return Thumbnailator.createThumbnail(Path.of(pathUtility.getBasePathToString(), source.toString()).toFile(), width, height);
    }

    private Path createThumbnailDirectories(String path, boolean isPortrait) throws IOException {
        Path thumbnailsFolder = Path.of(path, ".thumbnails");
        if (!Files.exists(thumbnailsFolder))
            Files.createDirectory(thumbnailsFolder);
        // create subfolders portrait/horizontal
        Path portraitThumbnailsFolder = Path.of(path, ".thumbnails", "portrait");
        Path horizontalThumbnailsFolder = Path.of(path, ".thumbnails", "horizontal");
        return isPortrait ? Files.createDirectory(portraitThumbnailsFolder) : Files.createDirectory(horizontalThumbnailsFolder);
    }

    private Path saveThumbnails(BufferedImage thumbnail, String filename, String format, boolean isPortrait) throws IOException {
        if (thumbnail == null)
            throw new NullPointerException("Buffered Image is null");
        Path thumbnailPath = Path.of(createThumbnailDirectories(userUtility.returnUserFolder().getPath(), isPortrait).toString(),  filename + "_thumbnail." + format);
        if (!ImageIO.write(thumbnail, format, thumbnailPath.toFile())) {
            throw new IOException("Failed to write thumbnail to destination");
        }
        return thumbnailPath;
    }

    @Override
    public void deleteAllThumbnails() {
        sqLiteDAO.deleteAllThumbnails(sqLiteDAO.findAllThumbnailsByUserID(userSession.getId()));
    }

    @Override
    public void deleteThumbnailByThumbnailID(long thumbnailId) throws SQLException {
        sqLiteDAO.deleteThumbnail(sqLiteDAO.queryThumbnailMetadata(thumbnailId, userSession.getId()));
    }

    @Override
    public void deleteThumbnailByFileID(long fileId) throws SQLException {
        sqLiteDAO.deleteThumbnail(sqLiteDAO.queryThumbnailMetadataUsingFileId(fileId, userSession.getId()));
    }

//    public Resource getFile(FileMetadata file, String path) throws Exception {
//        Path filePath = Paths.get(pathUtility.getFullPathToString(path), file.getName());
//        logger.info("file service path: {}", filePath);
//        Path normalizedRoot = pathUtility.getBasePath().normalize().toAbsolutePath();
//        if (filePath.startsWith(normalizedRoot))
//            throw new SecurityException("Unauthorized access");

    @Override
    public Resource getThumbnail(String thumbnailFilename, boolean isPortrait) throws Exception {
//        Path filePath = Paths.get(pathUtility.getFullPath(), file.getName());
        Path normalizedRoot = pathUtility.getBasePath().normalize().toAbsolutePath();
//        if (thumbnailPath.startsWith(normalizedRoot))
//            throw new SecurityException("Unauthorized access");
//        if (!Files.exists(thumbnailPath))
//            throw new IOException("File does not exist");
        return new UrlResource(normalizedRoot.toAbsolutePath().toUri());
    }
}
