package com.cloud.NetworkCloudDrive.Repositories.Maintenance;

import com.cloud.NetworkCloudDrive.Models.DTO.UserDTO;
import com.cloud.NetworkCloudDrive.Models.Domain.ThumbnailScanResults;
import com.cloud.NetworkCloudDrive.Models.Enum.ScanOptions;
import com.cloud.NetworkCloudDrive.Models.Jobs.Job;
import com.cloud.NetworkCloudDrive.Models.Response.ScanTaskResponse;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.function.Predicate;

@Repository
public interface MaintenanceRepository {
    Object scanOptionsControllerConverter(Job job) throws SQLException;

    //controller for scan options
    Object scanOptionsController(UserDTO userDTO, Path startingDirectory, long folderId, ScanOptions scanOptions) throws SQLException;

    ScanTaskResponse queueScan(long folderId, ScanOptions scanOptions) throws FileSystemException, SQLException;

//    boolean scanDirectory(Path startingPath, Predicate<Path> filter, boolean useRecursion);

    boolean scanDirectory(UserDTO userDTO, Path startingPath, Predicate<Path> filter, boolean useRecursion, boolean createThumbnails);

    void scanAndCreateThumbnails(long startingFolderId, UserDTO userDTO) throws IOException, SQLException;

    ThumbnailScanResults recursiveThumbnailScanInvoker(UserDTO userDTO, long folderId) throws SQLException;
}
