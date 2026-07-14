package com.cloud.NetworkCloudDrive.Tasks.Executor;

import com.cloud.NetworkCloudDrive.Models.DTO.UserDTO;
import com.cloud.NetworkCloudDrive.Models.Jobs.Job;
import com.cloud.NetworkCloudDrive.Repositories.Tasks.TaskHandlerInterface;
import com.cloud.NetworkCloudDrive.Sessions.UserSession;
import com.cloud.NetworkCloudDrive.Tasks.SequentialJobExecutor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SequentialJobExecutorTest {

    @Mock
    private TaskHandlerInterface taskHandler;

    @Mock
    private UserSession userSession;

    private SequentialJobExecutor sequentialJobExecutor;

    @BeforeEach
    void setUp() {
        sequentialJobExecutor = new SequentialJobExecutor(taskHandler, userSession);
    }

    @Test
    void queueJobs_SetsUserDTOAndDelegatesToHandler() throws Exception {
        UserDTO proposed = new UserDTO(5L, "testUser", "test@example.com");
        when(userSession.returnUserDTO()).thenReturn(proposed);

        Job job = new Job();
        sequentialJobExecutor.queueJobs(job);

        UserDTO dto = job.getUserDTO();
        assertNotNull(dto);
        assertEquals(5L, dto.getUserId());
        assertEquals("testUser", dto.getUserName());
        assertEquals("test@example.com", dto.getUserEmail());

        verify(taskHandler, timeout(1000)).handle(job);
    }

    @Test
    void queueJobs_WithThumbnailJob_SetsUserDTOAndDelegates() throws Exception {
        UserDTO proposed = new UserDTO(3L, "thumbUser", "thumb@test.com");
        when(userSession.returnUserDTO()).thenReturn(proposed);

        com.cloud.NetworkCloudDrive.Models.Jobs.ThumbnailJob job =
                new com.cloud.NetworkCloudDrive.Models.Jobs.ThumbnailJob(
                        java.nio.file.Path.of("/test"), "image.jpg", 42L);

        sequentialJobExecutor.queueJobs(job);

        UserDTO dto = job.getUserDTO();
        assertNotNull(dto);
        assertEquals(3L, dto.getUserId());

        verify(taskHandler, timeout(1000)).handle(job);
    }

    @Test
    void queueJobs_ThrowsWhenUserSessionFails() {
        when(userSession.returnUserDTO()).thenThrow(new RuntimeException("session error"));

        Job job = new Job();
        assertThrows(RuntimeException.class, () -> sequentialJobExecutor.queueJobs(job));
    }
}
