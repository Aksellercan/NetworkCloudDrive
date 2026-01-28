package com.cloud.NetworkCloudDrive.Repositories;

import org.springframework.stereotype.Repository;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Repository
public interface ThumbnailRepository {

    BufferedImage createThumbnailOfAnImage(Path source, int width, int height) throws IOException;
    List<String> createThumbnailsOfImages(List<Path> images, int width, int height) throws IOException;
    void deleteAllThumbnails();
    void deleteThumbnail(long fileId);
}
