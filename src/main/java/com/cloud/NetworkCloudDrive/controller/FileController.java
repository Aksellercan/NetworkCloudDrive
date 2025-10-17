package com.cloud.NetworkCloudDrive.controller;

import com.cloud.entity.FileDetails;
import com.cloud.service.FileService;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.File;

@RestController
@RequestMapping(path = "/file")
public class FileController {

    private final FileService fileService = new FileService();

    @PostMapping(value = "/test", produces = MediaType.APPLICATION_JSON_VALUE)
    public String getFile(@RequestBody String name) {
        return fileService.getFile(name).getName();
    }

    @PostMapping(value = "/path", produces = MediaType.APPLICATION_JSON_VALUE)
    public FileDetails getFile(@RequestBody ObjectNode json) {
        return fileService.getFile(json.get("name").asText() , json.get("path").asText());
    }

    @GetMapping("/hello")
    public String HelloWorld() {
        return "Hello, world!";
    }
}
