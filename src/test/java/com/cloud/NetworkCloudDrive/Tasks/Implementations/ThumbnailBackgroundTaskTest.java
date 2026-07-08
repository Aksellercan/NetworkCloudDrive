package com.cloud.NetworkCloudDrive.Tasks.Implementations;

import com.cloud.NetworkCloudDrive.Models.DTO.UserDTO;
import com.cloud.NetworkCloudDrive.Models.Enum.System.JobStatus;
import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.Jobs.Job;
import com.cloud.NetworkCloudDrive.Models.Jobs.ThumbnailJob;
import com.cloud.NetworkCloudDrive.Models.ThumbnailMetadata;
import com.cloud.NetworkCloudDrive.Persistence.SQLiteDAO;
import com.cloud.NetworkCloudDrive.Repositories.Maintenance.ThumbnailRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ThumbnailBackgroundTaskTest {

    @Mock
    private ThumbnailRepository thumbnailRepository;

    @Mock
    private SQLiteDAO sqLiteDAO;

    private ThumbnailBackgroundTask task;

    @BeforeEach
    void setUp() {
        task = new ThumbnailBackgroundTask(thumbnailRepository, sqLiteDAO);
    }

    @Test
    void runTask_WhenThumbnailCreatedSuccessfully_MarksCompletedAndUpdatesMetadata() throws Exception {
        ThumbnailJob job = new ThumbnailJob(Path.of("/test"), "image.jpg", 42L);
        job.setUserDTO(new UserDTO(1L, "user", "user@test.com"));
        ThumbnailMetadata thumbnailMetadata = new ThumbnailMetadata();
        FileMetadata fileMetadata = new FileMetadata();

        when(thumbnailRepository.createAndSaveThumbnail(Path.of("/test"), "image.jpg", 42L, job.getUserDTO()))
                .thenReturn(CompletableFuture.completedFuture(thumbnailMetadata));
        when(sqLiteDAO.queryFileMetadata(42L, 1L)).thenReturn(fileMetadata);

        boolean result = task.runTask(job);

        assertTrue(result);
        assertEquals(JobStatus.COMPLETED, job.getJobStatus());
        assertNotNull(job.getFinishedOn());
        verify(thumbnailRepository).createAndSaveThumbnail(Path.of("/test"), "image.jpg", 42L, job.getUserDTO());
        verify(sqLiteDAO).queryFileMetadata(42L, 1L);
        assertTrue(fileMetadata.isHasThumbnail());
        verify(sqLiteDAO).saveFile(fileMetadata);
    }

    @Test
    void runTask_WhenThumbnailCreationThrows_MarksFailed() throws Exception {
        ThumbnailJob job = new ThumbnailJob(Path.of("/test"), "image.jpg", 42L);
        job.setUserDTO(new UserDTO(1L, "user", "user@test.com"));

        when(thumbnailRepository.createAndSaveThumbnail(Path.of("/test"), "image.jpg", 42L, job.getUserDTO()))
                .thenThrow(new IOException("disk error"));

        boolean result = task.runTask(job);

        assertFalse(result);
        assertEquals(JobStatus.FAILED, job.getJobStatus());
        assertNull(job.getFinishedOn());
        verify(sqLiteDAO, never()).queryFileMetadata(anyLong(), anyLong());
        verify(sqLiteDAO, never()).saveFile(any());
    }

    @Test
    void runTask_WhenCompletableFutureThrowsExecutionException_MarksFailed() throws Exception {
        ThumbnailJob job = new ThumbnailJob(Path.of("/test"), "image.jpg", 42L);
        job.setUserDTO(new UserDTO(1L, "user", "user@test.com"));

        CompletableFuture<ThumbnailMetadata> future = CompletableFuture.failedFuture(new ExecutionException(new IOException("nested error")));
        when(thumbnailRepository.createAndSaveThumbnail(Path.of("/test"), "image.jpg", 42L, job.getUserDTO()))
                .thenReturn(future);

        boolean result = task.runTask(job);

        assertFalse(result);
        assertEquals(JobStatus.FAILED, job.getJobStatus());
    }

    @Test
    void runTask_WhenFileMetadataIsNull_SkipsUpdate() throws Exception {
        ThumbnailJob job = new ThumbnailJob(Path.of("/test"), "image.jpg", 42L);
        job.setUserDTO(new UserDTO(1L, "user", "user@test.com"));
        ThumbnailMetadata thumbnailMetadata = new ThumbnailMetadata();

        when(thumbnailRepository.createAndSaveThumbnail(Path.of("/test"), "image.jpg", 42L, job.getUserDTO()))
                .thenReturn(CompletableFuture.completedFuture(thumbnailMetadata));
        when(sqLiteDAO.queryFileMetadata(42L, 1L)).thenReturn(null);

        boolean result = task.runTask(job);

        assertTrue(result);
        assertEquals(JobStatus.COMPLETED, job.getJobStatus());
        verify(sqLiteDAO).queryFileMetadata(42L, 1L);
        verify(sqLiteDAO, never()).saveFile(any());
    }

    @Test
    void runTask_WhenJobIsNotThumbnailInstance_ThrowsException() {
        Job job = new Job();
        job.setUserDTO(new UserDTO(1L, "user", "user@test.com"));

        boolean result = task.runTask(job);

        assertFalse(result);
        assertEquals(JobStatus.FAILED, job.getJobStatus());
        verifyNoInteractions(thumbnailRepository, sqLiteDAO);
    }
}
