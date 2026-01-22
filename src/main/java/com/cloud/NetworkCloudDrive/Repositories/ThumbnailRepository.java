package com.cloud.NetworkCloudDrive.Repositories;

import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Path;

@Repository
public interface ThumbnailRepository {
    void createThumbnailOfAnImage(Path image) throws IOException;
    void createThumbnailsOfImages();
    void deleteAllThumbnails();
    void deleteThumbnail();
}
