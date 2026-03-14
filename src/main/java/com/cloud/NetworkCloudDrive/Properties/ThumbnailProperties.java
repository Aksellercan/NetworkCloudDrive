package com.cloud.NetworkCloudDrive.Properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@ConfigurationProperties(prefix = "app.thumbnail-allowed-formats-list")
@Component
public class ThumbnailProperties {
    private List<String> allowedFormats;

    public ThumbnailProperties() {
        this.allowedFormats = List.of(
                "image/jpeg",
                "image/png",
                "image/webp",
                "image/vnd.microsoft.icon",
                "image/avif"
        );
    }

    public boolean isAllowedFormat(String Mimetype) {
        return allowedFormats.stream().anyMatch(type -> type.equalsIgnoreCase(Mimetype));
    }

    public List<String> getAllowedFormats() {
        return allowedFormats;
    }

    public void setAllowedFormats(List<String> allowedFormats) {
        this.allowedFormats = allowedFormats;
    }
}
