package com.cloud.NetworkCloudDrive.Models.DTO;

import com.cloud.NetworkCloudDrive.Models.Enum.System.JobStatus;
import com.cloud.NetworkCloudDrive.Models.Jobs.Job;

import java.util.UUID;

public class JobDTO {
    private UUID UUID;
    private JobStatus jobStatus;

    public JobDTO() {
    }


    public JobDTO(Job job) {
        this.UUID = job.getId();
        this.jobStatus = job.getJobStatus();
    }

    public UUID getUUID() {
        return UUID;
    }

    public void setUUID(UUID UUID) {
        this.UUID = UUID;
    }

    public JobStatus getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(JobStatus jobStatus) {
        this.jobStatus = jobStatus;
    }
}
