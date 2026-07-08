package com.cloud.NetworkCloudDrive.Tasks.Implementations;

import com.cloud.NetworkCloudDrive.Models.Enum.System.JobStatus;
import com.cloud.NetworkCloudDrive.Models.Jobs.Job;
import com.cloud.NetworkCloudDrive.Models.Jobs.MaintenanceJob;
import com.cloud.NetworkCloudDrive.Repositories.Maintenance.MaintenanceRepository;
import com.cloud.NetworkCloudDrive.Repositories.Tasks.TaskInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class MaintenanceBackgroundTask implements TaskInterface {
    private final Logger logger = LoggerFactory.getLogger(MaintenanceBackgroundTask.class);
    private final MaintenanceRepository maintenanceRepository;
    private Job job;

    public MaintenanceBackgroundTask(@Lazy MaintenanceRepository maintenanceRepository) {
        this.maintenanceRepository = maintenanceRepository;
    }

    @Override
    public boolean runTask(Job job) {
        try {
            if (job instanceof MaintenanceJob maintenanceJob) {
                logger.info("offered job = {}", job.toString());
                job.setJobStatus(JobStatus.RUNNING);
                maintenanceRepository.scanOptionsControllerConverter(maintenanceJob);
                job.setJobStatus(JobStatus.COMPLETED);
                job.setFinishedOn();
                logger.info("completed job = {}", job.toString());
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            job.setJobStatus(JobStatus.FAILED);
            logger.warn(job.toString());
            return false;
        }
        return true;
    }

    private void setJob(Job job) {
        this.job = job;
    }

    @Override
    public Job getJob() {
        return this.job;
    }
}
