package com.cloud.NetworkCloudDrive.Models.Jobs;

import com.cloud.NetworkCloudDrive.Models.DTO.UserDTO;
import com.cloud.NetworkCloudDrive.Models.Enum.System.JobStatus;
import com.cloud.NetworkCloudDrive.Models.Enum.System.JobType;

import java.time.Instant;
import java.util.UUID;

public class Job {
    private final UUID id = UUID.randomUUID();
    private UserDTO userDTO;
    private String jobName;
    private String jobDescription;
    private JobStatus jobStatus = JobStatus.WAITING;
    private JobType jobType;
    private final Instant addedOn = Instant.now();
    private Instant finishedOn;
    private boolean retry = false;
    private int retryCount = 0;

    public Job() {
    }

    public UUID getId() {
        return id;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public String getJobDescription() {
        return jobDescription;
    }

    public void setJobDescription(String jobDescription) {
        this.jobDescription = jobDescription;
    }

    public JobStatus getJobStatus() {
        return jobStatus;
    }

    public void setJobStatus(JobStatus jobStatus) {
        this.jobStatus = jobStatus;
    }

    public JobType getJobType() {
        return jobType;
    }

    public void setFinishedOn() {
        this.finishedOn = Instant.now();
    }

    public Instant getAddedOn() {
        return addedOn;
    }

    public Instant getFinishedOn() {
        return finishedOn;
    }

    public void setJobType(JobType jobType) {
        this.jobType = jobType;
    }

    public void setFinishedOn(Instant finishedOn) {
        this.finishedOn = finishedOn;
    }

    public int getRetryCount() {
        return retryCount;
    }

    public void incrementRetryCount() {
        this.retryCount++;
    }

    public boolean allowRetry() {
        return retry;
    }

    public void setRetry(boolean retry) {
        this.retry = retry;
    }

    public UserDTO getUserDTO() {
        return userDTO;
    }

    public void setUserDTO(UserDTO userDTO) {
        this.userDTO = userDTO;
    }

    @Override
    public String toString() {
        return "Job{" +
                "id=" + id +
                ", userDTO=" + userDTO +
                ", jobName='" + jobName + '\'' +
                ", jobDescription='" + jobDescription + '\'' +
                ", jobStatus=" + jobStatus +
                ", jobType=" + jobType +
                ", addedOn=" + addedOn +
                ", finishedOn=" + finishedOn +
                ", retry=" + retry +
                ", retryCount=" + retryCount +
                '}';
    }
}
