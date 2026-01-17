package com.cloud.NetworkCloudDrive.Models.DTO;

public class UpdateFolderPathDTO {
    private long former_folder_id;
    private long destination_folder_id;

    public UpdateFolderPathDTO() {}

    public long getFormer_folder_id() {
        return this.former_folder_id;
    }
    public void setFormerFolderid(long former_folder_id) {
        this.former_folder_id = former_folder_id;
    }
    public long getDestination_folder_id() {
        return this.destination_folder_id;
    }
    public void setDestination_folder_id(long destination_folder_id) {
        this.destination_folder_id = destination_folder_id;
    }
}
