package com.cloud.NetworkCloudDrive.Controllers;

import com.cloud.NetworkCloudDrive.Controllers.Filesystem.Actions.FolderActionsController;
import com.cloud.NetworkCloudDrive.Models.DTO.UpdateFolderNameDTO;
import com.cloud.NetworkCloudDrive.Models.DTO.UpdateFolderPathDTO;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import com.cloud.NetworkCloudDrive.Repositories.FileSystemRepository;
import com.cloud.NetworkCloudDrive.Repositories.InformationRepository;
import com.cloud.NetworkCloudDrive.Security.EncodingUtility;
import com.cloud.NetworkCloudDrive.Utilities.PathUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FolderActionsControllerTest {

    @Mock
    private InformationRepository informationRepository;
    @Mock
    private EncodingUtility encodingUtility;
    @Mock
    private FileSystemRepository fileSystemRepository;
    @Mock
    private PathUtility pathUtility;

    private FolderActionsController controller;

    @BeforeEach
    void setUp() {
        controller = new FolderActionsController(informationRepository, encodingUtility, fileSystemRepository, pathUtility);
    }

    @Test
    void updateFolderName_Success_ReturnsOk() throws Exception {
        UpdateFolderNameDTO dto = new UpdateFolderNameDTO();
        dto.setFolder_id(5L);
        dto.setName("renamed");

        FolderMetadata folder = new FolderMetadata("old_encoded", "0/5");
        folder.setId(5L);
        when(informationRepository.getFolderMetadata(5L)).thenReturn(folder);
        when(encodingUtility.decodedBase32SplitArray("old_encoded")).thenReturn(new String[]{"5", "oldName", "1"});
        when(fileSystemRepository.updateFolderName("renamed", folder)).thenReturn("path/renamed");

        ResponseEntity<?> result = controller.updateFolderName(dto);

        assertEquals(200, result.getStatusCode().value());
    }

    @Test
    void moveFolder_Success_ReturnsOk() throws Exception {
        UpdateFolderPathDTO dto = new UpdateFolderPathDTO();
        dto.setFormerFolderid(5L);
        dto.setDestination_folder_id(10L);

        FolderMetadata folder = new FolderMetadata("folder", "0/5");
        folder.setId(5L);
        when(informationRepository.getFolderMetadata(5L)).thenReturn(folder);
        when(pathUtility.resolvePathFromIdString("0/5")).thenReturn("old_path");
        when(fileSystemRepository.moveFolder(folder, 10L)).thenReturn("new_path");

        ResponseEntity<?> result = controller.moveFile(dto);

        assertEquals(200, result.getStatusCode().value());
    }

    @Test
    void removeFolder_Success_ReturnsOk() throws Exception {
        FolderMetadata folder = new FolderMetadata("encoded", "0/5");
        folder.setId(5L);
        when(informationRepository.getFolderMetadata(5L)).thenReturn(folder);
        when(fileSystemRepository.removeFolder(folder)).thenReturn("path/encoded");

        ResponseEntity<?> result = controller.removeFolder(5L);

        assertEquals(200, result.getStatusCode().value());
    }
}
