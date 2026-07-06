package com.cloud.NetworkCloudDrive.Controllers;

import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import com.cloud.NetworkCloudDrive.Models.Response.JSONErrorResponse;
import com.cloud.NetworkCloudDrive.Repositories.InformationRepository;
import com.cloud.NetworkCloudDrive.Security.EncodingUtility;
import com.cloud.NetworkCloudDrive.Sessions.UserSession;
import com.cloud.NetworkCloudDrive.Utilities.PathUtility;
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
    private final UserSession userSession;
    private final InformationRepository informationRepository;
    private final Logger logger = LoggerFactory.getLogger(InformationController.class);
    private final EncodingUtility encodingUtility;
    private final UserUtility userUtility;
    private final PathUtility pathUtility;

    public InformationController(
            InformationRepository informationRepository,
            UserSession userSession,
            EncodingUtility encodingUtility,
            UserUtility userUtility,
            PathUtility pathUtility) {
        this.informationRepository = informationRepository;
        this.userSession = userSession;
        this.encodingUtility = encodingUtility;
        this.userUtility = userUtility;
        this.pathUtility = pathUtility;
    }

    @GetMapping(value = "get/filemetadata", produces = MediaType.ALL_VALUE)
    public @ResponseBody ResponseEntity<?> getFile(@RequestParam long fileid) {
        try {
            FileMetadata fileMetadata = informationRepository.getFileMetadata(fileid);
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
            if (folderid > 0) {
                folderMetadata = informationRepository.getFolderMetadata(folderid);
                folderMetadata.setPath(pathUtility.resolvePathFromIdString(folderMetadata.getPath()));
            } else {
                File folderRootMetadata = userUtility.returnUserFolder();
                folderMetadata = new FolderMetadata(folderRootMetadata.getName(), folderRootMetadata.getPath());
                folderMetadata.setId(0L);
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
