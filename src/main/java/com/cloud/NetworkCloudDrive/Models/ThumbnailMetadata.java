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

    @Column(name = "fileid", unique = true)
    private long fileId;

    @Column(name = "userid")
    private long userId;

    @Column(name = "isPortrait")
    private boolean isPortrait = false;

    public ThumbnailMetadata(String fileName, long userId, String mimeType, long size, long fileId, boolean isPortrait) {
        this.fileName = fileName;
        this.userId = userId;
        this.mimeType = mimeType;
        this.size = size;
        this.fileId = fileId;
        this.isPortrait = isPortrait;
    }

    public ThumbnailMetadata() {
    }

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

    public boolean isPortrait() {
        return isPortrait;
    }

    public void setPortrait(boolean portrait) {
        isPortrait = portrait;
    }

    public void setSize(long size) {
        this.size = size;
    }

    @Override
    public String toString() {
        return String.format("ID: %d Name: %s MimeType: %s Size: %d, FileID: %d", this.id, this.fileName, this.mimeType, this.size, this.fileId);
    }
}
