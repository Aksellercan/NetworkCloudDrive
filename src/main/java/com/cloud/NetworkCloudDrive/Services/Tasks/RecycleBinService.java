package com.cloud.NetworkCloudDrive.Services.Tasks;

import com.cloud.NetworkCloudDrive.Repositories.Services.UserRepository;
import com.cloud.NetworkCloudDrive.Repositories.Tasks.RecycleBinRepository;
import com.cloud.NetworkCloudDrive.Repositories.Tasks.ThumbnailRepository;
import com.cloud.NetworkCloudDrive.Utilities.FileUtility;
import com.cloud.NetworkCloudDrive.Utilities.PathUtility;
import com.cloud.NetworkCloudDrive.Utilities.UserUtility;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class RecycleBinService implements RecycleBinRepository {
    private final UserRepository userRepository;
    private final ThumbnailRepository thumbnailRepository;
    private final PathUtility pathUtility;
    private final FileUtility fileUtility;
    private final UserUtility userUtility;

    public RecycleBinService(
            UserRepository userRepository,
            ThumbnailRepository thumbnailRepository,
            PathUtility pathUtility,
            FileUtility fileUtility,
            UserUtility userUtility) {
        this.userRepository = userRepository;
        this.thumbnailRepository = thumbnailRepository;
        this.pathUtility = pathUtility;
        this.fileUtility = fileUtility;
        this.userUtility = userUtility;
    }

    @Override
    public Map<String, List<?>> getRecyclingList() throws IOException {
        Path userPath = userUtility.returnUserFolderasPath();
        return Map.of();
    }

    private Path generateUUIDFolders() throws IOException {
        UUID uuid = UUID.randomUUID();
        Path recyclePath = pathUtility.getRecycleBinPath();
        return Files.createDirectories(Path.of(recyclePath.toString(), uuid.toString()));
    }
}
