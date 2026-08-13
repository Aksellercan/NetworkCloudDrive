package com.cloud.NetworkCloudDrive.Controllers.Filesystem.List;

import com.cloud.NetworkCloudDrive.Models.Enum.FilterListEnum;
import com.cloud.NetworkCloudDrive.Models.Enum.SortListEnum;
import com.cloud.NetworkCloudDrive.Models.Response.JSONErrorResponse;
import com.cloud.NetworkCloudDrive.Repositories.FileSystemRepository;
import com.cloud.NetworkCloudDrive.Utilities.FileUtility;
import com.cloud.NetworkCloudDrive.Utilities.PathUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.FileSystemException;
import java.nio.file.Path;
import java.util.List;

@RestController
@RequestMapping(value = "filesystem/list")
public class ListController {
    private final FileUtility fileUtility;
    private final PathUtility pathUtility;
    private final FileSystemRepository fileSystemRepository;
    private final Logger logger = LoggerFactory.getLogger(ListController.class);

    public ListController(FileUtility fileUtility, PathUtility pathUtility, FileSystemRepository fileSystemRepository) {
        this.fileUtility = fileUtility;
        this.pathUtility = pathUtility;
        this.fileSystemRepository = fileSystemRepository;
    }

    //TODO add pagination max like = 6 items per type (files/folders)
    @GetMapping(version = "1.0")
    public @ResponseBody ResponseEntity<?> listFiles(@RequestParam long folderid) {
        try {
            List<Path> fileList = fileUtility.getFileAndFolderPathsFromFolder(pathUtility.getFullPath(pathUtility.getFolderPath(folderid)));
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).
                    body(fileSystemRepository.getListOfMetadataFromPath(fileList));
        } catch (FileSystemException fileSystemException) {
            logger.error("Some folders couldn't be found at folder with Id {}, reason: {}", folderid, fileSystemException.getMessage());
            return ResponseEntity.internalServerError().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONErrorResponse(fileSystemException, "Some folders couldn't be found at folder with Id %d", folderid));
        } catch (Exception e) {
            logger.error("Failed to list items in folder with Id {}, reason: {}", folderid, e.getMessage());
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body(
                    new JSONErrorResponse(e, "Failed to list items inside folder with Id %d", folderid));
        }
    }

    @GetMapping(params = {"folderid", "sortby"}, version = "1.0")
    public @ResponseBody ResponseEntity<?> listFiles(@RequestParam long folderid, @RequestParam SortListEnum sortby) {
        try {
            List<Path> fileList = fileUtility.getFileAndFolderPathsFromFolder(pathUtility.getFullPath(pathUtility.getFolderPath(folderid)));
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).
                    body(fileSystemRepository.getListOfMetadataFromPath(fileList, sortby));
        } catch (FileSystemException fileSystemException) {
            logger.error("Some folders couldn't be found at folder with Id {}, reason: {}", folderid, fileSystemException.getMessage());
            return ResponseEntity.internalServerError().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONErrorResponse(fileSystemException, "Some folders couldn't be found at folder with Id %d", folderid));
        } catch (Exception e) {
            logger.error("Failed to list items in folder with Id {}, reason: {}", folderid, e.getMessage());
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body(
                    new JSONErrorResponse(e, "Failed to list items inside folder with Id %d", folderid));
        }
    }

    @GetMapping(params = {"folderid", "page", "size"}, version = "2.0")
    public ResponseEntity<?> listFiles(long folderid, int page, int size) {
        try {
            List<Path> fileList = fileUtility.getFileAndFolderPathsFromFolder(pathUtility.getFullPath(pathUtility.getFolderPath(folderid)));
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).
                    body(fileSystemRepository.getListOfMetadataFromPath(fileList));
        } catch (FileSystemException fileSystemException) {
            logger.error("Some folders couldn't be found at folder with Id {}, reason: {}", folderid, fileSystemException.getMessage());
            return ResponseEntity.internalServerError().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONErrorResponse(fileSystemException, "Some folders couldn't be found at folder with Id %d", folderid));
        } catch (Exception e) {
            logger.error("Failed to list items in folder with Id {}, reason: {}", folderid, e.getMessage());
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body(
                    new JSONErrorResponse(e, "Failed to list items inside folder with Id %d", folderid));
        }
    }

    @GetMapping(params = {"folderid", "filterby"}, version = "1.0")
    public @ResponseBody ResponseEntity<?> listFiles(@RequestParam long folderid, @RequestParam FilterListEnum filterby) {
        try {
            List<Path> fileList = fileUtility.getFileAndFolderPathsFromFolder(pathUtility.getFullPath(pathUtility.getFolderPath(folderid)));
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).
                    body(fileSystemRepository.getListOfMetadataFromPath(fileList, filterby));
        } catch (FileSystemException fileSystemException) {
            logger.error("Some folders couldn't be found at folder with Id {}, reason: {}", folderid, fileSystemException.getMessage());
            return ResponseEntity.internalServerError().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONErrorResponse(fileSystemException, "Some folders couldn't be found at folder with Id %d", folderid));
        } catch (Exception e) {
            logger.error("Failed to list items in folder with Id {}, reason: {}", folderid, e.getMessage());
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body(
                    new JSONErrorResponse(e, "Failed to list items inside folder with Id %d", folderid));
        }
    }

    //DONE filter and sort
    //TODO filter and sort with keyword/type
    //TODO get type automatically by asking for extension then detect it by tika core
    //TODO I feel like parameters are getting too long, might be a good idea to switch to json to get filter requests

    @GetMapping(params = {"folderid", "filterby", "filter"}, version = "1.0")
    public @ResponseBody ResponseEntity<?> listFiles(@RequestParam long folderid, @RequestParam FilterListEnum filterby, @RequestParam String filter) {
        try {
            List<Path> fileList = fileUtility.getFileAndFolderPathsFromFolder(pathUtility.getFullPath(pathUtility.getFolderPath(folderid)));
            if ((filterby != FilterListEnum.KEYWORD) && (filterby != FilterListEnum.TYPE)) {
                return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).
                        body(fileSystemRepository.getListOfMetadataFromPath(fileList, filterby));
            }
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).
                    body(fileSystemRepository.getListOfMetadataFromPath(fileList, filterby, filter));
        } catch (FileSystemException fileSystemException) {
            logger.error("Some folders couldn't be found at folder with Id {}, reason: {}", folderid, fileSystemException.getMessage());
            return ResponseEntity.internalServerError().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONErrorResponse(fileSystemException, "Some folders couldn't be found at folder with Id %d", folderid));
        } catch (Exception e) {
            logger.error("Failed to list items in folder with Id {}, reason: {}", folderid, e.getMessage());
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).body(
                    new JSONErrorResponse(e, "Failed to list items inside folder with Id %d", folderid));
        }
    }
}
