package com.cloud.NetworkCloudDrive.Services;

import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import com.cloud.NetworkCloudDrive.Persistence.SQLiteDAO;
import com.cloud.NetworkCloudDrive.Repositories.InformationRepository;
import com.cloud.NetworkCloudDrive.Sessions.UserSession;
import com.cloud.NetworkCloudDrive.Utilities.FileUtility;
import com.cloud.NetworkCloudDrive.Utilities.PathUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.time.Instant;
import java.util.List;

@Service
public class InformationService implements InformationRepository {
    private final Logger logger = LoggerFactory.getLogger(InformationService.class);
    private final FileUtility fileUtility;
    private final SQLiteDAO sqLiteDAO;
    private final UserSession userSession;
    private final PathUtility pathUtility;


    public InformationService(FileUtility fileUtility, SQLiteDAO sqLiteDAO, UserSession userSession, PathUtility pathUtility) {
        this.fileUtility = fileUtility;
        this.userSession = userSession;
        this.sqLiteDAO = sqLiteDAO;
        this.pathUtility = pathUtility;
    }

    @Transactional
    @Override
    public FolderMetadata getFolderMetadataByFolderIdAndName(long folderId, String name, List<Long> skipList)
            throws FileSystemException {
        String idPath = sqLiteDAO.getIdPath(folderId, userSession.getId());
        List<FolderMetadata> findAllByPathList = sqLiteDAO.findAllContainingSectionOfIdPathIgnoreCase(idPath, userSession.getId());
        if (findAllByPathList.isEmpty())
            throw new FileSystemException("Can't resolve path");
        String[] splitOriginalPath = idPath.split("/");
        int originalPathLength = splitOriginalPath.length;
        FolderMetadata returnFolder = new FolderMetadata();
        for (FolderMetadata folderMetadata : findAllByPathList) {
            if (skipList.contains(folderMetadata.getId()))
                continue;
            String[] splitBySlash = folderMetadata.getPath().split("/");
            if ((splitBySlash.length > originalPathLength) && (splitBySlash.length < originalPathLength + 2)) {
                returnFolder = folderMetadata;
                break;
            }
        }
        return returnFolder;
    }

    @Override
    public FileMetadata getFileMetadata(long id) throws FileNotFoundException, FileSystemException {
        FileMetadata retrievedFile = sqLiteDAO.queryFileMetadata(id, userSession.getId());
        retrievedFile.setLastUpdated(Instant.now());
        File fileCheck = fileUtility.returnFileIfItExists(
                pathUtility.getFolderPath(retrievedFile.getFolderId()) + File.separator + retrievedFile.getName());
        retrievedFile.setSize(fileCheck.length()); //bytes
        return retrievedFile;
    }

    @Override
    public FolderMetadata getFolderMetadata(long folderId) throws IOException {
        FolderMetadata folder = sqLiteDAO.queryFolderMetadata(folderId, userSession.getId());
        folder.setLastUpdated(Instant.now());
        File getFolder = fileUtility.returnFileIfItExists(pathUtility.resolvePathFromIdString(folder.getPath()));
        logger.debug("Folder: Id: {} Path: {}", folderId, getFolder);
        return folder;
    }
}
