package com.cloud.NetworkCloudDrive.Repositories.SQL;

import com.cloud.NetworkCloudDrive.Models.ThumbnailMetadata;
import org.apache.el.stream.Stream;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SQLiteThumbnailRepository extends JpaRepository<ThumbnailMetadata, Long> {
    List<ThumbnailMetadata> findAllByUserId(long userId);
    Optional<ThumbnailMetadata> findByFileId(long fileId);
}
