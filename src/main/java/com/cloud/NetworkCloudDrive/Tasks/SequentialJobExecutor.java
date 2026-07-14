package com.cloud.NetworkCloudDrive.Tasks;

import com.cloud.NetworkCloudDrive.Models.Jobs.Job;
import com.cloud.NetworkCloudDrive.Repositories.Tasks.JobExecutorInterface;
import com.cloud.NetworkCloudDrive.Repositories.Tasks.TaskHandlerInterface;
import com.cloud.NetworkCloudDrive.Sessions.UserSession;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class SequentialJobExecutor implements JobExecutorInterface {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final UserSession userSession;
    private final TaskHandlerInterface taskHandler;

    public SequentialJobExecutor(
            TaskHandlerInterface taskHandler,
            UserSession userSession) {
        this.userSession = userSession;
        this.taskHandler = taskHandler;
    }

    @Override
    public void queueJobs(Job job) {
        job.setUserDTO(userSession.returnUserDTO());
        executorService.submit(() -> {
            taskHandler.handle(job);
        });
    }
}

