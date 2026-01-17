package com.cloud.NetworkCloudDrive.Properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@ConfigurationProperties(prefix = "app.file-ignore-list")
@Component
public class IgnoreFileListProperties {
    private List<String> ignoreFileList;

    public IgnoreFileListProperties() {
        this.ignoreFileList = List.of(
                //ignore MacOS generated file
                ".DS_Store",
                //ignore thumbnails folder in user dir
                ".thumbnails",
                //ignore compression folder in user dir
                ".compression"
        );
    }

    public boolean isInIgnoreList(String filename) {
        return ignoreFileList.stream().anyMatch(file -> file.equals(filename));
    }

    public List<String> getIgnoreFileList() {
        return ignoreFileList;
    }

    public void setIgnoreFileList(List<String> ignoreFileList) {
        this.ignoreFileList = ignoreFileList;
    }
}
