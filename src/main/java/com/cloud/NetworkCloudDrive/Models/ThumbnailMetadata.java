package com.cloud.NetworkCloudDrive.Models;

import jakarta.persistence.*;

@Entity
public class ThumbnailMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Column(name = "name")
    private String fileName;
    @Column(name = "mimeType")
    private String mimeType;
    private long size;
    @Column(name = "fileid")
    private long fileId;
    @Column(name = "userid")
    private long userId;

    public ThumbnailMetadata(String fileName, long userId, String mimeType, long size, long fileId) {
        this.fileName = fileName;
        this.userId = userId;
        this.mimeType = mimeType;
        this.size = size;
        this.fileId = fileId;
    }

    public ThumbnailMetadata() {}

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public long getSize() {
        return size;
    }
    public long getUserId() {
        return userId;
    }
    public void setUserId(long userId) {
        this.userId = userId;
    }
    public long getFileId() {
        return fileId;
    }
    public void setFileId(long fileId) {
        this.fileId = fileId;
    }
    public String getFileName() {
        return fileName;
    }
    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    public String getMimeType() {
        return mimeType;
    }
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }
}
