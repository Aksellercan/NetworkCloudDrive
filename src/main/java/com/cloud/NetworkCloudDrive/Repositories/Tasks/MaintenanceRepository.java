package com.cloud.NetworkCloudDrive.Repositories.Tasks;

import org.springframework.stereotype.Repository;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.function.Predicate;

@Repository
public interface MaintenanceRepository {
    boolean scanDirectory(Path startingPath, Predicate<Path> filter, boolean useRecursion);
    void handleFileCheck(File currentFile, long folderId) throws IOException;
    File handleFolderCheck(File currentFolder, long currentFolderId) throws SQLException, IOException;
}
