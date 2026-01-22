package com.cloud.NetworkCloudDrive.Services;

import com.cloud.NetworkCloudDrive.Repositories.ThumbnailRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

@Service
public class ThumbnailService implements ThumbnailRepository {
    private final Logger logger = LoggerFactory.getLogger(ThumbnailService.class);

    public ThumbnailService() {
    }

    @Override
    public void createThumbnailOfAnImage(Path image) throws IOException {
        BufferedImage img = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        img.createGraphics().drawImage(ImageIO.read(image.toFile()).getScaledInstance(100, 100, BufferedImage.SCALE_SMOOTH),0,0,null);
        ImageIO.write(img, "jpg", Path.of(".thumbnails", "thumbnail.jpg").toFile());
    }

    @Override
    public void createThumbnailsOfImages() {}

    @Override
    public void deleteAllThumbnails() {}

    @Override
    public void deleteThumbnail() {}
}
