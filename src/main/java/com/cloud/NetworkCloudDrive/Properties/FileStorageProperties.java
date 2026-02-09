package com.cloud.NetworkCloudDrive.Properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.File;

@ConfigurationProperties(prefix = "app.file-storage")
@Component
public class FileStorageProperties {
    private String basePath;

    public FileStorageProperties() {
        this.basePath = "." + File.separator + "root" + File.separator;
    }

    public String getFullPath(String username) {
        return basePath + username;
    }
    public String getBasePath() {
        return basePath;
    }
    public void setBasePath(String basePath) {
        this.basePath = basePath;
    }
}
