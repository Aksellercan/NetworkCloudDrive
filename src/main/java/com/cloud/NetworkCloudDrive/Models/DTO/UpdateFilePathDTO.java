package com.cloud.NetworkCloudDrive.Models.DTO;

public class UpdateFilePathDTO {
    private long file_id;
    private long folder_id;

    public UpdateFilePathDTO() {}

    public long getFile_id() {
        return this.file_id;
    }
    public void setFile_id(long file_id) {
        this.file_id = file_id;
    }
    public long getFolder_id() {
        return this.folder_id;
    }
    public void setFolder_id(long folder_id) {
        this.folder_id = folder_id;
    }
}
