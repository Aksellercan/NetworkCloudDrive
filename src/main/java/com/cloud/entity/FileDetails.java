package com.cloud.entity;

public class FileDetails {
    private String name;
    private String path;
    private long size;
    private boolean isFolder;

    public FileDetails(String name, String path, boolean isFolder) {
        this.name = name;
        this.path = path;
        this.isFolder = isFolder;
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        return path;
    }

    public long getSize() {
        return size;
    }

    public boolean isFolder() {
        return isFolder;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setFolder(boolean folder) {
        isFolder = folder;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
