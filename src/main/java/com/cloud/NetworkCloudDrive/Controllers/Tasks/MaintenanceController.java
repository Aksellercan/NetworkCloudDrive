package com.cloud.NetworkCloudDrive.Controllers.Tasks;

import com.cloud.NetworkCloudDrive.Models.Enum.ScanOptions;
import com.cloud.NetworkCloudDrive.Models.Responses.JSONErrorResponse;
import com.cloud.NetworkCloudDrive.Models.Responses.JSONObjectResponse;
import com.cloud.NetworkCloudDrive.Models.Responses.JSONResponse;
import com.cloud.NetworkCloudDrive.Services.Tasks.MaintenanceService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(path = "/api/maintenance")
public class MaintenanceController {
    private final MaintenanceService maintenanceService;

    public MaintenanceController(MaintenanceService maintenanceService) {
        this.maintenanceService = maintenanceService;
    }

    @PostMapping(value = "scan", params = "scanOptions")
    public @ResponseBody ResponseEntity<?> scanDirectory(@RequestParam ScanOptions scanOptions) {
        try {
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                    .body(new JSONObjectResponse(maintenanceService.scanOptionsController(0L, scanOptions), "Scan completed"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON)
                    .body(new JSONErrorResponse(e, "Error scanning"));
        }
    }

    @PostMapping(value = "scan", params = "folderid")
    public @ResponseBody ResponseEntity<?> scanDirectoryNestedFolders(@RequestParam long folderid) {
        try {
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                    .body(new JSONObjectResponse(maintenanceService.scanOptionsController(folderid, ScanOptions.NORMAL), "Scan completed"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON)
                    .body(new JSONErrorResponse(e, "Error scanning"));
        }
    }

    @PostMapping(value = "scan", params = {"folderid", "scanOptions"})
    public @ResponseBody ResponseEntity<?> scanDirectoryOptions(@RequestParam long folderid, @RequestParam ScanOptions scanOptions) {
        try {
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON)
                    .body(new JSONObjectResponse(maintenanceService.scanOptionsController(folderid, scanOptions), "Scan completed"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON)
                    .body(new JSONErrorResponse(e, "Error scanning"));
        }
    }
}
