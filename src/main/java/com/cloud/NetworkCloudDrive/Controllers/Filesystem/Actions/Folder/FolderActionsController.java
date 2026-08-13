package com.cloud.NetworkCloudDrive.Controllers.Filesystem.Actions.Folder;

import com.cloud.NetworkCloudDrive.Models.DTO.UpdateFolderNameDTO;
import com.cloud.NetworkCloudDrive.Models.DTO.UpdateFolderPathDTO;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import com.cloud.NetworkCloudDrive.Models.Response.JSONErrorResponse;
import com.cloud.NetworkCloudDrive.Models.Response.JSONMapResponse;
import com.cloud.NetworkCloudDrive.Models.Response.JSONResponse;
import com.cloud.NetworkCloudDrive.Repositories.FileSystemRepository;
import com.cloud.NetworkCloudDrive.Repositories.InformationRepository;
import com.cloud.NetworkCloudDrive.Security.EncodingUtility;
import com.cloud.NetworkCloudDrive.Utilities.PathUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping(value = "filesystem/folder")
public class FolderActionsController {
    private final Logger logger = LoggerFactory.getLogger(FolderActionsController.class);
    private final InformationRepository informationRepository;
    private final EncodingUtility encodingUtility;
    private final FileSystemRepository fileSystemRepository;
    private final PathUtility pathUtility;

    public FolderActionsController(InformationRepository informationRepository, EncodingUtility encodingUtility, FileSystemRepository fileSystemRepository, PathUtility pathUtility) {
        this.informationRepository = informationRepository;
        this.encodingUtility = encodingUtility;
        this.fileSystemRepository = fileSystemRepository;
        this.pathUtility = pathUtility;
    }

    @PatchMapping(value = "folder/rename")
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

    @PostMapping(value = "folder/move")
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

    @DeleteMapping(value = "folder/remove")
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
}
