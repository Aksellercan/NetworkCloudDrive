package com.cloud.NetworkCloudDrive.Repositories.Tasks;

import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.function.Predicate;

@Repository
public interface MaintenanceRepository {
    boolean scanDirectory(Path startingPath, Predicate<Path> filter, boolean useRecursion);

    boolean scanDirectory(Path startingPath, Predicate<Path> filter, boolean useRecursion, boolean createThumbnails);

    void scanAndCreateThumbnails(long startingFolderId) throws IOException, SQLException;
}
