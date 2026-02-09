package com.cloud.NetworkCloudDrive.Controllers.Tasks;

import com.cloud.NetworkCloudDrive.Models.Responses.JSONErrorResponse;
import com.cloud.NetworkCloudDrive.Models.ThumbnailMetadata;
import com.cloud.NetworkCloudDrive.Services.Tasks.ThumbnailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.FileSystemException;

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
                    new JSONErrorResponse(e, "Failed to thumbnail"));
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
                    new JSONErrorResponse(e, "Failed to thumbnail"));
        }
    }
}
