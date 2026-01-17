package com.cloud.NetworkCloudDrive.Controllers;

import com.cloud.NetworkCloudDrive.Models.*;
import com.cloud.NetworkCloudDrive.Models.DTO.UpdateFileNameDTO;
import com.cloud.NetworkCloudDrive.Models.DTO.UpdateFilePathDTO;
import com.cloud.NetworkCloudDrive.Models.DTO.UpdateFolderNameDTO;
import com.cloud.NetworkCloudDrive.Models.DTO.UpdateFolderPathDTO;
import com.cloud.NetworkCloudDrive.Models.Responses.JSONErrorResponse;
import com.cloud.NetworkCloudDrive.Models.Responses.JSONMapResponse;
import com.cloud.NetworkCloudDrive.Models.Responses.JSONResponse;
import com.cloud.NetworkCloudDrive.Properties.FileStorageProperties;
import com.cloud.NetworkCloudDrive.Services.FileSystemService;
import com.cloud.NetworkCloudDrive.Services.InformationService;
import com.cloud.NetworkCloudDrive.Sessions.UserSession;
import com.cloud.NetworkCloudDrive.Utilities.EncodingUtility;
import com.cloud.NetworkCloudDrive.Utilities.FileUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.nio.file.FileSystemException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/api/filesystem")
public class FileSystemController {
    private final FileSystemService fileSystemService;
    private final FileUtility fileUtility;
    private final InformationService informationService;
    private final UserSession userSession;
    private final Logger logger = LoggerFactory.getLogger(FileSystemController.class);
    private final EncodingUtility encodingUtility;
    private final FileStorageProperties fileStorageProperties;

    public FileSystemController(
            FileSystemService fileSystemService,
            InformationService informationService,
            UserSession userSession,
            FileUtility fileUtility, EncodingUtility encodingUtility, FileStorageProperties fileStorageProperties) {
        this.fileSystemService = fileSystemService;
        this.informationService = informationService;
        this.userSession = userSession;
        this.fileUtility = fileUtility;
        this.encodingUtility = encodingUtility;
        this.fileStorageProperties = fileStorageProperties;
    }

    @PostMapping(value = "file/rename", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ResponseEntity<JSONResponse> updateFileName(@RequestBody UpdateFileNameDTO updateFileNameDTO) {
        try {
            FileMetadata oldFile = informationService.getFileMetadata(updateFileNameDTO.getFile_id());
            String oldName = oldFile.getName();
            String updatedPath = fileSystemService.updateFileName(updateFileNameDTO.getName(), oldFile);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONResponse("Updated file with Id %d from %s to %s. Updated path %s",
                            updateFileNameDTO.getFile_id(), oldName, updateFileNameDTO.getName(), updatedPath));
        } catch (Exception e) {
            logger.error("Cannot update name: {}", e.getMessage());
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONErrorResponse(e, "Failed to update file with Id %d: %s", updateFileNameDTO.getFile_id(), e.getMessage()));
        }
    }

    @PostMapping(value = "folder/rename", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ResponseEntity<JSONResponse> updateFolderName(@RequestBody UpdateFolderNameDTO updateFolderNameDTO) {
        try {
            FolderMetadata oldFolder = informationService.getFolderMetadata(updateFolderNameDTO.getFolder_id());
            String oldName = encodingUtility.decodedBase32SplitArray(oldFolder.getName())[1];
            String updatedPath = fileSystemService.updateFolderName(updateFolderNameDTO.getName(), oldFolder);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONResponse("Updated folder name with Id %d from %s to %s. Updated path %s",
                            updateFolderNameDTO.getFolder_id(), oldName, updateFolderNameDTO.getName(), updatedPath));
        } catch (Exception e) {
            logger.error("Cannot update folder name. {}", e.getMessage());
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONErrorResponse(e,
                            "Failed to update folder with Id %d: %s", updateFolderNameDTO.getFolder_id(), e.getMessage()));
        }
    }

    @PostMapping(value = "folder/move", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ResponseEntity<JSONResponse> moveFile(@RequestBody UpdateFolderPathDTO updateFolderPathDTO) {
        try {
            FolderMetadata folderToMove = informationService.getFolderMetadata(updateFolderPathDTO.getFormer_folder_id());
            String oldPath = fileUtility.resolvePathFromIdString(folderToMove.getPath());
            String newPath = fileSystemService.moveFolder(folderToMove, updateFolderPathDTO.getDestination_folder_id());
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONMapResponse(
                            Map.of("old_path", oldPath, "new_path", newPath),
                            "Successfully moved folder with Id %d", updateFolderPathDTO.getFormer_folder_id()));
        } catch (Exception e) {
            logger.error("Cannot move folder. {}", e.getMessage());
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONErrorResponse(e,
                            "Failed to move folder with Id %d: %s", updateFolderPathDTO.getFormer_folder_id(), e.getMessage()));
        }
    }

    @PostMapping(value = "file/move", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ResponseEntity<JSONResponse> moveFile(@RequestBody UpdateFilePathDTO updateFilePathDTO) {
        try {
            FileMetadata fileToMove = informationService.getFileMetadata(updateFilePathDTO.getFile_id());
            String oldPath = (updateFilePathDTO.getFolder_id() != 0 ?
                    fileUtility.resolvePathFromIdString(informationService.getFolderMetadata(updateFilePathDTO.getFolder_id()).getPath())
                    :
                    userSession.getName());
            logger.info("old path controller {}", oldPath);
            String newPath = fileSystemService.moveFile(fileToMove, updateFilePathDTO.getFolder_id());
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONResponse("Moved file with Id %d from %s to %s", updateFilePathDTO.getFile_id(), oldPath, newPath));
        } catch (Exception e) {
            logger.error("Cannot move name: {}", e.getMessage());
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONErrorResponse(e, "Failed to move file with Id %d: %s", updateFilePathDTO.getFile_id(), e.getMessage()));
        }
    }

    @PostMapping(value = "folder/remove", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ResponseEntity<JSONResponse> removeFolder(@RequestParam long folderid) {
        try {
            FolderMetadata folderToRemove = informationService.getFolderMetadata(folderid);
            String oldPath = fileSystemService.removeFolder(folderToRemove);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONResponse("Folder with Id %d at path %s was successfully removed", folderToRemove.getId(), oldPath));
        } catch (Exception e) {
            logger.error("Cannot remove folder #{}: {}", folderid, e.getMessage());
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONErrorResponse(e, "Failed remove folder with Id %d", folderid));
        }
    }

    @PostMapping(value = "file/remove", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ResponseEntity<JSONResponse> removeFile(@RequestParam long fileid) {
        try {
            FileMetadata fileToRemove = informationService.getFileMetadata(fileid);
            String oldPath = fileSystemService.removeFile(fileToRemove);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONResponse("file with Id %d at path %s was successfully removed", fileToRemove.getId(), oldPath));
        } catch (Exception e) {
            logger.error("Cannot remove file #{}: {}", fileid, e.getMessage());
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONErrorResponse(e, "Failed remove file with Id %d: %s", fileid, e.getMessage()));
        }
    }

    //TODO add pagination max like = 12 items
    @GetMapping(value = "list", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ResponseEntity<?> listFiles(@RequestParam long folderid) {
        try {
            List<Path> fileList = fileUtility.getFileAndFolderPathsFromFolder(new File(fileStorageProperties.getFullPath(fileUtility.getFolderPath(folderid))));
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).
                    body(fileSystemService.getListOfMetadataFromPath(fileList));
        } catch (FileSystemException fileSystemException) {
            logger.error("Some folders couldn't be found at folder with Id {}, reason: {}", folderid, fileSystemException.getMessage());
            return ResponseEntity.internalServerError().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONErrorResponse(fileSystemException, "Some folders couldn't be found at folder with Id %d", folderid));
        } catch (Exception e) {
            logger.error("Failed to list items in folder with Id {}, reason: {}", folderid, e.getMessage());
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body(
                    new JSONErrorResponse(e, "Failed to list items inside folder with Id %d", folderid));
        }
    }
}
