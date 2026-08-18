package com.cloud.NetworkCloudDrive.Controllers;

import com.cloud.NetworkCloudDrive.Models.DTO.CreateFolderDTO;
import com.cloud.NetworkCloudDrive.Models.Enum.UploadOptions;
import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import com.cloud.NetworkCloudDrive.Models.Response.JSONErrorResponse;
import com.cloud.NetworkCloudDrive.Repositories.FileRepository;
import com.cloud.NetworkCloudDrive.Repositories.InformationRepository;
import com.cloud.NetworkCloudDrive.Security.EncodingUtility;
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
@RequestMapping(path = "file")
public class FileController {
    private final FileRepository fileRepository;
    private final InformationRepository informationRepository;
    private final Logger logger = LoggerFactory.getLogger(FileController.class);
    private final EncodingUtility encodingUtility;
    private final PathUtility pathUtility;

    public FileController(
            InformationRepository informationRepository,
            EncodingUtility encodingUtility,
            PathUtility pathUtility,
            FileRepository fileRepository) {
        this.informationRepository = informationRepository;
        this.encodingUtility = encodingUtility;
        this.pathUtility = pathUtility;
        this.fileRepository = fileRepository;
    }

    @PostMapping("upload")
    public ResponseEntity<?> uploadFile(@RequestParam MultipartFile[] files, @RequestParam long folderid) {
        try {
            if (files.length == 0)
                throw new NullPointerException("No files provided");
            String folderPath = pathUtility.getFolderPath(folderid);
            return ResponseEntity.ok().body(fileRepository.uploadFiles(files, folderPath, folderid));
        } catch (FileAlreadyExistsException fileAlreadyExistsException) {
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

    @PostMapping(value = "upload", params = {"files", "folderid", "options"})
    public ResponseEntity<?> uploadFile(@RequestParam MultipartFile[] files, @RequestParam long folderid, @RequestParam UploadOptions options) {
        try {
            if (files.length == 0)
                throw new NullPointerException("No files provided");
            String folderPath = pathUtility.getFolderPath(folderid);
            return ResponseEntity.ok().body(fileRepository.uploadFiles(files, folderPath, folderid));
        } catch (FileAlreadyExistsException fileAlreadyExistsException) {
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
            FileMetadata metadata = informationRepository.getFileMetadata(fileid);
            String actualPath = pathUtility.getFolderPath(metadata.getFolderId());
            Resource file = fileRepository.getFile(metadata, actualPath).get();
            return ResponseEntity.ok().
                    header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + metadata.getName() + "\" ")
                    .contentType(MediaType.parseMediaType(metadata.getMimiType()))
                    .contentLength(metadata.getSize())
                    .body(file);
        } catch (FileSystemException fse) {
            logger.error("Internal error occurred. {}", fse.getMessage());
            return ResponseEntity.internalServerError().body(new JSONErrorResponse(fse, "Internal error occurred"));
        } catch (Exception e) {
            logger.error("Failed to download file. {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    new JSONErrorResponse(e, "Failed to download file"));
        }
    }

    @PostMapping(value = "create/folder")
    public ResponseEntity<?> createFolder(@RequestBody CreateFolderDTO folderDTO) {
        try {
            FolderMetadata folderMetadata = fileRepository.createFolder(folderDTO.getName(), folderDTO.getFolder_id());
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
