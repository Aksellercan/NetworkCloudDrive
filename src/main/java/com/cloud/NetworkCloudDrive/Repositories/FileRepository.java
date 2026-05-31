package com.cloud.NetworkCloudDrive.Repositories;

import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Repository;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.Map;

@Repository
public interface FileRepository {
    Map<String, ?> uploadFiles(MultipartFile[] files, String folderPath, long folderId) throws Exception;

    Path storeFile(InputStream inputStream, String fileName, String parentPath) throws IOException;

    Resource getFile(FileMetadata file, String path) throws Exception;

    FolderMetadata createFolder(String folderName, long folderId) throws Exception;
}
