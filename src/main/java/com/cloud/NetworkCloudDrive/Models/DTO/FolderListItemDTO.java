package com.cloud.NetworkCloudDrive.Models.DTO;

import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import com.cloud.NetworkCloudDrive.Models.Implementations.ASCIIRanker;

import java.time.Instant;

public class FolderListItemDTO extends ASCIIRanker implements Comparable<FolderListItemDTO> {
    private long id;
    private String name;
    private String path;
    private Instant createdAt;

    public FolderListItemDTO() {}

    public FolderListItemDTO(FolderMetadata folderMetadata) {
        this.id = folderMetadata.getId();
        this.name = folderMetadata.getName();
        this.path = folderMetadata.getPath();
        this.createdAt = folderMetadata.getCreatedAt();
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
    public String getPath() {
        return path;
    }
    public void setPath(String path) {
        this.path = path;
    }
    public Instant getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public int compareTo(FolderListItemDTO f2) {
        return Integer.compare(calculateRank(this.name.toLowerCase()), calculateRank(f2.getName().toLowerCase()));
    }
}

