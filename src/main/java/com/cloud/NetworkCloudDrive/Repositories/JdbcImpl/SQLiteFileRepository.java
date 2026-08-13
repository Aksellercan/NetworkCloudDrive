package com.cloud.NetworkCloudDrive.Repositories.JdbcImpl;

import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SQLiteFileRepository extends JpaRepository<FileMetadata, Long> {
    List<FileMetadata> searchFileMetadataByName(String name);

    boolean existsFileMetadataByName(String name);

    List<FileMetadata> findAllByUseridAndHasThumbnail(Long userid, boolean hasThumbnail);

    List<FileMetadata> findAllByUseridAndHasThumbnailAndFolderId(Long userid, boolean hasThumbnail, Long folderId);

    List<FileMetadata> findAllByUserid(Long userid);

    void deleteAllByUserid(Long userid);

    Page<FileMetadata> findAllByUseridAndLastUpdatedNotNullOrderByLastUpdatedDesc(long userId, Pageable pageable);
}
