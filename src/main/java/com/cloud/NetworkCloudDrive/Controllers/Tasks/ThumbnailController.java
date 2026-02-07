package com.cloud.NetworkCloudDrive.Controllers.Tasks;

import com.cloud.NetworkCloudDrive.Services.Tasks.ThumbnailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/thumbnails")
public class ThumbnailController {
    public final Logger logger = LoggerFactory.getLogger(ThumbnailController.class);
    private final ThumbnailService thumbnailService;

    public ThumbnailController(ThumbnailService thumbnailService) {
        this.thumbnailService = thumbnailService;
    }

//    @GetMapping("get")
//    public @ResponseBody ResponseEntity<?> getThumbnailByID(@RequestParam long thumbId) {
//        try {
//            thumbnailService.getThumbnail()
//        } catch (Exception e) {
//            logger.error("Thumbnail Controller {}", e.getMessage());
//        }
//    }

}
