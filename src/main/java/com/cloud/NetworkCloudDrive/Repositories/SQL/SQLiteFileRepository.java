package com.cloud.NetworkCloudDrive.Repositories.SQL;

import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SQLiteFileRepository extends JpaRepository<FileMetadata, Long> {
    List<FileMetadata> searchFileMetadataByName(String name);
    boolean existsFileMetadataByName(String name);
    List<FileMetadata> findAllByUseridAndHasThumbnail(Long userid, boolean hasThumbnail);
    List<FileMetadata> findAllByUseridAndHasThumbnailAndFolderId(Long userid, boolean hasThumbnail, Long folderId);
}
