package com.cloud.NetworkCloudDrive.Utilities;

import com.cloud.NetworkCloudDrive.Properties.FileStorageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.nio.file.Path;

@Component
public class PathUtility {
    private final FileStorageProperties fileStorageProperties;
    private final Logger logger = LoggerFactory.getLogger(PathUtility.class);

    public PathUtility(FileStorageProperties fileStorageProperties) {
        this.fileStorageProperties = fileStorageProperties;
    }

    public boolean isPathAllowed(Path path) {
        Path resolvePath = Path.of(fileStorageProperties.getBasePath()).resolve(path).normalize().toAbsolutePath();
        logger.error("resolved Path: {}", resolvePath);
        return true;
    }
}
