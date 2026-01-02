package com.cloud.NetworkCloudDrive.Controllers;

import com.cloud.NetworkCloudDrive.Enum.ScanOptions;
import com.cloud.NetworkCloudDrive.Models.Responses.JSONErrorResponse;
import com.cloud.NetworkCloudDrive.Models.Responses.JSONResponse;
import com.cloud.NetworkCloudDrive.Services.MaintenanceService;
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
    private final MaintenanceService maintenanceService;

    public MaintenanceController(FileUtility fileUtility, MaintenanceService maintenanceService) {
        this.fileUtility = fileUtility;
        this.maintenanceService = maintenanceService;
    }

    @GetMapping("scan")
    public @ResponseBody ResponseEntity<?> scanDirectory(@RequestParam long folderid, @RequestParam ScanOptions scanOptions) {
        try {
            maintenanceService.scanFoldersAndFiles(folderid, scanOptions);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                    .body(new JSONResponse("scan completed"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON)
                    .body(new JSONErrorResponse(e, "error scanning"));
        }
    }
}
