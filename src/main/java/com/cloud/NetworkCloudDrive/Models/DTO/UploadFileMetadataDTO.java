package com.cloud.NetworkCloudDrive.Models.DTO;

import com.cloud.NetworkCloudDrive.Models.FileMetadata;

public class UploadFileMetadataDTO {
    private Long fileId;
    private Long folderId;
    private Long userId;
    private boolean hasThumbnail = false;

    public UploadFileMetadataDTO(Long fileId, Long folderId, Long userId, boolean hasThumbnail) {
        this.fileId = fileId;
        this.folderId = folderId;
        this.userId = userId;
        this.hasThumbnail = hasThumbnail;
    }

    public UploadFileMetadataDTO(FileMetadata metadata) {
        this.fileId = metadata.getId();
        this.folderId = metadata.getFolderId();
        this.userId = metadata.getUserid();
        this.hasThumbnail = metadata.isHasThumbnail();
    }

    public UploadFileMetadataDTO() {}

    public Long getFileId() {
        return fileId;
    }

    public void setFileId(Long fileId) {
        this.fileId = fileId;
    }

    public Long getFolderId() {
        return folderId;
    }

    public void setFolderId(Long folderId) {
        this.folderId = folderId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public boolean isHasThumbnail() {
        return hasThumbnail;
    }

    public void setHasThumbnail(boolean hasThumbnail) {
        this.hasThumbnail = hasThumbnail;
    }
}
