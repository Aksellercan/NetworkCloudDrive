package com.cloud.NetworkCloudDrive.Services;

import com.cloud.NetworkCloudDrive.Models.DTO.FileListItemDTO;
import com.cloud.NetworkCloudDrive.Models.DTO.FolderListItemDTO;
import com.cloud.NetworkCloudDrive.Models.Enum.SortListEnum;
import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import com.cloud.NetworkCloudDrive.Repositories.Services.FileSystemRepository;
import com.cloud.NetworkCloudDrive.Services.Tasks.ThumbnailService;
import com.cloud.NetworkCloudDrive.Sessions.UserSession;
import com.cloud.NetworkCloudDrive.Utilities.Security.EncodingUtility;
import com.cloud.NetworkCloudDrive.Utilities.FileUtility;
import com.cloud.NetworkCloudDrive.DAO.SQLiteDAO;
import com.cloud.NetworkCloudDrive.Utilities.PathUtility;
import com.cloud.NetworkCloudDrive.Utilities.UserUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.*;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Stream;

@Service
public class FileSystemService implements FileSystemRepository {
    private final FileUtility fileUtility;
    private final UserSession userSession;
    private final SQLiteDAO sqLiteDAO;
    private final Logger logger = LoggerFactory.getLogger(FileSystemService.class);
    private final EncodingUtility encodingUtility;
    private final UserUtility userUtility;
    private final PathUtility pathUtility;
    private final ThumbnailService thumbnailService;

    public FileSystemService(
            UserSession userSession,
            FileUtility fileUtility,
            SQLiteDAO sqLiteDAO,
            EncodingUtility encodingUtility,
            UserUtility userUtility,
            PathUtility pathUtility, ThumbnailService thumbnailService) {
        this.userSession = userSession;
        this.fileUtility = fileUtility;
        this.sqLiteDAO = sqLiteDAO;
        this.encodingUtility = encodingUtility;
        this.userUtility = userUtility;
        this.pathUtility = pathUtility;
        this.thumbnailService = thumbnailService;
    }

    @Override
    public Map<String, List<?>> getListOfMetadataFromPath(List<Path> filePaths, SortListEnum sortListEnum) throws FileSystemException, SQLException {
        List<FileListItemDTO> fileList = new LinkedList<>();
        List<FolderListItemDTO> folderList = new LinkedList<>();
        for (Path file : filePaths) {
            //ignore dotfiles
            if (fileUtility.isIgnoredFile(file.getFileName().toString())) {
                logger.debug("file skip {}", file.getFileName());
                continue;
            }
            logger.debug("file/folder in queue {}", file);
            String[] arrayString = encodingUtility.decodedBase32SplitArray(file.getFileName().toString());
            long actualFileId = Long.parseLong(arrayString[0]);
            String actualFileName = arrayString[1];
            if (Files.isRegularFile(file)) {
                FileMetadata foundFile = sqLiteDAO.queryFileMetadata(actualFileId, userSession.getId());
                FileListItemDTO fileListItemDTO = new FileListItemDTO(foundFile);
                fileListItemDTO.setName(actualFileName);
                fileList.add(fileListItemDTO);
                continue;
            }
            FolderMetadata foundFolderMetadata = sqLiteDAO.queryFolderMetadata(actualFileId, userSession.getId());
            FolderListItemDTO folderListItemDTO = new FolderListItemDTO(foundFolderMetadata);
            folderListItemDTO.setName(actualFileName);
            folderList.add(folderListItemDTO);
        }
        logger.debug("Sorted by: {}", sortListEnum.name());
        return sortFileList(sortListEnum, fileList.stream(), folderList.stream());
    }

    private Map<String, List<?>> sortFileList(SortListEnum sortListEnum, Stream<FileListItemDTO> fileList, Stream<FolderListItemDTO> folderList) {
        Comparator<FileListItemDTO> fileListItemDTOComparator;
        Comparator<FolderListItemDTO> folderListItemDTOComparator;
        switch (sortListEnum) {
            case ALPHABETIC:
                fileListItemDTOComparator = Comparator.comparing(f -> f.getName().toLowerCase());
                folderListItemDTOComparator = Comparator.comparing(fl -> fl.getName().toLowerCase());
                break;
            case REVERSE_ALPHABETIC:
                fileListItemDTOComparator = Comparator.comparing(f -> f.getName().toLowerCase(), Comparator.reverseOrder());
                folderListItemDTOComparator = Comparator.comparing(fl -> fl.getName().toLowerCase(), Comparator.reverseOrder());
                break;
            case NEWEST:
                fileListItemDTOComparator = Comparator.comparing(FileListItemDTO::getCreatedAt, Comparator.reverseOrder());
                folderListItemDTOComparator = Comparator.comparing(FolderListItemDTO::getCreatedAt, Comparator.reverseOrder());
                break;
            case OLDEST:
                fileListItemDTOComparator = Comparator.comparing(FileListItemDTO::getCreatedAt);
                folderListItemDTOComparator = Comparator.comparing(FolderListItemDTO::getCreatedAt);
                break;
            case FOLDERS_FIRST:
                LinkedHashMap<String, List<?>> linkedHashMap = new LinkedHashMap<>();
                linkedHashMap.put("folders", folderList.toList());
                linkedHashMap.put("files", fileList.toList());
                return linkedHashMap;
            default:
                return Map.of(
                        "files", fileList.toList(),
                        "folders", folderList.toList()
                );
        }
        return Map.of(
                "files", fileList.sorted(fileListItemDTOComparator).toList(),
                "folders", folderList.sorted(folderListItemDTOComparator).toList()
        );
    }

    @Override
    public String removeFile(FileMetadata file) throws Exception {
        //find folder
        Path checkExists = fileUtility.returnPathIfItExists(Paths.get(
                pathUtility.getFolderPath(file.getFolderId()), file.getName()).toString());
        //remove Folder
        // use nio instead
        if (!Files.deleteIfExists(checkExists))
            throw new FileSystemException(String.format("Failed to remove folder at path %s\n", checkExists));
        sqLiteDAO.deleteFile(file);
        thumbnailService.deleteThumbnailByFileID(file.getId());
        return checkExists.toString();
    }

    @Override
    public String removeFolder(FolderMetadata folder) throws IOException {
        String pathToRemove = pathUtility.resolvePathFromIdString(folder.getPath());
        logger.info("pathToRemove = {}", pathToRemove);
        //find folder
        Path checkExists = fileUtility.returnPathIfItExists(pathToRemove);
        //remove Folder
        deleteFsTree(checkExists, folder.getPath());
        if (!Files.deleteIfExists(checkExists))
            throw new IOException("Failed to remove parent folder");
        sqLiteDAO.deleteFolder(folder);
        return checkExists.toString();
    }

    //TODO instead of generating Id paths use startsWith from DAO and filter files by found folders id's then delete them both from db and system
    private void deleteFsTree(Path dir, String startingIdPath) throws IOException {
        logger.info("Start File Tree deletion operation");
        long errorCount = 0;
        List<Path> fileTreeStream = fileUtility.walkFsTree(dir, true);
        for (Path file : fileTreeStream) {
            if (file.getParent().equals(userUtility.returnUserFolderasPath())) {
                logger.debug("Skipped base path");
                continue;
            }
            if (!Files.exists(file)) {
                errorCount++;
                continue;
            }
            if (Files.exists(file) && Files.isRegularFile(file)) {
                String parentFolderIdPath = pathUtility.generateIdPaths(file.getParent().toString(), startingIdPath);
                logger.debug("generated file path: {}", parentFolderIdPath);
                FolderMetadata folderMetadata =
                        sqLiteDAO.getFolderMetadataFromIdPathAndName(parentFolderIdPath, file.getParent().getFileName().toString(), userSession.getId());
                FileMetadata output = sqLiteDAO.getFileMetadataByFolderIdNameAndUserId(folderMetadata.getId(), file.getFileName().toString(), userSession.getId());
                if (!Files.deleteIfExists(file)) {
                    errorCount++;
                    continue;
                }
                sqLiteDAO.deleteFile(sqLiteDAO.getFileMetadataByFolderIdNameAndUserId(folderMetadata.getId(), file.getFileName().toString(), userSession.getId()));
                logger.debug("File metadata: name {} path {} Id {}", output.getName(), output.getFolderId(), output.getId());
                continue;
            }
            String parentFolderIdPath = pathUtility.generateIdPaths(file.toString(), startingIdPath);
            logger.debug("generated folder path: {}", parentFolderIdPath);
            FolderMetadata folderMetadata = sqLiteDAO.getFolderMetadataFromIdPathAndName(parentFolderIdPath, file.getFileName().toString(), userSession.getId());
            // manage folders here
            if (!Files.deleteIfExists(file)) {
                errorCount++;
                continue;
            }
            sqLiteDAO.deleteFolder(folderMetadata);
            //check if it's correct
            logger.debug("Folder metadata: name {} path {} Id {}", folderMetadata.getName(), folderMetadata.getPath(), folderMetadata.getId());
        }
        if (errorCount == 0)
            logger.info("Completed file tree deletion operation. Error count {}", errorCount);
        else
            logger.warn("Completed file tree deletion operation with some errors. Error count {}", errorCount);
    }

    @Override
    public String updateFolderName(String newName, FolderMetadata folder) throws Exception {
        //find file
        Path checkExists = fileUtility.returnPathIfItExists(pathUtility.resolvePathFromIdString(folder.getPath()));
        //check duplicate
        if (fileUtility.checkIfFileExistsDecodeNames(pathUtility.returnParentFolderPathFromFolderID(folder.getId()), newName))
            throw new FileAlreadyExistsException(String.format("Folder with name %s already exists", newName));
        // Encode newName in BASE32
        String encodeBase32FolderName = encodingUtility.encodeBase32FolderName(folder.getId(), newName, folder.getUserid());
        //rename file
        Path renamedFolder =
                fileUtility.returnPathIfItsNotADuplicate(Paths.get(checkExists.getParent().toString(), encodeBase32FolderName).toString());
        logger.info("estimated path: {}", renamedFolder);
        Path newUpdatedPath = Files.move(checkExists, renamedFolder);
        if (Files.exists(newUpdatedPath)) {
            //set new name and path
            folder.setName(encodeBase32FolderName);
            //save
            sqLiteDAO.saveFolder(folder);
            logger.info("Renamed folder full path: {}", renamedFolder);
        } else {
            throw new FileSystemException(String.format("Failed to rename the folder to %s", newName));
        }
        return renamedFolder.toString();
    }

    @Override
    public String updateFileName(String newName, FileMetadata file) throws Exception {
        String folderPath = pathUtility.getFullPathToString(pathUtility.getFolderPath(file.getFolderId()));
        //find file
        Path checkExists = Paths.get(folderPath, file.getName());
        if (!Files.exists(checkExists, LinkOption.NOFOLLOW_LINKS))
            throw new FileNotFoundException("File not found");
        // Encode newName in BASE32
        if (!fileUtility.hasFileExtension(newName)) {
            String decodeOldFileName = encodingUtility.decodedBase32SplitArray(file.getName())[1];
            //save extension
            String oldExtension = fileUtility.getFileExtension(decodeOldFileName);
            newName = newName + oldExtension;
        }
        String encodeBase32FolderName = encodingUtility.encodeBase32FolderName(file.getId(), newName, file.getUserid());
        //rename file
        Path renamedFile = Paths.get(folderPath, encodeBase32FolderName);
        if (fileUtility.checkIfFileExistsDecodeNames(pathUtility.getFolderPath(file.getFolderId()), newName))
            throw new FileAlreadyExistsException(String.format("File with name %s already exists", newName));
        // Perform movement
        Path newUpdatedPath = Files.move(checkExists, renamedFile);
        if (!Files.exists(newUpdatedPath))
            throw new FileSystemException(String.format("Failed to rename the file to %s", renamedFile.getFileName()));
        // get ready for transaction
        // mimetype has bug in the library (cant detect types such as YAML)
        String newMimeType = fileUtility.getMimeTypeFromExtensionUsingTikaCore(newUpdatedPath.toFile()); /* <- get new mimetype of file */
        //set new name and path
        file.setName(encodeBase32FolderName);
        file.setMimiType(newMimeType != null ? newMimeType : file.getMimiType());
        //save
        sqLiteDAO.saveFile(file);
        logger.info("Renamed file full path: {}", renamedFile);
        return renamedFile.toString();
    }

    @Override
    public String moveFile(FileMetadata targetFile, long folderId) throws Exception {
        String destinationFolder = pathUtility.getFullPathToString(pathUtility.getFolderPath(folderId));
        String currentFolder = pathUtility.getFolderPath(targetFile.getFolderId());
        String newPath = Paths.get(destinationFolder, targetFile.getName()).toString();
        logger.info("new file path = {}", newPath);
        //find file
        String oldPath = Paths.get(pathUtility.getBasePathToString(), currentFolder, targetFile.getName()).toString();
        logger.info("old path service {}", oldPath);
        Path checkExists = Path.of(oldPath);
        Path checkDestinationExists = Path.of(destinationFolder);
        if (!Files.exists(checkExists, LinkOption.NOFOLLOW_LINKS))
            throw new FileNotFoundException(String.format("File does not exist with name %s at path %s", targetFile.getName(), oldPath));
        if (!Files.exists(checkDestinationExists))
            throw new FileNotFoundException(String.format("Destination folder does not exist at path %s", checkDestinationExists));
        Path updatedPath = Path.of(newPath);
        Path movedFile = Files.move(checkExists, updatedPath);
        if (!Files.exists(movedFile))
            throw new FileSystemException(
                    String.format("Failed to move file with name %s from %s to %s", targetFile.getName(), oldPath, newPath));
        //set new name and path
        targetFile.setFolderId(folderId);
        //save
        sqLiteDAO.saveFile(targetFile);
        return checkDestinationExists.toString();
    }

    /**
     * Updates List of Folder Metadata's ID paths with prefix
     *
     * @param folderList list of Folder Metadata
     * @param oldPrefix  old prefix to replace
     * @param newPrefix  new prefix to replace old prefix with
     * @return updated Folder Metadata List
     */
    private List<FolderMetadata> updateFolderIdPaths(List<FolderMetadata> folderList, String oldPrefix, String newPrefix) {
        List<FolderMetadata> result = new ArrayList<>();
        for (FolderMetadata folderMetadata : folderList) {
            folderMetadata.setPath(folderMetadata.getPath().replaceAll(oldPrefix, newPrefix));
        }
        return result;
    }

    /**
     * <p>Moves folder(s) to new location.</p>
     *
     * <p>How it works:</p>
     * Generates Folder ID path if the target is 0 and the source is at 0/1/4/2 then it will be 0/2
     * preceding source will be 0/1/4 if target is 0/5/9 then it will be 0/5/9/2 and contents will be 0/5/9/2/x
     *
     * @param folder              source folder metadata
     * @param destinationFolderId destination folder id
     * @return updated path
     * @throws Exception throws FileSystemException and FileNotFoundException
     */
    @Override
    public String moveFolder(FolderMetadata folder, long destinationFolderId) throws Exception {
        String sourcePath = pathUtility.getFolderPath(folder.getId());
        logger.warn("source path {}", sourcePath);
        // check if source folder exists
        Path sourceFolder = fileUtility.returnPathIfItExists(sourcePath);
        logger.warn("sourcefolder path {}", sourceFolder);
        // check if destination folder exists
        Path destinationFolder = fileUtility.returnPathIfItExists(pathUtility.getFolderPath(destinationFolderId));
        logger.warn("destinationfolder path {}", destinationFolder);
        // Get folders inside source folder
        logger.warn("prefix {}", folder.getPath() + "/");
        List<FolderMetadata> folderMetadataList = sqLiteDAO.findAllStartsWithIdPath(folder.getPath() + "/");
        // Update ID paths of folders affected
        folderMetadataList = updateFolderIdPaths(folderMetadataList, folder.getPath(),
                sqLiteDAO.getIdPath(destinationFolderId) + "/" + folder.getId());
        // Update ID path of source folder individually
        folder.setPath(
                folder.getPath().replaceAll(folder.getPath(), sqLiteDAO.getIdPath(destinationFolderId) + "/" + folder.getId()));
        // Move folder in system
        Path updatedPath = Files.move(sourceFolder, Paths.get(destinationFolder.toString(), folder.getName()));
        if (Files.notExists(updatedPath))
            throw new FileSystemException(String.format("Failed to move the folder from %s to %s", sourcePath, updatedPath));
        // Save changes
        sqLiteDAO.saveAllFolders(folderMetadataList);
        // return new path
        return updatedPath.toString();
    }
}
