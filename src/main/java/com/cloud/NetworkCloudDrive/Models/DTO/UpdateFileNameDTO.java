package com.cloud.NetworkCloudDrive.Models.DTO;

public class UpdateFileNameDTO {
    private long file_id;
    private String name;

    public UpdateFileNameDTO() {}

    public long getFile_id() {
        return this.file_id;
    }
    public void setFile_id(long file_id) {
        this.file_id = file_id;
    }
    public String getName() {
        return this.name;
    }
    public void setName(String name) {
        this.name = name;
    }
}
