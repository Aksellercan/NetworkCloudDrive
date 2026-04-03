package com.cloud.NetworkCloudDrive.Models.Data;

public class ThumbnailScanResults extends ScanResults {
    private int createdThumbnails = 0;
    private int discoveredFiles = 0;
    private int failedThumbnails = 0;

    public ThumbnailScanResults() {
    }

    public void incrementCreatedOrFailedThumbnailCount(boolean result) {
        if (result) incrementCreatedThumbnailCount();
        else incrementFailedThumbnailCount();
    }

    public void incrementDiscoveredFileCount() {
        this.discoveredFiles++;
    }

    public void incrementCreatedThumbnailCount() {
        this.createdThumbnails++;
    }

    public void incrementFailedThumbnailCount() {
        this.failedThumbnails++;
    }

    public int getCreatedThumbnails() {
        return createdThumbnails;
    }

    public void setCreatedThumbnails(int createdThumbnails) {
        this.createdThumbnails = createdThumbnails;
    }

    @Override
    public int getDiscoveredFiles() {
        return discoveredFiles;
    }

    @Override
    public void setDiscoveredFiles(int discoveredFiles) {
        this.discoveredFiles = discoveredFiles;
    }

    @Override
    public String toString() {
        return String.format("Results\nDiscovered Files: %d\nCreated thumbnails: %d\nFailed thumbnails count: %d",
                this.discoveredFiles, this.createdThumbnails, this.failedThumbnails);
    }
}
