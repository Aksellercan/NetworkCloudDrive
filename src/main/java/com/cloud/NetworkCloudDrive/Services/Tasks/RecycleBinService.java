package com.cloud.NetworkCloudDrive.Services.Tasks;

import com.cloud.NetworkCloudDrive.DAO.SQLiteDAO;
import com.cloud.NetworkCloudDrive.Repositories.Services.UserRepository;
import com.cloud.NetworkCloudDrive.Repositories.Tasks.RecycleBinRepository;
import com.cloud.NetworkCloudDrive.Repositories.Tasks.ThumbnailRepository;
import com.cloud.NetworkCloudDrive.Sessions.UserSession;
import com.cloud.NetworkCloudDrive.Utilities.FileUtility;
import com.cloud.NetworkCloudDrive.Utilities.PathUtility;
import com.cloud.NetworkCloudDrive.Utilities.UserUtility;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class RecycleBinService implements RecycleBinRepository {
    private final UserRepository userRepository;
    private final SQLiteDAO sqLiteDAO;
    private final PathUtility pathUtility;
    private final UserSession userSession;
    private final FileUtility fileUtility;
    private final UserUtility userUtility;

    public RecycleBinService(
            UserRepository userRepository,
            UserSession userSession,
            SQLiteDAO sqLiteDAO,
            PathUtility pathUtility,
            FileUtility fileUtility,
            UserUtility userUtility) {
        this.userRepository = userRepository;
        this.sqLiteDAO = sqLiteDAO;
        this.userSession = userSession;
        this.pathUtility = pathUtility;
        this.fileUtility = fileUtility;
        this.userUtility = userUtility;
    }

    @Override
    public Map<String, List<?>> getRecyclingList() throws IOException {
        Path userPath = userUtility.returnUserFolderasPath();
        return Map.of();
    }

    public Path moveFileToRecycleBin(long fileId) throws IOException, SQLException {
        Path fileToMove =
                Path.of(
                        pathUtility
                                .resolvePathFromIdString(
                                        sqLiteDAO
                                                .getIdPath(
                                                        sqLiteDAO
                                                                .queryFileMetadata(fileId, userSession.getId()).getFolderId())));

        if (!Files.exists(fileToMove)) {
            throw new IOException("File does not exist: " + fileToMove.toString());
        }
        
        Path destination = Path.of(generateUUIDFolders().toString(), fileToMove.toString());

        return Files.move(fileToMove, destination);
    }

    private Path generateUUIDFolders() throws IOException {
        UUID uuid = UUID.randomUUID();
        Path recyclePath = pathUtility.getRecycleBinPath();
        return Files.createDirectories(Path.of(recyclePath.toString(), uuid.toString()));
    }
}
