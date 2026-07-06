package com.cloud.NetworkCloudDrive.Tasks.Executor;

import com.cloud.NetworkCloudDrive.Models.DTO.UserDTO;
import com.cloud.NetworkCloudDrive.Models.Enum.System.JobStatus;
import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.Jobs.Job;
import com.cloud.NetworkCloudDrive.Models.Jobs.ThumbnailJob;
import com.cloud.NetworkCloudDrive.Models.ThumbnailMetadata;
import com.cloud.NetworkCloudDrive.Persistence.SQLiteDAO;
import com.cloud.NetworkCloudDrive.Repositories.Maintenance.ThumbnailRepository;
import com.cloud.NetworkCloudDrive.Sessions.UserSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class Executor {
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private final Logger logger = LoggerFactory.getLogger(Executor.class);
    private final SQLiteDAO sQLiteDAO;
    private final UserSession userSession;
    private final ThumbnailRepository thumbnailRepository;

    public Executor(SQLiteDAO sQLiteDAO,
                    UserSession userSession,
                    ThumbnailRepository thumbnailRepository) {
        this.sQLiteDAO = sQLiteDAO;
        this.userSession = userSession;
        this.thumbnailRepository = thumbnailRepository;
    }

    public void queueJobs(Job job) {
        job.setUserDTO(new UserDTO(userSession));
        executorService.submit(() -> {
            handle(job);
        });
    }

    public ThumbnailMetadata handleThumbnailCreation(Path originalFolderPath, String originalFilename, long fileId, UserDTO userDTO) throws IOException, SQLException, ExecutionException, InterruptedException {
        ThumbnailMetadata thumbnail = thumbnailRepository.createAndSaveThumbnail(originalFolderPath, originalFilename, fileId, userDTO).get();
        updateFileMetadata(fileId, true, userDTO.getUserId());
        return thumbnail;
    }

    private void updateFileMetadata(long fileId, boolean setThumbnail, long userId) throws SQLException {
        FileMetadata fileMetadata = sQLiteDAO.queryFileMetadata(fileId, userId);
        if (fileMetadata == null) {
            return;
        }
        fileMetadata.setHasThumbnail(setThumbnail);
        sQLiteDAO.saveFile(fileMetadata);
    }

    public void handle(Job job) {
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
                        ThumbnailMetadata thumbnailMetadata = handleThumbnailCreation(((ThumbnailJob) job).getOriginalFolderPath(), ((ThumbnailJob) job).getOriginalFilename(), ((ThumbnailJob) job).getFileId(), job.getUserDTO());
                        logger.info(thumbnailMetadata.toString());
                        job.setJobStatus(JobStatus.COMPLETED);
                        job.setFinishedOn();
                        logger.info("completed job = {}", job.toString());
                    } catch (Exception e) {
                        logger.error(e.getMessage());
                        job.setJobStatus(JobStatus.FAILED);
                        logger.warn(job.toString());
                    }
                }
            }
            default -> {
            }
        }
    }
}

