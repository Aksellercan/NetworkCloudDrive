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

    /**
     * Async function, creates and saves thumbnails to their file location and database table.
     * Uses defaults, If there is already and thumbnail entry for a file then it replaces it by changing thumbnail file name.
     * <p>
     * Saves thumbnails to their folders by dimensions like "horizontal" and "vertical".
     *
     * @param filePath        File path of the file to create thumbnail of
     * @param encodedFileName File's BASE32 encoded file name
     * @param fileId          File's File Metadata ID
     * @param userDTO         General User credentials such as: ID, username and mail. Used as "alternative" for background task runners to access scope specific variables
     * @return Completable future, async function thread can be awaited or its result can be ignored
     * @throws IOException If an I/O error occurs
     */
    @Async
    CompletableFuture<ThumbnailMetadata> createAndSaveThumbnail(Path filePath, String encodedFileName, long fileId, UserDTO userDTO) throws IOException;

    CompletableFuture<ThumbnailMetadata> createAndSaveThumbnailDefaultSettings(Path filePath, String encodedFileName, long fileId) throws IOException;

    /**
     * Deletes all by I/O thumbnails and database whether they exist in database or not. Can be used to fix issues when API returns duplicate entries pointing to same file.
     *
     * @return Deletion Result object which shows how many were successful and failures.
     * @throws IOException If an I/O error occurs
     */
    DeletionResults nuclearDeleteAllThumbnails() throws IOException;

    /**
     * Deletes all by I/O thumbnails whether they exist in database or not.
     *
     * @return Deletion Result object which shows how many were successful and failures.
     * @throws IOException If an I/O error occurs
     */
    DeletionResults deleteOnlyFromIO() throws IOException;

    /**
     * Default delete behaviour, Deletes all thumbnails if they exist in database and filesystem.
     *
     * @return Deletion Result object which shows how many were successful and failures.
     * @throws IOException  If an I/O error occurs
     * @throws SQLException If thumbnail entry doesn't exist or points to same file as another entry
     */
    DeletionResults deleteAllThumbnails() throws IOException, SQLException;

    void deleteThumbnailByThumbnailID(long thumbnailId) throws SQLException, IOException;

    void deleteThumbnailByFileID(long fileId) throws SQLException, IOException;

    Resource getThumbnail(String thumbnailFilename, boolean isPortrait) throws Exception;

    ThumbnailMetadata getThumbnailByFileID(long fileId) throws SQLException;

    ThumbnailMetadata getThumbnailByID(long thumbnailId) throws SQLException;

    /**
     * Wrapper function over delete by fileID function. It deletes thumbnail and switches related File's "has_thumbnail" from true to false.
     *
     * @param fileId File's fileID to delete
     * @throws SQLException If the file metadata entry or thumbnail database entry doesn't exist or an error occurs
     * @throws IOException  If the thumbnail can't be deleted due to I/O error
     */
    void deleteThumbnailByFileIDAndSetThumbnailStatus(long fileId) throws SQLException, IOException;

}
