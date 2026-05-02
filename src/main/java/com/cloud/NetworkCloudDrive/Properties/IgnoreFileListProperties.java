package com.cloud.NetworkCloudDrive.Properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@ConfigurationProperties(prefix = "app.file-ignore-list")
@Component
public class IgnoreFileListProperties {
    private final List<String> ignoreSystemFilesList;
    private final List<String> ignoreAPIFilesList;

    public IgnoreFileListProperties() {
        this.ignoreSystemFilesList = List.of(
                //ignore MacOS generated file
                ".DS_Store"
        );

        this.ignoreAPIFilesList = List.of(
                //ignore thumbnails folder in user dir
                ".thumbnails",
                //ignore compression folder in user dir
                ".compression"
        );
    }

    public boolean isInIgnoreSystemFilesList(String filename) {
        return ignoreSystemFilesList.stream().anyMatch(file -> file.equals(filename));
    }

    public boolean isInIgnoreAPIFilesList(String filename) {
        return ignoreAPIFilesList.stream().anyMatch(file -> file.equals(filename));
    }

    public List<String> getIgnoreAPIFilesList() {
        return ignoreAPIFilesList;
    }

    public List<String> getIgnoreFileList() {
        return ignoreSystemFilesList;
    }
}
