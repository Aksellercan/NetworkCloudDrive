package com.cloud.NetworkCloudDrive.Models.Exception;

public class JobRunFailed extends RuntimeException {
    public JobRunFailed(String message) {
        super(message);
    }
}
