package com.cloud.NetworkCloudDrive.Tasks;

import com.cloud.NetworkCloudDrive.Models.Jobs.Job;
import com.cloud.NetworkCloudDrive.Repositories.Tasks.TaskHandlerInterface;
import com.cloud.NetworkCloudDrive.Tasks.Implementations.MaintenanceBackgroundTask;
import com.cloud.NetworkCloudDrive.Tasks.Implementations.ThumbnailBackgroundTask;
import org.springframework.stereotype.Component;

@Component
public class TaskHandler implements TaskHandlerInterface {
    private Job job;
    private final ThumbnailBackgroundTask thumbnailBackgroundTask;
    private final MaintenanceBackgroundTask maintenanceBackgroundTask;

    public TaskHandler(
            ThumbnailBackgroundTask thumbnailBackgroundTask,
            MaintenanceBackgroundTask maintenanceBackgroundTask) {
        this.thumbnailBackgroundTask = thumbnailBackgroundTask;
        this.maintenanceBackgroundTask = maintenanceBackgroundTask;
    }

    @Override
    public void handle(Job job) {
        switch (job.getJobType()) {
            case IO_FUNCTION -> {
                boolean result = maintenanceBackgroundTask.runTask(job);
            }
            case THUMBNAIL_FUNCTION -> {
                boolean result = thumbnailBackgroundTask.runTask(job);
            }
            case RECURRENT_FUNCTION -> {

            }
            default -> {
            }
        }
    }

    @Override
    public Job getJob() {
        return this.job;
    }

    @Override
    public int getProgress() {
        return 0;
    }

    @Override
    public boolean halt() {
        return false;
    }
}
