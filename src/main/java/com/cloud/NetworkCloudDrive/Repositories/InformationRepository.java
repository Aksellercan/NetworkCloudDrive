package com.cloud.NetworkCloudDrive.Repositories;

import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import org.springframework.stereotype.Repository;

import java.nio.file.FileSystemException;
import java.sql.SQLException;
import java.util.List;

@Repository
public interface InformationRepository {
    FolderMetadata getFolderMetadataByFolderIdAndName(long folderId, String name, List<Long> skipList) throws FileSystemException, SQLException;

    FileMetadata getFileMetadata(long id) throws Exception;

    FolderMetadata getFolderMetadata(long fileId) throws Exception;
}
