package com.cloud.NetworkCloudDrive.Repositories;

import org.springframework.stereotype.Repository;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Repository
public interface ThumbnailRepository {
    BufferedImage createThumbnailOfAnImage(Path image, double ratio) throws IOException;
    List<String> createThumbnailsOfImages(List<Path> images, double ratio) throws IOException;
    void deleteAllThumbnails();
    void deleteThumbnail();
}
