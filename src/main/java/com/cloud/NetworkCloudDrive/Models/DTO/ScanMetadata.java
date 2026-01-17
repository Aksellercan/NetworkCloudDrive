package com.cloud.NetworkCloudDrive.Models.DTO;

public class ScanMetadata<T> {
    private T metadata;
    private boolean updated = false;

    public ScanMetadata(T metadata, boolean updated) {
        this.metadata = metadata;
        this.updated = updated;
    }

    public ScanMetadata() {
    }

    public T getMetadata() {
        return metadata;
    }

    public void setMetadata(T metadata) {
        this.metadata = metadata;
    }

    public boolean isUpdated() {
        return updated;
    }

    public void setUpdated(boolean updated) {
        this.updated = updated;
    }
}
