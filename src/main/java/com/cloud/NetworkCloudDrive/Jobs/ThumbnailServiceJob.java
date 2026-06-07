package com.cloud.NetworkCloudDrive.Jobs;

import com.cloud.NetworkCloudDrive.Models.FileMetadata;
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

@Component
public class ThumbnailServiceJob {
    private final ThumbnailRepository thumbnailRepository;
    private final Logger logger = LoggerFactory.getLogger(ThumbnailServiceJob.class);
    private final SQLiteDAO sQLiteDAO;
    private final UserSession userSession;

    public ThumbnailServiceJob(ThumbnailRepository thumbnailRepository, SQLiteDAO sQLiteDAO, UserSession userSession) {
        this.thumbnailRepository = thumbnailRepository;
        this.sQLiteDAO = sQLiteDAO;
        this.userSession = userSession;
    }

    public ThumbnailMetadata handleThumbnailCreation(Path originalFolderPath, String originalFilename, long fileId) throws IOException, SQLException, ExecutionException, InterruptedException {
        ThumbnailMetadata thumbnail = thumbnailRepository.createAndSaveThumbnailDefaultSettings(originalFolderPath, originalFilename, fileId).get();
        updateFileMetadata(fileId, true);
        return thumbnail;
    }

    private void updateFileMetadata(long fileId, boolean setThumbnail) throws SQLException {
        FileMetadata fileMetadata = sQLiteDAO.queryFileMetadata(fileId, userSession.getId());
        if (fileMetadata == null) {
            return;
        }
        fileMetadata.setHasThumbnail(setThumbnail);
        sQLiteDAO.saveFile(fileMetadata);
    }
}
