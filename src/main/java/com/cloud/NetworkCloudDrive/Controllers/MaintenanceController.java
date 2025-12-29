package com.cloud.NetworkCloudDrive.Controllers;

import com.cloud.NetworkCloudDrive.Models.JSONErrorResponse;
import com.cloud.NetworkCloudDrive.Models.JSONResponse;
import com.cloud.NetworkCloudDrive.Utilities.FileUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/maintenance")
public class MaintenanceController {
    private final Logger logger = LoggerFactory.getLogger(MaintenanceController.class);
    private final FileUtility fileUtility;

    public MaintenanceController(FileUtility fileUtility) {
        this.fileUtility = fileUtility;
    }

    @GetMapping("scan")
    public @ResponseBody ResponseEntity<?> scanDirectory(@RequestParam long folderid) {
        try {
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(new JSONResponse(fileUtility.traverseFileTree(folderid)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON)
                    .body(new JSONErrorResponse(e, "error scanning"));
        }
    }
}
