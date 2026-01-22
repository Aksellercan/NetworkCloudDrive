package com.cloud.NetworkCloudDrive.Repositories;

import org.springframework.stereotype.Repository;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;

@Repository
public interface ThumbnailRepository {
    BufferedImage createThumbnailOfAnImage(Path image, double ratio) throws IOException;
    void createThumbnailsOfImages();
    void deleteAllThumbnails();
    void deleteThumbnail();
}
