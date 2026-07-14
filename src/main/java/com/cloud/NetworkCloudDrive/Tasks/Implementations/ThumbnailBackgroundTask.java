package com.cloud.NetworkCloudDrive.Tasks.Implementations;

import com.cloud.NetworkCloudDrive.Models.DTO.UserDTO;
import com.cloud.NetworkCloudDrive.Models.Enum.System.JobStatus;
import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.Jobs.Job;
import com.cloud.NetworkCloudDrive.Models.Jobs.ThumbnailJob;
import com.cloud.NetworkCloudDrive.Models.ThumbnailMetadata;
import com.cloud.NetworkCloudDrive.Persistence.SQLiteDAO;
import com.cloud.NetworkCloudDrive.Repositories.Maintenance.ThumbnailRepository;
import com.cloud.NetworkCloudDrive.Repositories.Tasks.TaskInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.concurrent.ExecutionException;

@Component
public class ThumbnailBackgroundTask implements TaskInterface {
    private final Logger logger = LoggerFactory.getLogger(ThumbnailBackgroundTask.class);
    private final ThumbnailRepository thumbnailRepository;
    private final SQLiteDAO sqLiteDAO;
    private Job currentJob;

    public ThumbnailBackgroundTask(
            ThumbnailRepository thumbnailRepository,
            SQLiteDAO sqLiteDAO) {
        this.thumbnailRepository = thumbnailRepository;
        this.sqLiteDAO = sqLiteDAO;
    }

    private ThumbnailMetadata handleThumbnailCreation(Path originalFolderPath, String originalFilename, long fileId, UserDTO userDTO) throws IOException, SQLException, ExecutionException, InterruptedException {
        ThumbnailMetadata thumbnail = thumbnailRepository.createAndSaveThumbnail(originalFolderPath, originalFilename, fileId, userDTO).get();
        updateFileMetadata(fileId, true, userDTO.getUserId());
        return thumbnail;
    }

    private void updateFileMetadata(long fileId, boolean setThumbnail, long userId) throws SQLException {
        FileMetadata fileMetadata = sqLiteDAO.queryFileMetadata(fileId, userId);
        if (fileMetadata == null) {
            return;
        }
        fileMetadata.setHasThumbnail(setThumbnail);
        sqLiteDAO.saveFile(fileMetadata);
    }

    private void setCurrentJob(Job job) {
        this.currentJob = job;
    }

    @Override
    public boolean runTask(Job job) {
        try {
            ThumbnailJob thumbnailJob = (ThumbnailJob) job;
            setCurrentJob(thumbnailJob);
            logger.info("offered job = {}", job.toString());
            job.setJobStatus(JobStatus.RUNNING);
            ThumbnailMetadata thumbnailMetadata =
                    handleThumbnailCreation(
                            thumbnailJob.getOriginalFolderPath(),
                            thumbnailJob.getOriginalFilename(),
                            thumbnailJob.getFileId(),
                            job.getUserDTO()
                    );
            logger.info(thumbnailMetadata.toString());
            job.setJobStatus(JobStatus.COMPLETED);
            job.setFinishedOn();
            logger.info("completed job = {}", job.toString());
        } catch (Exception e) {
            logger.error(e.getMessage());
            job.setJobStatus(JobStatus.FAILED);
            logger.warn(job.toString());
            return false;
        }
        return true;
    }

    @Override
    public Job getJob() {
        return this.currentJob;
    }
}
