package com.cloud.NetworkCloudDrive.Controllers.Filesystem.Actions.File;

import com.cloud.NetworkCloudDrive.Models.DTO.UpdateFileNameDTO;
import com.cloud.NetworkCloudDrive.Models.DTO.UpdateFilePathDTO;
import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.Response.JSONErrorResponse;
import com.cloud.NetworkCloudDrive.Models.Response.JSONResponse;
import com.cloud.NetworkCloudDrive.Repositories.FileSystemRepository;
import com.cloud.NetworkCloudDrive.Repositories.InformationRepository;
import com.cloud.NetworkCloudDrive.Sessions.UserSession;
import com.cloud.NetworkCloudDrive.Utilities.PathUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(value = "filesystem/actions/file")
public class FileActionsController {
    private final Logger logger = LoggerFactory.getLogger(FileActionsController.class);
    private final InformationRepository informationRepository;
    private final FileSystemRepository fileSystemRepository;
    private final PathUtility pathUtility;
    private final UserSession userSession;

    public FileActionsController(InformationRepository informationRepository, FileSystemRepository fileSystemRepository, PathUtility pathUtility, UserSession userSession) {
        this.informationRepository = informationRepository;
        this.fileSystemRepository = fileSystemRepository;
        this.pathUtility = pathUtility;
        this.userSession = userSession;
    }

    @PatchMapping(value = "rename")
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

    @PostMapping(value = "move")
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

    @DeleteMapping(value = "remove", produces = MediaType.APPLICATION_JSON_VALUE)
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
}
