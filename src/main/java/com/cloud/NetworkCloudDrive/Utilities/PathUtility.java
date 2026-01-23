package com.cloud.NetworkCloudDrive.Utilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;

@Component
public class PathUtility {
    private final Logger logger = LoggerFactory.getLogger(PathUtility.class);
    private final UserUtility userUtility;

    public PathUtility(UserUtility userUtility) {
        this.userUtility = userUtility;
    }

    public boolean isPathAllowed(Path path) throws IOException {
        return path.startsWith(userUtility.returnUserFolderasPath());
    }

    public boolean filenameAllowed(String filename) {
        return !filename.startsWith(".");
    }
}
