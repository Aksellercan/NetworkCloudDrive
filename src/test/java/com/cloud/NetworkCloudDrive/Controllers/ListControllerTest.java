package com.cloud.NetworkCloudDrive.Controllers;

import com.cloud.NetworkCloudDrive.Controllers.Filesystem.List.ListController;
import com.cloud.NetworkCloudDrive.Repositories.FileSystemRepository;
import com.cloud.NetworkCloudDrive.Utilities.FileUtility;
import com.cloud.NetworkCloudDrive.Utilities.PathUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListControllerTest {

    @Mock
    private FileUtility fileUtility;
    @Mock
    private PathUtility pathUtility;
    @Mock
    private FileSystemRepository fileSystemRepository;

    private ListController controller;

    @BeforeEach
    void setUp() {
        controller = new ListController(fileUtility, pathUtility, fileSystemRepository);
    }

    @Test
    void listFiles_ReturnsOk() throws Exception {
        when(pathUtility.getFolderPath(0L)).thenReturn("user_folder");
        when(pathUtility.getFullPath("user_folder")).thenReturn(Path.of("/root/user_folder"));
        when(fileUtility.getFileAndFolderPathsFromFolder(any(Path.class))).thenReturn(List.of());
        when(fileSystemRepository.getListOfMetadataFromPath(anyList())).thenReturn(Map.of("files", List.of(), "folders", List.of()));

        ResponseEntity<?> result = controller.listFiles(0L);

        assertEquals(200, result.getStatusCode().value());
    }

    @Test
    void listFiles_WithSortBy_ReturnsOk() throws Exception {
        when(pathUtility.getFolderPath(0L)).thenReturn("user_folder");
        when(pathUtility.getFullPath("user_folder")).thenReturn(Path.of("/root/user_folder"));
        when(fileUtility.getFileAndFolderPathsFromFolder(any(Path.class))).thenReturn(List.of());
        when(fileSystemRepository.getListOfMetadataFromPath(anyList())).thenReturn(Map.of("files", List.of(), "folders", List.of()));

        ResponseEntity<?> result = controller.listFiles(0L);

        assertEquals(200, result.getStatusCode().value());
    }
}
