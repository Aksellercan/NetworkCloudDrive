package com.cloud.NetworkCloudDrive.Controllers;

import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import com.cloud.NetworkCloudDrive.Models.Responses.JSONErrorResponse;
import com.cloud.NetworkCloudDrive.Services.InformationService;
import com.cloud.NetworkCloudDrive.Sessions.UserSession;
import com.cloud.NetworkCloudDrive.Utilities.EncodingUtility;
import com.cloud.NetworkCloudDrive.Utilities.FileUtility;
import com.cloud.NetworkCloudDrive.Utilities.UserUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;

@RestController
@RequestMapping(path = "/api/info")
public class InformationController {
    private final FileUtility fileUtility;
    private final UserSession userSession;
    private final InformationService informationService;
    private final Logger logger = LoggerFactory.getLogger(InformationController.class);
    private final EncodingUtility encodingUtility;
    private final UserUtility userUtility;

    public InformationController(
            FileUtility fileUtility,
            InformationService informationService,
            UserSession userSession,
            EncodingUtility encodingUtility,
            UserUtility userUtility) {
        this.fileUtility = fileUtility;
        this.informationService = informationService;
        this.userSession = userSession;
        this.encodingUtility = encodingUtility;
        this.userUtility = userUtility;
    }

    @GetMapping(value = "get/filemetadata", produces = MediaType.ALL_VALUE)
    public @ResponseBody ResponseEntity<?> getFile(@RequestParam long fileid) {
        try {
            FileMetadata fileMetadata = informationService.getFileMetadata(fileid);
            String decodeName = encodingUtility.decodeBase32StringNoPadding(fileMetadata.getName());
            String[] splitColons = decodeName.split(":");
            fileMetadata.setName(splitColons[1]);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(fileMetadata);
        } catch (Exception e) {
            logger.error("Failed to get file metadata for fileId: {}. {}", fileid, e.getMessage());
            return ResponseEntity.internalServerError().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONErrorResponse(e, "Failed to get file metadata for fileId: %d. %s", fileid, e.getMessage()));
        }
    }

    @GetMapping(value = "get/foldermetadata", produces = MediaType.ALL_VALUE)
    public @ResponseBody ResponseEntity<?> getFolder(@RequestParam long folderid) {
        try {
            FolderMetadata folderMetadata;
            if (folderid != 0) {
                folderMetadata = informationService.getFolderMetadata(folderid);
                folderMetadata.setPath(fileUtility.resolvePathFromIdString(folderMetadata.getPath()));
            } else {
                File folderRootMetadata = userUtility.returnUserFolder();
                folderMetadata = new FolderMetadata(folderRootMetadata.getName(), folderRootMetadata.getPath());
                folderMetadata.setId(folderid);
                folderMetadata.setUserid(userSession.getId());
            }
            String decodeName = encodingUtility.decodeBase32StringNoPadding(folderMetadata.getName());
            String[] splitColons = decodeName.split(":");
            folderMetadata.setName(splitColons[1]);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(folderMetadata);
        } catch (Exception e) {
            logger.error("Failed to get folder metadata for fileId: {}. {}", folderid, e.getMessage());
            return ResponseEntity.internalServerError().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONErrorResponse(e, "Failed to get folder metadata for fileId: %d. %s", folderid, e.getMessage()));
        }
    }
}
