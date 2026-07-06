package com.cloud.NetworkCloudDrive.Controllers.Maintenance;

import com.cloud.NetworkCloudDrive.Models.Response.JSONResponse;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/health")
public class APIHealthController {
    private final BuildProperties buildProperties;

    public APIHealthController(BuildProperties buildProperties) {
        this.buildProperties = buildProperties;
    }

    @GetMapping(path = "version")
    public ResponseEntity<JSONResponse> getAPIVersion() {
        return new ResponseEntity<>(new JSONResponse(buildProperties.getVersion()), HttpStatus.OK);
    }
}
