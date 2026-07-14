package com.cloud.NetworkCloudDrive.Repositories.Tasks;

import com.cloud.NetworkCloudDrive.Models.Jobs.Job;
import org.springframework.stereotype.Component;

@Component
public interface JobExecutorInterface {
    void queueJobs(Job job);
}
