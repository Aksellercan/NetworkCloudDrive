package com.cloud.entity;

import com.cloud.enumurator.FileTypes;

public class Files {
    private String fileName;
    private double fileSize;
    private String dateTime;
    private String path;
    private FileTypes fileType;

    public Files(String fileName, String path ,FileTypes fileType) {
        this.fileName = fileName;
        this.path = path;
        this.fileType = fileType;
    }

    public String getFileName() {
        return fileName;
    }

    public double getFileSize() {
        return fileSize;
    }

    public String getDateTime() {
        return dateTime;
    }

    public FileTypes getFileType() {
        return fileType;
    }

    public String getPath() {
        return path;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public void setDateTime(String dateTime) {
        this.dateTime = dateTime;
    }

    public void setFileType(FileTypes fileType) {
        this.fileType = fileType;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public void setFileSize(double fileSize) {
        this.fileSize = fileSize;
    }

    @Override
    public String toString() {
        return String.format("File:\nname: %s\nsize: %f\ntype: %s\ndate: %s", this.fileName, this.fileSize, this.fileType.toString(), this.dateTime);
    }
}
