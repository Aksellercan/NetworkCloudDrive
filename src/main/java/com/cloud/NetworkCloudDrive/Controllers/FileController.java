package com.cloud.NetworkCloudDrive.Controllers;

import com.cloud.NetworkCloudDrive.Models.DTO.CreateFolderDTO;
import com.cloud.NetworkCloudDrive.Models.*;
import com.cloud.NetworkCloudDrive.Models.Responses.JSONErrorResponse;
import com.cloud.NetworkCloudDrive.Services.FileService;
import com.cloud.NetworkCloudDrive.Services.InformationService;
import com.cloud.NetworkCloudDrive.Utilities.EncodingUtility;
import com.cloud.NetworkCloudDrive.Utilities.PathUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemException;
import java.sql.SQLException;

@RestController
@RequestMapping(path = "/api/file")
public class FileController {
    private final FileService fileService;
    private final InformationService informationService;
    private final Logger logger = LoggerFactory.getLogger(FileController.class);
    private final EncodingUtility encodingUtility;
    private final PathUtility pathUtility;

    public FileController(
            FileService fileService,
            InformationService informationService,
            EncodingUtility encodingUtility,
            PathUtility pathUtility) {
        this.fileService = fileService;
        this.informationService = informationService;
        this.encodingUtility = encodingUtility;
        this.pathUtility = pathUtility;
    }

    @PostMapping("upload")
    public ResponseEntity<?> uploadFile(@RequestParam MultipartFile[] files, @RequestParam long folderid) {
        try {
            if (files.length == 0)
                throw new NullPointerException("No file is provided");
            String folderPath = pathUtility.getFolderPath(folderid);
            return ResponseEntity.ok().body(fileService.uploadFiles(files, folderPath, folderid));
        } catch(FileAlreadyExistsException fileAlreadyExistsException) {
            logger.error("File already exists at destination {}", fileAlreadyExistsException.getMessage());
            return ResponseEntity.badRequest().body(new JSONErrorResponse(fileAlreadyExistsException));
        } catch (SQLException sqlException) {
            logger.error("SQL error occurred {}", sqlException.getMessage());
            return ResponseEntity.internalServerError().body(new JSONErrorResponse(sqlException, "SQL error occurred"));
        } catch (Exception e) {
            logger.error("Failed to upload file. {}", e.getMessage());
            return ResponseEntity.internalServerError().body(new JSONErrorResponse(e, "Failed to upload file"));
        }
    }

    @GetMapping("download")
    public ResponseEntity<?> downloadFile(@RequestParam long fileid) {
        try {
            FileMetadata metadata = informationService.getFileMetadata(fileid);
            String actualPath = pathUtility.getFolderPath(metadata.getFolderId());
            String decodedFileName = encodingUtility.decodedBase32SplitArray(metadata.getName())[1];
            logger.info("path requested {}", actualPath);
            Resource file = fileService.getFile(metadata, actualPath);
            return ResponseEntity.ok().
                    header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + decodedFileName + "\" ").
                            contentType(MediaType.parseMediaType(metadata.getMimiType())).
                    contentLength(metadata.getSize()).body(file);
        }
        catch (FileSystemException fse) {
            logger.error("Internal error occurred. {}", fse.getMessage());
            return ResponseEntity.internalServerError().body(new JSONErrorResponse(fse, "Internal error occurred"));
        }
        catch (Exception e) {
            logger.error("Failed to download file. {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    new JSONErrorResponse(e, "Failed to download file"));
        }
    }

    @PostMapping(value = "create/folder", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> createFolder(@RequestBody CreateFolderDTO folderDTO) {
        try {
            FolderMetadata folderMetadata = fileService.createFolder(folderDTO.getName(), folderDTO.getFolder_id());
            folderMetadata.setPath(pathUtility.resolvePathFromIdString(folderMetadata.getPath()));
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(folderMetadata);
        } catch (FileAlreadyExistsException fae) {
            logger.error("Folder with name {} already exists. {}", folderDTO.getName(), fae.getMessage());
            return ResponseEntity.badRequest().body(new JSONErrorResponse(fae));
        } catch (SecurityException e) {
            logger.error("Path is not allowed: {}. {}", folderDTO.getName(), e.getMessage());
            return ResponseEntity.internalServerError().body(new JSONErrorResponse(e, "Security Error"));
        } catch (Exception e) {
            logger.error("Error creating folder with name: {}. {}", folderDTO.getName(), e.getMessage());
            return ResponseEntity.internalServerError().body(new JSONErrorResponse(e, "IO Error"));
        }
    }
}
