package com.cloud.NetworkCloudDrive.Queues;

import com.cloud.NetworkCloudDrive.Jobs.ThumbnailServiceJob;
import com.cloud.NetworkCloudDrive.Models.DTO.JobDTO;
import com.cloud.NetworkCloudDrive.Models.Enum.System.JobStatus;
import com.cloud.NetworkCloudDrive.Models.Jobs.Job;
import com.cloud.NetworkCloudDrive.Models.Jobs.ThumbnailJob;
import com.cloud.NetworkCloudDrive.Models.ThumbnailMetadata;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.concurrent.LinkedBlockingQueue;

@Service
public class JobQueue {
    private final LinkedBlockingQueue<Job> queue = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<Job> retryQueue = new LinkedBlockingQueue<>();
    private final int workerCount = 2;
    private final Logger logger = LoggerFactory.getLogger(JobQueue.class);

    //Repos
    private final ThumbnailServiceJob thumbnailServiceJob;

    public JobQueue(ThumbnailServiceJob thumbnailServiceJob) {
        this.thumbnailServiceJob = thumbnailServiceJob;
    }

    public JobDTO addToQueue(Job job) {
        queue.offer(job);
        return new JobDTO(job);
    }

    @PostConstruct
    public void worker() {
        Thread thread = new Thread(() -> {
            try {
                while (true) {
                    processAllQueues();
                    Thread.sleep(2500);
                }
            } catch (InterruptedException e) {
                logger.error(e.getMessage());
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    public void processAllQueues() {
        for (Job job : queue) {
            executer(job);
            if (job.getJobStatus().equals(JobStatus.FAILED)) {
                if (job.allowRetry()) {
                    job.incrementRetryCount();
                    retryQueue.offer(job);
                }
            }
            logger.info("ran job = {}", job.toString());
            queue.remove(job);
        }
        if (!retryQueue.isEmpty()) {
            for (Job job : retryQueue) {
                executer(job);
                job.incrementRetryCount();
                if (job.getRetryCount() <= 2) {
                    logger.info("Removed retry job {}", job.getId());
                    retryQueue.remove(job);
                }
                logger.info("retried job = {}", job.toString());
            }
        }
    }

    public void executer(Job job) {
        switch (job.getJobType()) {
            case IO_FUNCTION, RECURRENT_FUNCTION -> {
                job.setJobStatus(JobStatus.COMPLETED);
                job.setFinishedOn();
                logger.info(job.toString());
            }
            case THUMBNAIL_FUNCTION -> {
                if (job instanceof ThumbnailJob) {
                    try {
                        logger.info("offered job = {}", job.toString());
                        job.setJobStatus(JobStatus.RUNNING);
                        ThumbnailMetadata thumbnailMetadata = thumbnailServiceJob.handleThumbnailCreation(((ThumbnailJob) job).getOriginalFolderPath(), ((ThumbnailJob) job).getOriginalFilename(), ((ThumbnailJob) job).getFileId());
                        logger.info(thumbnailMetadata.toString());
                        job.setJobStatus(JobStatus.COMPLETED);
                        job.setFinishedOn();
                    } catch (Exception e) {
                        logger.error(e.getMessage());
                        job.setJobStatus(JobStatus.FAILED);
                    }
                }
            }
            default -> {
            }
        }
    }

    public LinkedBlockingQueue<Job> getQueue() {
        return queue;
    }

    LinkedBlockingQueue<Job> getRetryQueue() {
        return retryQueue;
    }
}
