package com.cloud.NetworkCloudDrive.Models.Data;

public class ThumbnailScanResults extends ScanResults {
    private int createdThumbnails = 0;
    private int failedThumbnails = 0;

    public ThumbnailScanResults() {
    }

    public void incrementCreatedOrFailedThumbnailCount(boolean result) {
        if (result) incrementCreatedThumbnailCount();
        else incrementFailedThumbnailCount();
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

    public int getFailedThumbnails() {
        return failedThumbnails;
    }

    public void setFailedThumbnails(int failedThumbnails) {
        this.failedThumbnails = failedThumbnails;
    }

    @Override
    public String toString() {
        return String.format("Results\nDiscovered Files: %d\nCreated thumbnails: %d\nFailed thumbnails count: %d\nTime taken: %d",
                getDiscoveredFiles(), this.createdThumbnails, this.failedThumbnails, this.getTimeTaken());
    }
}
