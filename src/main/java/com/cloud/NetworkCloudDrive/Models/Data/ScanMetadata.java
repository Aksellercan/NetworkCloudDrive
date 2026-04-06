package com.cloud.NetworkCloudDrive.Models.Data;

// No idea what this is for...
public class ScanMetadata<T> {
    private T metadata;

    public ScanMetadata(T metadata) {
        this.metadata = metadata;
    }

    public ScanMetadata() {
    }

    public T getMetadata() {
        return metadata;
    }

    public void setMetadata(T metadata) {
        this.metadata = metadata;
    }
}
