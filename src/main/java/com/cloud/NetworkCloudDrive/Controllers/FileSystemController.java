package com.cloud.NetworkCloudDrive.Controllers;

import com.cloud.NetworkCloudDrive.Models.DTO.UpdateFileNameDTO;
import com.cloud.NetworkCloudDrive.Models.DTO.UpdateFilePathDTO;
import com.cloud.NetworkCloudDrive.Models.DTO.UpdateFolderNameDTO;
import com.cloud.NetworkCloudDrive.Models.DTO.UpdateFolderPathDTO;
import com.cloud.NetworkCloudDrive.Models.Enum.FilterListEnum;
import com.cloud.NetworkCloudDrive.Models.Enum.SortListEnum;
import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import com.cloud.NetworkCloudDrive.Models.Response.JSONErrorResponse;
import com.cloud.NetworkCloudDrive.Models.Response.JSONMapResponse;
import com.cloud.NetworkCloudDrive.Models.Response.JSONResponse;
import com.cloud.NetworkCloudDrive.Repositories.FileSystemRepository;
import com.cloud.NetworkCloudDrive.Repositories.InformationRepository;
import com.cloud.NetworkCloudDrive.Security.EncodingUtility;
import com.cloud.NetworkCloudDrive.Sessions.UserSession;
import com.cloud.NetworkCloudDrive.Utilities.FileUtility;
import com.cloud.NetworkCloudDrive.Utilities.PathUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.FileSystemException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(path = "/api/filesystem")
public class FileSystemController {
    private final FileSystemRepository fileSystemRepository;
    private final FileUtility fileUtility;
    private final InformationRepository informationRepository;
    private final UserSession userSession;
    private final Logger logger = LoggerFactory.getLogger(FileSystemController.class);
    private final EncodingUtility encodingUtility;
    private final PathUtility pathUtility;

    public FileSystemController(
            InformationRepository informationRepository,
            UserSession userSession,
            FileUtility fileUtility,
            EncodingUtility encodingUtility,
            PathUtility pathUtility,
            FileSystemRepository fileSystemRepository) {
        this.informationRepository = informationRepository;
        this.userSession = userSession;
        this.fileUtility = fileUtility;
        this.encodingUtility = encodingUtility;
        this.pathUtility = pathUtility;
        this.fileSystemRepository = fileSystemRepository;
    }

    @PatchMapping(value = "file/rename", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ResponseEntity<JSONResponse> updateFileName(@RequestBody UpdateFileNameDTO updateFileNameDTO) {
        try {
            FileMetadata oldFile = informationRepository.getFileMetadata(updateFileNameDTO.getFile_id());
            String oldName = oldFile.getName();
            String updatedPath = fileSystemRepository.updateFileName(updateFileNameDTO.getName(), oldFile);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONResponse("Updated file with Id %d from %s to %s. Updated path %s",
                            updateFileNameDTO.getFile_id(), oldName, updateFileNameDTO.getName(), updatedPath));
        } catch (Exception e) {
            logger.error("Cannot update name: {}", e.getMessage());
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONErrorResponse(e, "Failed to update file with Id %d: %s", updateFileNameDTO.getFile_id(), e.getMessage()));
        }
    }

    @PatchMapping(value = "folder/rename", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ResponseEntity<JSONResponse> updateFolderName(@RequestBody UpdateFolderNameDTO updateFolderNameDTO) {
        try {
            FolderMetadata oldFolder = informationRepository.getFolderMetadata(updateFolderNameDTO.getFolder_id());
            String oldName = encodingUtility.decodedBase32SplitArray(oldFolder.getName())[1];
            String updatedPath = fileSystemRepository.updateFolderName(updateFolderNameDTO.getName(), oldFolder);
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
            FolderMetadata folderToMove = informationRepository.getFolderMetadata(updateFolderPathDTO.getFormer_folder_id());
            String oldPath = pathUtility.resolvePathFromIdString(folderToMove.getPath());
            String newPath = fileSystemRepository.moveFolder(folderToMove, updateFolderPathDTO.getDestination_folder_id());
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
            FileMetadata fileToMove = informationRepository.getFileMetadata(updateFilePathDTO.getFile_id());
            String oldPath = (updateFilePathDTO.getFolder_id() > 0 ?
                    pathUtility.resolvePathFromIdString(informationRepository.getFolderMetadata(updateFilePathDTO.getFolder_id()).getPath())
                    :
                    userSession.getName());
            logger.info("old path controller {}", oldPath);
            String newPath = fileSystemRepository.moveFile(fileToMove, updateFilePathDTO.getFolder_id());
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONResponse("Moved file with Id %d from %s to %s", updateFilePathDTO.getFile_id(), oldPath, newPath));
        } catch (Exception e) {
            logger.error("Cannot move name: {}", e.getMessage());
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONErrorResponse(e, "Failed to move file with Id %d: %s", updateFilePathDTO.getFile_id(), e.getMessage()));
        }
    }

    @DeleteMapping(value = "folder/remove", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ResponseEntity<JSONResponse> removeFolder(@RequestParam long folderid) {
        try {
            FolderMetadata folderToRemove = informationRepository.getFolderMetadata(folderid);
            String oldPath = fileSystemRepository.removeFolder(folderToRemove);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONResponse("Folder with Id %d at path %s was successfully removed", folderToRemove.getId(), oldPath));
        } catch (Exception e) {
            logger.error("Cannot remove folder #{}: {}", folderid, e.getMessage());
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONErrorResponse(e, "Failed remove folder with Id %d", folderid));
        }
    }

    @DeleteMapping(value = "file/remove", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ResponseEntity<JSONResponse> removeFile(@RequestParam long fileid) {
        try {
            FileMetadata fileToRemove = informationRepository.getFileMetadata(fileid);
            String oldPath = fileSystemRepository.removeFile(fileToRemove);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONResponse("file with Id %d at path %s was successfully removed", fileToRemove.getId(), oldPath));
        } catch (Exception e) {
            logger.error("Cannot remove file #{}: {}", fileid, e.getMessage());
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONErrorResponse(e, "Failed remove file with Id %d: %s", fileid, e.getMessage()));
        }
    }

    //TODO add pagination max like = 6 items per type (files/folders)
    @GetMapping(value = "list", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody ResponseEntity<?> listFiles(@RequestParam long folderid) {
        try {
            List<Path> fileList = fileUtility.getFileAndFolderPathsFromFolder(pathUtility.getFullPath(pathUtility.getFolderPath(folderid)));
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).
                    body(fileSystemRepository.getListOfMetadataFromPath(fileList));
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

    @GetMapping(value = "list", produces = MediaType.APPLICATION_JSON_VALUE, params = {"folderid", "sortby"})
    public @ResponseBody ResponseEntity<?> listFiles(@RequestParam long folderid, @RequestParam SortListEnum sortby) {
        try {
            List<Path> fileList = fileUtility.getFileAndFolderPathsFromFolder(pathUtility.getFullPath(pathUtility.getFolderPath(folderid)));
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).
                    body(fileSystemRepository.getListOfMetadataFromPath(fileList, sortby));
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

    @GetMapping(value = "list", produces = MediaType.APPLICATION_JSON_VALUE, params = {"folderid", "filterby"})
    public @ResponseBody ResponseEntity<?> listFiles(@RequestParam long folderid, @RequestParam FilterListEnum filterby) {
        try {
            List<Path> fileList = fileUtility.getFileAndFolderPathsFromFolder(pathUtility.getFullPath(pathUtility.getFolderPath(folderid)));
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).
                    body(fileSystemRepository.getListOfMetadataFromPath(fileList, filterby));
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

    //TODO filter and sort
    //TODO filter and sort with keyword/type
    //TODO get type automatically by asking for extension then detect it by tika core
    //TODO I feel like parameters are getting too long, might be a good idea to switch to json to get filter requests

    @GetMapping(value = "list", produces = MediaType.APPLICATION_JSON_VALUE, params = {"folderid", "filterby", "filter"})
    public @ResponseBody ResponseEntity<?> listFiles(@RequestParam long folderid, @RequestParam FilterListEnum filterby, @RequestParam String filter) {
        try {
            List<Path> fileList = fileUtility.getFileAndFolderPathsFromFolder(pathUtility.getFullPath(pathUtility.getFolderPath(folderid)));
            if ((filterby != FilterListEnum.KEYWORD) && (filterby != FilterListEnum.TYPE)) {
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).
                        body(fileSystemRepository.getListOfMetadataFromPath(fileList, filterby));
            }
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).
                    body(fileSystemRepository.getListOfMetadataFromPath(fileList, filterby, filter));
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
