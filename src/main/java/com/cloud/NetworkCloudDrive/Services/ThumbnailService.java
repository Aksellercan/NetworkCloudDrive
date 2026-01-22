package com.cloud.NetworkCloudDrive.Services;

import com.cloud.NetworkCloudDrive.Repositories.ThumbnailRepository;
import com.cloud.NetworkCloudDrive.Utilities.FileUtility;
import com.cloud.NetworkCloudDrive.Utilities.UserUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
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

    public ThumbnailService(
            UserUtility userUtility,
            FileUtility fileUtility) {
        this.userUtility = userUtility;
        this.fileUtility = fileUtility;
    }

    public String saveThumbnails(BufferedImage thumbnail, String filename) throws IOException {
        boolean success = ImageIO.write(thumbnail,
                "jpg",
                Path.of(userUtility.returnUserFolder().getPath(), ".thumbnails",
                                filename + "_thumbnail." + fileUtility.getFileExtension(filename))
                        .toFile());
        if (!success)
            throw new IOException("Failed to create thumbnail for image " + filename);
        return Paths.get(userUtility.returnUserFolder().getPath()).toString();
    }

    // found on stackoverflow but its kind of slow
    @Override
    public BufferedImage createThumbnailOfAnImage(Path image, double ratio) throws IOException {
        BufferedImage source = ImageIO.read(image.toFile());
        int w = (int) (source.getWidth() * ratio);
        int h = (int) (source.getHeight() * ratio);
        BufferedImage bi = getCompatibleImage(w, h);
        Graphics2D g2d = bi.createGraphics();
        double xScale = (double) w / source.getWidth();
        double yScale = (double) h / source.getHeight();
        AffineTransform at = AffineTransform.getScaleInstance(xScale,yScale);
        g2d.drawRenderedImage(source, at);
        g2d.dispose();
        return bi;
    }

    private BufferedImage getCompatibleImage(int w, int h) {
        return
                GraphicsEnvironment.getLocalGraphicsEnvironment()
                        .getDefaultScreenDevice()
                        .getDefaultConfiguration()
                        .createCompatibleImage(w, h);
    }

    @Override
    public List<String> createThumbnailsOfImages(List<Path> images, double ratio) throws IOException {
        List<String> thumbnailStoragePath = new LinkedList<>();
        for (Path image : images) {
            thumbnailStoragePath.add(saveThumbnails(createThumbnailOfAnImage(image, ratio), image.getFileName().toString()));
        }
        return thumbnailStoragePath;
    }

    @Override
    public void deleteAllThumbnails() {}

    @Override
    public void deleteThumbnail() {}
}
