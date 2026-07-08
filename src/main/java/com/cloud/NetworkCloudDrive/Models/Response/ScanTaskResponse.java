package com.cloud.NetworkCloudDrive.Models.Response;

import com.cloud.NetworkCloudDrive.Models.Enum.System.JobStatus;

import java.time.Instant;
import java.util.UUID;

public class ScanTaskResponse {
    private final UUID uuid = UUID.randomUUID();
    private String taskName;
    private UUID jobId;
    private Instant jobStartedOn;

    private JobStatus jobStatus;

    public ScanTaskResponse() {
    }

    public ScanTaskResponse(String taskName, UUID jobId, Instant jobStartedOn, JobStatus jobStatus) {
        this.taskName = taskName;
        this.jobId = jobId;
        this.jobStartedOn = jobStartedOn;
        this.jobStatus = jobStatus;
    }

    public UUID getUuid() {
        return uuid;
    }

    public JobStatus getJobStatus() {
        return jobStatus;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public void setJobStatus(JobStatus jobStatus) {
        this.jobStatus = jobStatus;
    }

    public UUID getJobId() {
        return jobId;
    }

    public void setJobId(UUID jobId) {
        this.jobId = jobId;
    }

    public Instant getJobStartedOn() {
        return jobStartedOn;
    }

    public void setJobStartedOn(Instant jobStartedOn) {
        this.jobStartedOn = jobStartedOn;
    }
}
