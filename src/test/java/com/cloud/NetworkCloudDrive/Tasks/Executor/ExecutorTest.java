package com.cloud.NetworkCloudDrive.Tasks.Executor;

import com.cloud.NetworkCloudDrive.Models.DTO.UserDTO;
import com.cloud.NetworkCloudDrive.Models.Enum.System.JobStatus;
import com.cloud.NetworkCloudDrive.Models.Enum.System.JobType;
import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.Jobs.Job;
import com.cloud.NetworkCloudDrive.Models.Jobs.ThumbnailJob;
import com.cloud.NetworkCloudDrive.Models.ThumbnailMetadata;
import com.cloud.NetworkCloudDrive.Persistence.SQLiteDAO;
import com.cloud.NetworkCloudDrive.Repositories.Maintenance.ThumbnailRepository;
import com.cloud.NetworkCloudDrive.Sessions.UserSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ExecutorTest {

    @Mock
    private SQLiteDAO sQLiteDAO;

    @Mock
    private UserSession userSession;

    @Mock
    private ThumbnailRepository thumbnailRepository;

    private Executor executor;

    @BeforeEach
    void setUp() {
        executor = new Executor(sQLiteDAO, userSession, thumbnailRepository);
    }

    @Test
    void handle_ThumbnailJob_Success() throws Exception {
        ThumbnailJob job = new ThumbnailJob(Path.of("/test"), "image.jpg", 42L);
        UserDTO userDTO = new UserDTO(1L, "user", "user@test.com");
        job.setUserDTO(userDTO);
        ThumbnailMetadata thumbnailMetadata = new ThumbnailMetadata();
        FileMetadata fileMetadata = new FileMetadata();

        when(thumbnailRepository.createAndSaveThumbnail(Path.of("/test"), "image.jpg", 42L, userDTO))
                .thenReturn(CompletableFuture.completedFuture(thumbnailMetadata));
        when(sQLiteDAO.queryFileMetadata(42L, 1L)).thenReturn(fileMetadata);

        executor.handle(job);

        assertEquals(JobStatus.COMPLETED, job.getJobStatus());
        assertNotNull(job.getFinishedOn());
        verify(thumbnailRepository).createAndSaveThumbnail(Path.of("/test"), "image.jpg", 42L, userDTO);
        verify(sQLiteDAO).queryFileMetadata(42L, 1L);
        assertTrue(fileMetadata.isHasThumbnail());
        verify(sQLiteDAO).saveFile(fileMetadata);
    }

    @Test
    void handle_ThumbnailJob_WhenThumbnailCreationThrows_SetsFailed() throws Exception {
        ThumbnailJob job = new ThumbnailJob(Path.of("/test"), "image.jpg", 42L);
        UserDTO userDTO = new UserDTO(1L, "user", "user@test.com");
        job.setUserDTO(userDTO);

        when(thumbnailRepository.createAndSaveThumbnail(Path.of("/test"), "image.jpg", 42L, userDTO))
                .thenThrow(new IOException("disk error"));

        executor.handle(job);

        assertEquals(JobStatus.FAILED, job.getJobStatus());
        assertNull(job.getFinishedOn());
        verify(sQLiteDAO, never()).queryFileMetadata(anyLong(), anyLong());
    }

    @Test
    void handle_ThumbnailJob_WhenJobIsNotThumbnailInstance_DoesNothing() {
        Job job = new Job();
        job.setUserDTO(new UserDTO(1L, "user", "user@test.com"));
        job.setJobType(JobType.THUMBNAIL_FUNCTION);

        executor.handle(job);

        assertEquals(JobStatus.WAITING, job.getJobStatus());
        assertNull(job.getFinishedOn());
        verifyNoInteractions(thumbnailRepository, sQLiteDAO);
    }

    @Test
    void handle_IOFunction_SetsCompleted() {
        Job job = new Job();
        job.setJobType(JobType.IO_FUNCTION);

        executor.handle(job);

        assertEquals(JobStatus.COMPLETED, job.getJobStatus());
        assertNotNull(job.getFinishedOn());
        verifyNoInteractions(thumbnailRepository, sQLiteDAO);
    }

    @Test
    void handle_RecurrentFunction_SetsCompleted() {
        Job job = new Job();
        job.setJobType(JobType.RECURRENT_FUNCTION);

        executor.handle(job);

        assertEquals(JobStatus.COMPLETED, job.getJobStatus());
        assertNotNull(job.getFinishedOn());
        verifyNoInteractions(thumbnailRepository, sQLiteDAO);
    }

    @Test
    void queueJobs_SetsUserDTOOnJob() {
        UserDTO proposed = new UserDTO(5L, "testUser", "test@example.com");
        when(userSession.returnUserDTO()).thenReturn(proposed);

        ThumbnailJob job = new ThumbnailJob(Path.of("/test"), "image.jpg", 42L);

        executor.queueJobs(job);

        UserDTO dto = job.getUserDTO();
        assertNotNull(dto);
        assertEquals(5L, dto.getUserId());
        assertEquals("testUser", dto.getUserName());
        assertEquals("test@example.com", dto.getUserEmail());
    }
}
