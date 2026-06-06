package com.cloud.NetworkCloudDrive.Jobs;

import com.cloud.NetworkCloudDrive.Models.ThumbnailMetadata;
import com.cloud.NetworkCloudDrive.Repositories.Maintenance.ThumbnailRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.ExecutionException;

@Component
public class ThumbnailServiceJob {
    private final ThumbnailRepository thumbnailRepository;
    private final Logger logger = LoggerFactory.getLogger(ThumbnailServiceJob.class);

    public ThumbnailServiceJob(ThumbnailRepository thumbnailRepository) {
        this.thumbnailRepository = thumbnailRepository;
    }

    public ThumbnailMetadata handleThumbnailCreation(Path originalFolderPath, String originalFilename, long fileId) {
        ThumbnailMetadata thumbnail;
        try {
            thumbnail = thumbnailRepository.createAndSaveThumbnailDefaultSettings(originalFolderPath, originalFilename, fileId).get();
        } catch (IOException | NullPointerException | ExecutionException | InterruptedException e) {
            logger.error("Failed to create thumbnail {}", e.getMessage());
            return null;
        }
        return thumbnail;
    }
}
