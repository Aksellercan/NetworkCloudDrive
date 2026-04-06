package com.cloud.NetworkCloudDrive.Controllers.Tasks;

import com.cloud.NetworkCloudDrive.Models.Responses.JSONErrorResponse;
import com.cloud.NetworkCloudDrive.Models.Responses.JSONResponse;
import com.cloud.NetworkCloudDrive.Models.ThumbnailMetadata;
import com.cloud.NetworkCloudDrive.Services.Tasks.ThumbnailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.FileNotFoundException;
import java.nio.file.FileSystemException;
import java.sql.SQLException;

@RestController
@RequestMapping(path = "/api/thumbnails")
public class ThumbnailController {
    public final Logger logger = LoggerFactory.getLogger(ThumbnailController.class);
    private final ThumbnailService thumbnailService;

    public ThumbnailController(ThumbnailService thumbnailService) {
        this.thumbnailService = thumbnailService;
    }

    @GetMapping("get")
    public @ResponseBody ResponseEntity<?> getThumbnailByID(@RequestParam long thumbId) {
        try {
            ThumbnailMetadata thumbnailMetadata = thumbnailService.getThumbnailByID(thumbId);
            Resource file = thumbnailService.getThumbnail(thumbnailMetadata.getFileName(), thumbnailMetadata.isPortrait());
            return ResponseEntity.ok().
                    header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + thumbnailMetadata.getFileName() + "\" ").
                    contentType(MediaType.parseMediaType(thumbnailMetadata.getMimeType())).
                    contentLength(thumbnailMetadata.getSize()).body(file);
        } catch (FileSystemException fse) {
            logger.error("Internal error occurred. {}", fse.getMessage());
            return ResponseEntity.internalServerError().body(new JSONErrorResponse(fse, "Internal error occurred"));
        } catch (Exception e) {
            logger.error("Thumbnail Controller {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    new JSONErrorResponse(e, "Failed to get thumbnail"));
        }
    }

    @GetMapping("getbyfileid")
    public @ResponseBody ResponseEntity<?> getThumbnailByFileID(@RequestParam long fileId) {
        try {
            ThumbnailMetadata thumbnailMetadata = thumbnailService.getThumbnailByFileID(fileId);
            Resource file = thumbnailService.getThumbnail(thumbnailMetadata.getFileName(), thumbnailMetadata.isPortrait());
            return ResponseEntity.ok().
                    header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + thumbnailMetadata.getFileName() + "\" ").
                    contentType(MediaType.parseMediaType(thumbnailMetadata.getMimeType())).
                    contentLength(thumbnailMetadata.getSize()).body(file);
        } catch (FileSystemException fse) {
            logger.error("Internal error occurred. {}", fse.getMessage());
            return ResponseEntity.internalServerError().body(new JSONErrorResponse(fse, "Internal error occurred"));
        } catch (Exception e) {
            logger.error("Thumbnail Controller {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    new JSONErrorResponse(e, "Failed to get thumbnail"));
        }
    }

    @DeleteMapping("delete")
    public @ResponseBody ResponseEntity<?> deleteThumbnailByID(@RequestParam long thumbId) {
        try {
            thumbnailService.deleteThumbnailByThumbnailID(thumbId);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONResponse("Thumbnail with Id %d was successfully removed", thumbId));
        } catch (FileNotFoundException fnf) {
            logger.error("Internal error occurred. {}", fnf.getMessage());
            return ResponseEntity.internalServerError().body(new JSONErrorResponse(fnf, "Internal error occurred"));
        } catch (SQLException sql) {
            logger.error("Internal error occurred. {}", sql.getMessage());
            return ResponseEntity.internalServerError().body(new JSONErrorResponse(sql, "Thumbnail with ID %d does not exists in database", thumbId));
        } catch (Exception e) {
            logger.error("Thumbnail Controller {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    new JSONErrorResponse(e, "Failed to delete thumbnail"));
        }
    }

    @DeleteMapping("deleteall")
    public @ResponseBody ResponseEntity<?> deleteAllThumbnails() {
        try {
            thumbnailService.deleteAllThumbnails();
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONResponse("Removed all thumbnails"));
        } catch (FileNotFoundException fnf) {
            logger.error("Internal error occurred. {}", fnf.getMessage());
            return ResponseEntity.internalServerError().body(new JSONErrorResponse(fnf, "Internal error occurred"));
        } catch (Exception e) {
            logger.error("Thumbnail Controller {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    new JSONErrorResponse(e, "Failed to delete thumbnail"));
        }
    }

    @DeleteMapping("deletebyfileid")
    public @ResponseBody ResponseEntity<?> deleteThumbnailByFileID(@RequestParam long fileId) {
        try {
            thumbnailService.deleteThumbnailByFileIDAndSetThumbnailStatus(fileId);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONResponse("Thumbnail with related to File Id %d was successfully removed", fileId));
        } catch (FileNotFoundException fnf) {
            logger.error("Internal error occurred. {}", fnf.getMessage());
            return ResponseEntity.internalServerError().body(new JSONErrorResponse(fnf, "Internal error occurred"));
        } catch (SQLException sql) {
            logger.error("Internal error occurred. {}", sql.getMessage());
            return ResponseEntity.internalServerError().body(new JSONErrorResponse(sql, "No thumbnail related to File ID %d was found in database", fileId));
        } catch (Exception e) {
            logger.error("Thumbnail Controller {}", e.getMessage());
            return ResponseEntity.internalServerError().body(
                    new JSONErrorResponse(e, "Failed to delete thumbnail"));
        }
    }
}
