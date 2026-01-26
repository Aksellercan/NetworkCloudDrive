package com.cloud.NetworkCloudDrive.Services;

import com.cloud.NetworkCloudDrive.DAO.SQLiteDAO;
import com.cloud.NetworkCloudDrive.Repositories.ThumbnailRepository;
import com.cloud.NetworkCloudDrive.Sessions.UserSession;
import com.cloud.NetworkCloudDrive.Utilities.FileUtility;
import com.cloud.NetworkCloudDrive.Utilities.PathUtility;
import com.cloud.NetworkCloudDrive.Utilities.UserUtility;
import net.coobird.thumbnailator.Thumbnailator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.nio.file.Files;
import java.util.LinkedList;
import java.util.List;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ThumbnailService implements ThumbnailRepository {
    private final Logger logger = LoggerFactory.getLogger(ThumbnailService.class);
    private final UserUtility userUtility;
    private final FileUtility fileUtility;
    private final SQLiteDAO sqLiteDAO;
    private final UserSession userSession;
    private final PathUtility pathUtility;

    public ThumbnailService(
            UserUtility userUtility,
            UserSession userSession,
            FileUtility fileUtility,
            SQLiteDAO sqLiteDAO,
            PathUtility pathUtility) {
        this.userUtility = userUtility;
        this.userSession = userSession;
        this.sqLiteDAO = sqLiteDAO;
        this.fileUtility = fileUtility;
        this.pathUtility = pathUtility;
    }

    public void createAndSaveThumbnailDefaultSettings(Path filePath, String encodedFileName) throws IOException {
        saveThumbnails(createThumbnailOfAnImage(filePath, 100, 100), encodedFileName);
    }

    @Override
    public BufferedImage createThumbnailOfAnImage(Path source, int width, int height) throws IOException {
        if (source == null)
            throw new IOException("Image is null");
        return Thumbnailator
                .createThumbnail(Path.of(pathUtility.getBasePathToString(), source.toString())
                        .toFile(), width, height);
    }

    public boolean isPortrait(int width, int height) {
        return height > width;
    }

    public String saveThumbnails(BufferedImage thumbnail, String filename) throws IOException {
        Path thumbnailsFolder = Path.of(userUtility.returnUserFolder().getPath(), ".thumbnails");
        if (!Files.exists(thumbnailsFolder))
            Files.createDirectory(thumbnailsFolder);
        String format = "jpg";
        boolean success = ImageIO.write(thumbnail, format, Path.of(thumbnailsFolder.toString(), filename + "_thumbnail" + format).toFile());
        if (!success)
            throw new IOException("Failed to create thumbnail for image " + filename);
        return Paths.get(userUtility.returnUserFolder().getPath()).toString();
    }

    @Override
    public List<String> createThumbnailsOfImages(List<Path> images, int width, int height) throws IOException {
        List<String> thumbnailStoragePath = new LinkedList<>();
        for (Path image : images) {
            thumbnailStoragePath.add(saveThumbnails(createThumbnailOfAnImage(image, width, height), image.getFileName().toString()));
        }
        return thumbnailStoragePath;
    }

    @Override
    public void deleteAllThumbnails() {
        sqLiteDAO.deleteAllThumbnails(sqLiteDAO.findAllThumbnailsByUserID(userSession.getId()));
    }

    @Override
    public void deleteThumbnail(long fileId) {
//        sqLiteDAO.deleteThumbnail(sqLiteDAO.);
    }
}
