package com.cloud.NetworkCloudDrive.Repositories;

import com.cloud.NetworkCloudDrive.Models.Enum.FilterListEnum;
import com.cloud.NetworkCloudDrive.Models.Enum.SortListEnum;
import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.FileSystemException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Repository
public interface FileSystemRepository {
    String removeFile(FileMetadata file) throws Exception;

    String removeFolder(FolderMetadata folder) throws IOException;

    String updateFolderName(String newName, FolderMetadata folder) throws Exception;

    String updateFileName(String newName, FileMetadata file) throws Exception;

    /**
     * <p>Moves folder(s) to new location.</p>
     *
     * <p>How it works:</p>
     * Generates Folder ID path if the target is 0 and the source is at 0/1/4/2 then it will be 0/2
     * original source will be 0/1/4 if target is 0/5/9 then it will be 0/5/9/2 and contents will be 0/5/9/2/x
     *
     * @param folder              source folder metadata
     * @param destinationFolderId destination folder id
     * @return updated path
     * @throws Exception throws FileSystemException and FileNotFoundException
     */
    String moveFolder(FolderMetadata folder, long destinationFolderId) throws Exception;

    String moveFile(FileMetadata targetFile, long folderId) throws Exception;

    // Filters and Sorting
    Map<String, List<?>> getListOfMetadataFromPath(List<Path> filePaths, SortListEnum sortListEnum) throws FileSystemException, SQLException;

    Map<String, List<?>> getListOfMetadataFromPath(List<Path> filePaths, FilterListEnum filterListEnum) throws SQLException;

    Map<String, List<?>> getListOfMetadataFromPath(List<Path> filePaths, FilterListEnum filterListEnum, String filterCase) throws SQLException;

    Map<String, List<?>> collectAllRecents();

    Map<String, List<?>> collectAllRecentsPageable(Pageable pageable);

    Map<String, List<?>> getListOfMetadataFromPath(List<Path> filePaths) throws FileSystemException, SQLException;
}
