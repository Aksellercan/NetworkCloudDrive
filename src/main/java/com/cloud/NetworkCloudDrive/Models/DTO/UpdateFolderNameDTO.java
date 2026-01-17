package com.cloud.NetworkCloudDrive.Models.DTO;

public class UpdateFolderNameDTO {
    private long folder_id;
    private String name;

    public UpdateFolderNameDTO() {}

    public long getFolder_id() {
        return folder_id;
    }
    public void setFolder_id(long folder_id) {
        this.folder_id = folder_id;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
