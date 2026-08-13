package com.cloud.NetworkCloudDrive.Controllers.Maintenance;

import com.cloud.NetworkCloudDrive.Models.Enum.ScanOptions;
import com.cloud.NetworkCloudDrive.Models.Response.JSONErrorResponse;
import com.cloud.NetworkCloudDrive.Models.Response.JSONObjectResponse;
import com.cloud.NetworkCloudDrive.Repositories.Maintenance.MaintenanceRepository;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "maintenance")
public class MaintenanceController {
    private final MaintenanceRepository maintenanceRepository;

    public MaintenanceController(MaintenanceRepository maintenanceRepository) {
        this.maintenanceRepository = maintenanceRepository;
    }

    @PostMapping(value = "scan", params = "scanOptions", version = "2.0")
    public @ResponseBody ResponseEntity<?> scanDirectory(@RequestParam ScanOptions scanOptions) {
        try {
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                    .body(new JSONObjectResponse(maintenanceRepository.queueScan(0L, scanOptions), "Scan queued successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON)
                    .body(new JSONErrorResponse(e, "Error scanning"));
        }
    }

    @PostMapping(value = "scan", params = "folderid", version = "2.0")
    public @ResponseBody ResponseEntity<?> scanDirectoryNestedFolders(@RequestParam long folderid) {
        try {
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                    .body(new JSONObjectResponse(maintenanceRepository.queueScan(folderid, ScanOptions.NORMAL), "Scan queued successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON)
                    .body(new JSONErrorResponse(e, "Error scanning"));
        }
    }

    @PostMapping(value = "scan", params = {"folderid", "scanOptions"}, version = "2.0")
    public @ResponseBody ResponseEntity<?> scanDirectoryOptions(@RequestParam long folderid, @RequestParam ScanOptions scanOptions) {
        try {
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                    .body(new JSONObjectResponse(maintenanceRepository.queueScan(folderid, scanOptions), "Scan queued successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON)
                    .body(new JSONErrorResponse(e, "Error scanning"));
        }
    }
}
