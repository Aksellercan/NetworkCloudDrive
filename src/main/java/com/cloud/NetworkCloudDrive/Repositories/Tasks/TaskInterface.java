package com.cloud.NetworkCloudDrive.Repositories.Tasks;

import com.cloud.NetworkCloudDrive.Models.Jobs.Job;
import org.springframework.stereotype.Component;

@Component
public interface TaskInterface {
    boolean runTask(Job job);

    Job getJob();
}
