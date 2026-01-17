package com.cloud.NetworkCloudDrive.Controllers;

import com.cloud.NetworkCloudDrive.Models.Responses.JSONErrorResponse;
import com.cloud.NetworkCloudDrive.Models.Responses.JSONResponse;
import com.cloud.NetworkCloudDrive.Services.MaintenanceService;
import com.cloud.NetworkCloudDrive.Utilities.FileUtility;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;

@RestController
@RequestMapping(path = "/api/maintenance")
public class MaintenanceController {
    private final MaintenanceService maintenanceService;
    private final FileUtility fileUtility;

    public MaintenanceController(MaintenanceService maintenanceService, FileUtility fileUtility) {
        this.maintenanceService = maintenanceService;
        this.fileUtility = fileUtility;
    }

    @GetMapping("scan")
    public @ResponseBody ResponseEntity<?> scanDirectory(@RequestParam long folderid) {
        try {
            maintenanceService.scanFoldersAndFiles(folderid);
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                    .body(new JSONResponse("scan completed"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON)
                    .body(new JSONErrorResponse(e, "error scanning"));
        }
    }

    @GetMapping("better-scan")
    public @ResponseBody ResponseEntity<?> betterScanDirectory(@RequestParam long folderid) {
        try {
            if (!maintenanceService.betterSearch(new File(fileUtility.getFolderPath(folderid))))
                throw new RuntimeException("Scan stopped");
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                    .body(new JSONResponse("scan completed"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON)
                    .body(new JSONErrorResponse(e, "error scanning"));
        }
    }
}
