package com.cloud.NetworkCloudDrive.Repositories.Tasks;

import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@Repository
public interface RecycleBinRepository {
    Map<String, List<?>> getRecyclingList() throws IOException;
}
