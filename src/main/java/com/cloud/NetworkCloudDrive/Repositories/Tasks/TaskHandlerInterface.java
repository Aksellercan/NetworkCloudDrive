package com.cloud.NetworkCloudDrive.Repositories.Tasks;

import com.cloud.NetworkCloudDrive.Models.Jobs.Job;
import org.springframework.stereotype.Component;

@Component
public interface TaskHandlerInterface {
    void handle(Job job);

    Job getJob();

    int getProgress();

    boolean halt();
}
