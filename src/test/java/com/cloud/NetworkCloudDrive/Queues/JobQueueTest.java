package com.cloud.NetworkCloudDrive.Queues;

import com.cloud.NetworkCloudDrive.Jobs.ThumbnailServiceJob;
import com.cloud.NetworkCloudDrive.Models.DTO.JobDTO;
import com.cloud.NetworkCloudDrive.Models.Enum.System.JobStatus;
import com.cloud.NetworkCloudDrive.Models.Enum.System.JobType;
import com.cloud.NetworkCloudDrive.Models.Jobs.Job;
import com.cloud.NetworkCloudDrive.Models.Jobs.ThumbnailJob;
import com.cloud.NetworkCloudDrive.Models.ThumbnailMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobQueueTest {

    @Mock
    private ThumbnailServiceJob thumbnailServiceJob;

    private JobQueue jobQueue;

    @BeforeEach
    void setUp() {
        jobQueue = new JobQueue(thumbnailServiceJob);
    }

    @Test
    void executeThumbnailJob_Success() throws Exception {
        ThumbnailJob job = new ThumbnailJob(Path.of("/test"), "image.jpg", 42L);
        when(thumbnailServiceJob.handleThumbnailCreation(
                Path.of("/test"), "image.jpg", 42L))
                .thenReturn(new ThumbnailMetadata());

        jobQueue.executer(job);

        assertEquals(JobStatus.COMPLETED, job.getJobStatus());
        assertNotNull(job.getFinishedOn());
        verify(thumbnailServiceJob).handleThumbnailCreation(Path.of("/test"), "image.jpg", 42L);
    }

    @Test
    void executeThumbnailJob_WhenServiceThrowsIOException_SetsFailed() throws Exception {
        ThumbnailJob job = new ThumbnailJob(Path.of("/test"), "image.jpg", 42L);
        when(thumbnailServiceJob.handleThumbnailCreation(
                Path.of("/test"), "image.jpg", 42L)).thenThrow(new IOException());

        jobQueue.executer(job);

        assertEquals(JobStatus.FAILED, job.getJobStatus());
        assertNull(job.getFinishedOn());
    }

    @Test
    void executeThumbnailJob_WhenHandleThumbnailCreationReturnsNull_SetsFailed() throws Exception {
        ThumbnailJob job = new ThumbnailJob(Path.of("/test"), "image.jpg", 42L);
        when(thumbnailServiceJob.handleThumbnailCreation(
                Path.of("/test"), "image.jpg", 42L)).thenReturn(null);

        jobQueue.executer(job);

        assertEquals(JobStatus.FAILED, job.getJobStatus());
        assertNull(job.getFinishedOn());
    }

    @Test
    void executeThumbnailJob_WhenServiceThrowsSQLException_SetsFailed() throws Exception {
        ThumbnailJob job = new ThumbnailJob(Path.of("/test"), "image.jpg", 42L);
        when(thumbnailServiceJob.handleThumbnailCreation(
                Path.of("/test"), "image.jpg", 42L)).thenThrow(new SQLException("DB error"));

        jobQueue.executer(job);

        assertEquals(JobStatus.FAILED, job.getJobStatus());
        assertNull(job.getFinishedOn());
    }

    @Test
    void executeThumbnailJob_WhenServiceThrowsRuntimeException_SetsFailed() throws Exception {
        ThumbnailJob job = new ThumbnailJob(Path.of("/test"), "image.jpg", 42L);
        when(thumbnailServiceJob.handleThumbnailCreation(
                Path.of("/test"), "image.jpg", 42L)).thenThrow(new RuntimeException("unexpected"));

        jobQueue.executer(job);

        assertEquals(JobStatus.FAILED, job.getJobStatus());
        assertNull(job.getFinishedOn());
    }

    @Test
    void executeThumbnailJob_WhenJobIsNotThumbnailInstance_DoesNothing() {
        Job job = new Job();
        job.setJobType(JobType.THUMBNAIL_FUNCTION);

        jobQueue.executer(job);

        assertEquals(JobStatus.WAITING, job.getJobStatus());
        assertNull(job.getFinishedOn());
        verifyNoInteractions(thumbnailServiceJob);
    }

    @Test
    void executeNonThumbnailTypes_SetCompleted() {
        for (JobType type : new JobType[]{JobType.IO_FUNCTION, JobType.RECURRENT_FUNCTION}) {
            Job job = new Job();
            job.setJobType(type);
            assertNull(job.getFinishedOn());

            jobQueue.executer(job);

            assertEquals(JobStatus.COMPLETED, job.getJobStatus());
            assertNotNull(job.getFinishedOn());
            verifyNoInteractions(thumbnailServiceJob);
        }
    }

    @Test
    void addToQueue_AddsThumbnailJobAndReturnsDTO() {
        ThumbnailJob job = new ThumbnailJob(Path.of("/test"), "image.jpg", 42L);

        JobDTO dto = jobQueue.addToQueue(job);

        assertNotNull(dto);
        assertEquals(job.getId(), dto.getUUID());
        assertEquals(JobStatus.WAITING, dto.getJobStatus());
        assertTrue(jobQueue.getQueue().contains(job));
    }

    @Test
    void thumbnailJobDoesNotAllowRetry() {
        ThumbnailJob job = new ThumbnailJob(Path.of("/test"), "image.jpg", 42L);
        assertFalse(job.allowRetry());
    }

    @Test
    void processAllQueues_ProcessesJobAndRemovesFromQueue() throws Exception {
        ThumbnailJob job = new ThumbnailJob(Path.of("/test"), "image.jpg", 42L);
        when(thumbnailServiceJob.handleThumbnailCreation(
                Path.of("/test"), "image.jpg", 42L))
                .thenReturn(new ThumbnailMetadata());
        jobQueue.addToQueue(job);
        assertTrue(jobQueue.getQueue().contains(job));

        jobQueue.processAllQueues();

        assertEquals(JobStatus.COMPLETED, job.getJobStatus());
        assertFalse(jobQueue.getQueue().contains(job));
    }

    @Test
    void processAllQueues_MovesFailedRetryableJobToRetryQueue() throws Exception {
        ThumbnailJob job = new ThumbnailJob(Path.of("/test"), "image.jpg", 42L);
        job.setRetry(true);
        when(thumbnailServiceJob.handleThumbnailCreation(
                Path.of("/test"), "image.jpg", 42L))
                .thenThrow(new IOException());

        jobQueue.addToQueue(job);
        jobQueue.processAllQueues();

        assertEquals(JobStatus.FAILED, job.getJobStatus());
        assertEquals(2, job.getRetryCount());
        assertFalse(jobQueue.getRetryQueue().contains(job));
    }

    @Test
    void processAllQueues_DoesNotMoveFailedNonRetryableJobToRetryQueue() {
        ThumbnailJob job = new ThumbnailJob(Path.of("/test"), "image.jpg", 42L);

        jobQueue.addToQueue(job);
        jobQueue.processAllQueues();

        assertEquals(JobStatus.FAILED, job.getJobStatus());
        assertEquals(0, job.getRetryCount());
        assertTrue(jobQueue.getRetryQueue().isEmpty());
    }

    @Test
    void processAllQueues_ProcessesRetryQueueJobs() {
        Job job = new Job();
        job.setRetry(true);
        job.setJobType(JobType.IO_FUNCTION);
        jobQueue.getRetryQueue().offer(job);

        jobQueue.processAllQueues();

        assertEquals(JobStatus.COMPLETED, job.getJobStatus());
        assertEquals(1, job.getRetryCount());
        assertFalse(jobQueue.getRetryQueue().contains(job));
    }
}
