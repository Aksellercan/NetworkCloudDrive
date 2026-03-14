package com.cloud.NetworkCloudDrive.Models.Data;

public class ScanResults {
    private int discoveredFiles = 0;
    private int discoveredFolders = 0;
    private int createdFiles = 0;
    private int createdFolders = 0;

    public ScanResults() {}

    public void incrementDiscoveredFileCount() {
        this.discoveredFiles++;
    }

    public void incrementDiscoveredFolderCount() {
        this.discoveredFolders++;
    }

    public void incrementCreatedFileCount() {
        this.createdFiles++;
    }

    public void incrementCreatedFolderCount() {
        this.createdFolders++;
    }

    public int getDiscoveredFiles() {
        return discoveredFiles;
    }

    public void setDiscoveredFiles(int discoveredFiles) {
        this.discoveredFiles = discoveredFiles;
    }

    public int getDiscoveredFolders() {
        return discoveredFolders;
    }

    public void setDiscoveredFolders(int discoveredFolders) {
        this.discoveredFolders = discoveredFolders;
    }

    public int getCreatedFiles() {
        return createdFiles;
    }

    public void setCreatedFiles(int createdFiles) {
        this.createdFiles = createdFiles;
    }

    public int getCreatedFolders() {
        return createdFolders;
    }

    public void setCreatedFolders(int createdFolders) {
        this.createdFolders = createdFolders;
    }

    @Override
    public String toString() {
        return String.format("Results\nDiscovered Folders: %d\nDiscovered Files: %d\nCreated Files: %d\nCreated Folders: %d",
                this.discoveredFolders, this.discoveredFiles, this.createdFiles, this.createdFolders);
    }
}
