package com.cloud.NetworkCloudDrive.Models.Jobs;

import com.cloud.NetworkCloudDrive.Models.Enum.ScanOptions;
import com.cloud.NetworkCloudDrive.Models.Enum.System.JobType;

import java.nio.file.Path;

public class MaintenanceJob extends Job {
    private Path startingDirectory;
    private long startingFolderId;
    private ScanOptions scanOptions;

    public MaintenanceJob(String jobName, String jobDescription, Path startingDirectory, long startingFolderId, ScanOptions scanOptions) {
        setJobName(jobName);
        setJobDescription(jobDescription);
        setJobType(JobType.IO_FUNCTION);
        this.scanOptions = scanOptions;
        this.startingDirectory = startingDirectory;
        this.startingFolderId = startingFolderId;
    }

    public MaintenanceJob(Path startingDirectory, long startingFolderId, ScanOptions scanOptions) {
        setJobName("Maintenance Job");
        setJobDescription("Scans directories. Fixes missing files and thumbnails.");
        setJobType(JobType.IO_FUNCTION);
        this.scanOptions = scanOptions;
        this.startingDirectory = startingDirectory;
        this.startingFolderId = startingFolderId;
    }

    public Path getStartingDirectory() {
        return startingDirectory;
    }

    public void setStartingDirectory(Path startingDirectory) {
        this.startingDirectory = startingDirectory;
    }

    public long getStartingFolderId() {
        return startingFolderId;
    }

    public void setStartingFolderId(long startingFolderId) {
        this.startingFolderId = startingFolderId;
    }

    public ScanOptions getScanOptions() {
        return scanOptions;
    }

    public void setScanOptions(ScanOptions scanOptions) {
        this.scanOptions = scanOptions;
    }
}
