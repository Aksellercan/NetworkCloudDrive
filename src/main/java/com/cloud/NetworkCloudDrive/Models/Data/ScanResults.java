package com.cloud.NetworkCloudDrive.Models.Data;

public class ScanResults {
    private int discoveredFiles = 0;
    private int discoveredFolders = 0;
    private int createdFiles = 0;
    private int createdFolders = 0;
    private long timeTaken = System.currentTimeMillis();

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

    public long getTimeTaken() {
        return timeTaken;
    }

    public void setTimeTaken(long timeTaken) {
        this.timeTaken = timeTaken;
    }

    public long stopTimerAndGetTimeTaken() {
        this.timeTaken = System.currentTimeMillis() - this.timeTaken;
        return this.timeTaken;
    }

    @Override
    public String toString() {
        return String.format(
                "Results\nDiscovered Folders: %d\nDiscovered Files: %d\nCreated Files: %d\nCreated Folders: %d\nTime Taken: %d ms\n",
                this.discoveredFolders, this.discoveredFiles, this.createdFiles, this.createdFolders, this.timeTaken);
    }
}
