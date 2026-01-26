package com.cloud.NetworkCloudDrive.Repositories.SQL;

import com.cloud.NetworkCloudDrive.Models.ThumbnailMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SQLiteThumbnailRepository extends JpaRepository<ThumbnailMetadata, Long> {
    List<ThumbnailMetadata> findAllByUserId(long userId);
}
