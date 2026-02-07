package com.cloud.NetworkCloudDrive.Repositories.Tasks;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

@Repository
public interface ThumbnailRepository {
    String createAndSaveThumbnailDefaultSettings(Path filePath, String encodedFileName, long fileId) throws IOException, NullPointerException;
    BufferedImage createThumbnailOfAnImage(Path source, int width, int height) throws IOException;
    void deleteAllThumbnails();
    void deleteThumbnailByThumbnailID(long thumbnailId) throws SQLException;
    void deleteThumbnailByFileID(long fileId) throws SQLException;
    Resource getThumbnail(String thumbnailFilename, boolean isPortrait) throws Exception;
    }
