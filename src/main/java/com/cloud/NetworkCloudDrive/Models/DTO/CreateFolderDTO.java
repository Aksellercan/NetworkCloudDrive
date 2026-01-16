package com.cloud.NetworkCloudDrive.Models.DTO;

public class CreateFolderDTO {
    private long folder_id;
    private String name;

    public CreateFolderDTO() {}

    public long getFolder_id() {
        return folder_id;
    }
    public void setFolder_id(long folder_id) {
        this.folder_id = folder_id;
    }
    public String getName() {
        return name;
    }
    public void getPath(String name) {
        this.name = name;
    }
}
