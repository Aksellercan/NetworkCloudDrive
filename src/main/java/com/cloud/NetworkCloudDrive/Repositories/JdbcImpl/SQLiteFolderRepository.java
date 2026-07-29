package com.cloud.NetworkCloudDrive.Repositories.JdbcImpl;

import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SQLiteFolderRepository extends JpaRepository<FolderMetadata, Long> {
    List<FolderMetadata> findAllByPathContainsIgnoreCase(String path);

    boolean existsFolderMetadataByName(String name);

    List<FolderMetadata> findAllByUserid(Long userid);

    void deleteAllByUserid(Long userid);
}
