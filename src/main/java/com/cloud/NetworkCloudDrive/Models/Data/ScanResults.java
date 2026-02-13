package com.cloud.NetworkCloudDrive.Models.Data;

public class ScanResults {
    private boolean success;
    private int discoveredFiles;
    private int discoveredFolders;
    private int createdFiles;
    private int createdFolders;

    public ScanResults() {
    }

    public ScanResults(boolean success, int discoveredFiles, int discoveredFolders, int createdFiles, int createdFolders) {
        this.success = success;
        this.discoveredFiles = discoveredFiles;
        this.discoveredFolders = discoveredFolders;
        this.createdFiles = createdFiles;
        this.createdFolders = createdFolders;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
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
}
