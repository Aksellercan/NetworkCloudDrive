package com.cloud.NetworkCloudDrive.Models.Jobs;

import com.cloud.NetworkCloudDrive.Models.Enum.System.JobType;

import java.nio.file.Path;

public class ThumbnailJob extends Job {
    private Path originalFolderPath;
    private String originalFilename;
    private long fileId;

    public ThumbnailJob(Path originalFolderPath, String originalFilename, long fileId) {
        setJobName("Thumbnail Job");
        setJobDescription("Generates thumbnail");
        setJobType(JobType.THUMBNAIL_FUNCTION);
        setRetry(false);
        this.originalFolderPath = originalFolderPath;
        this.originalFilename = originalFilename;
        this.fileId = fileId;
    }

    public Path getOriginalFolderPath() {
        return originalFolderPath;
    }

    public void setOriginalFolderPath(Path originalFolderPath) {
        this.originalFolderPath = originalFolderPath;
    }

    public String getOriginalFilename() {
        return originalFilename;
    }

    public void setOriginalFilename(String originalFilename) {
        this.originalFilename = originalFilename;
    }

    public long getFileId() {
        return fileId;
    }

    public void setFileId(long fileId) {
        this.fileId = fileId;
    }
}
