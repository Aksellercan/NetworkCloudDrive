package com.cloud.NetworkCloudDrive.Properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@ConfigurationProperties(prefix = "app.thumbnail-allowed-formats-list")
@Component
public class ThumbnailProperties {
    private List<String> allowedImageFormats;
    private List<String> allowedVideoFormats;

    public ThumbnailProperties() {
        this.allowedImageFormats = List.of(
                "image/jpeg",
                "image/png",
                "image/webp",
                "image/vnd.microsoft.icon",
                "image/avif"
        );
        this.allowedVideoFormats = List.of(
                "video/webm",
                "video/mpeg",
                "video/mp4"
        );
    }

    public boolean isAllowedImageFormat(String Mimetype) {
        return allowedImageFormats.stream().anyMatch(type -> type.equalsIgnoreCase(Mimetype));
    }

    public boolean isAllowedVideoFormat(String Mimetype) {
        return allowedVideoFormats.stream().anyMatch(type -> type.equalsIgnoreCase(Mimetype));
    }

    public boolean isAllowedFormat(String Mimetype) {
        return
                allowedImageFormats.stream().anyMatch(type -> type.equalsIgnoreCase(Mimetype))
                        ||
                        allowedVideoFormats.stream().anyMatch(type -> type.equalsIgnoreCase(Mimetype));
    }

    public List<String> getAllowedVideoFormats() {
        return allowedVideoFormats;
    }

    public void setAllowedVideoFormats(List<String> allowedVideoFormats) {
        this.allowedVideoFormats = allowedVideoFormats;
    }

    public List<String> getAllowedImageFormats() {
        return allowedImageFormats;
    }

    public void setAllowedImageFormats(List<String> allowedImageFormats) {
        this.allowedImageFormats = allowedImageFormats;
    }
}
