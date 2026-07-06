package com.cloud.NetworkCloudDrive.Repositories.Maintenance;

import com.cloud.NetworkCloudDrive.Models.DTO.UserDTO;
import com.cloud.NetworkCloudDrive.Models.Domain.DeletionResults;
import com.cloud.NetworkCloudDrive.Models.ThumbnailMetadata;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.concurrent.CompletableFuture;

@Repository
public interface ThumbnailRepository {
    @Async
    CompletableFuture<ThumbnailMetadata> createAndSaveThumbnail(Path filePath, String encodedFileName, long fileId, UserDTO userDTO) throws IOException;

    CompletableFuture<ThumbnailMetadata> createAndSaveThumbnailDefaultSettings(Path filePath, String encodedFileName, long fileId) throws IOException;

    DeletionResults nuclearDeleteAllThumbnails() throws IOException;

    DeletionResults deleteOnlyFromIO() throws IOException;

    DeletionResults deleteAllThumbnails() throws IOException, SQLException;

    void deleteThumbnailByThumbnailID(long thumbnailId) throws SQLException, IOException;

    void deleteThumbnailByFileID(long fileId) throws SQLException, IOException;

    Resource getThumbnail(String thumbnailFilename, boolean isPortrait) throws Exception;

    ThumbnailMetadata getThumbnailByFileID(long fileId) throws SQLException;

    ThumbnailMetadata getThumbnailByID(long thumbnailId) throws SQLException;

    void deleteThumbnailByFileIDAndSetThumbnailStatus(long fileId) throws SQLException, IOException;

}
