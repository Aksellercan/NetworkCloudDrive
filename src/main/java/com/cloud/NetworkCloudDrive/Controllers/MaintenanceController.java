package com.cloud.NetworkCloudDrive.Controllers;

import com.cloud.NetworkCloudDrive.Enum.ScanOptions;
import com.cloud.NetworkCloudDrive.Models.Responses.JSONErrorResponse;
import com.cloud.NetworkCloudDrive.Models.Responses.JSONResponse;
import com.cloud.NetworkCloudDrive.Properties.FileStorageProperties;
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
    private final FileStorageProperties fileStorageProperties;

    public MaintenanceController(MaintenanceService maintenanceService, FileUtility fileUtility, FileStorageProperties fileStorageProperties) {
        this.maintenanceService = maintenanceService;
        this.fileUtility = fileUtility;
        this.fileStorageProperties = fileStorageProperties;
    }

    @GetMapping(value = "scan", params = "folderid")
    public @ResponseBody ResponseEntity<?> scanDirectoryNestedFolders(@RequestParam long folderid) {
        try {
            if (!maintenanceService.betterScan(new File(fileStorageProperties.getFullPath(fileUtility.getFolderPath(folderid)))))
                throw new RuntimeException("Scan stopped");
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                    .body(new JSONResponse("Scan completed"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON)
                    .body(new JSONErrorResponse(e, "Error scanning"));
        }
    }

    @GetMapping(value = "scan", params = {"folderid", "scanOptions"})
    public @ResponseBody ResponseEntity<?> scanDirectoryOptions(@RequestParam long folderid, @RequestParam ScanOptions scanOptions) {
        try {
            maintenanceService.betterScan(new File(fileStorageProperties.getFullPath(fileUtility.getFolderPath(folderid))));
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                    .body(new JSONResponse("scan completed"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON)
                    .body(new JSONErrorResponse(e, "error scanning"));
        }
    }
}
