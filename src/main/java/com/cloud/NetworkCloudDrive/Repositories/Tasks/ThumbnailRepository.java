package com.cloud.NetworkCloudDrive.Repositories.Tasks;

import org.springframework.stereotype.Repository;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Repository
public interface ThumbnailRepository {
    String createAndSaveThumbnailDefaultSettings(Path filePath, String encodedFileName, long fileId) throws IOException, NullPointerException;
    BufferedImage createThumbnailOfAnImage(Path source, int width, int height) throws IOException;
    void deleteAllThumbnails();
    void deleteThumbnail(long fileId);
}
