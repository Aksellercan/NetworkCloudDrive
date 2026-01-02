package com.cloud.NetworkCloudDrive.DTO;

import com.cloud.NetworkCloudDrive.Models.FileMetadata;

import java.time.Instant;

public class FileListItemDTO {
    private long id;
    private String name;
    private String mimeType;
    private long size;
    private Instant createdAt;

    public FileListItemDTO() {}

    public FileListItemDTO(FileMetadata fileMetadata) {
        this.id = fileMetadata.getId();
        this.name = fileMetadata.getName();
        this.mimeType = fileMetadata.getMimiType();
        this.size = fileMetadata.getSize();
        this.createdAt = fileMetadata.getCreatedAt();
    }


    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getMimeType() {
        return mimeType;
    }
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
    public long getSize() {
        return size;
    }
    public void setSize(long size) {
        this.size = size;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
