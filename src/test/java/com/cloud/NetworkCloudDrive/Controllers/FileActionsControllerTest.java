package com.cloud.NetworkCloudDrive.Controllers;

import com.cloud.NetworkCloudDrive.Controllers.Filesystem.Actions.FileActionsController;
import com.cloud.NetworkCloudDrive.Models.DTO.UpdateFileNameDTO;
import com.cloud.NetworkCloudDrive.Models.DTO.UpdateFilePathDTO;
import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Repositories.FileSystemRepository;
import com.cloud.NetworkCloudDrive.Repositories.InformationRepository;
import com.cloud.NetworkCloudDrive.Sessions.UserSession;
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
class FileActionsControllerTest {

    @Mock
    private InformationRepository informationRepository;
    @Mock
    private FileSystemRepository fileSystemRepository;
    @Mock
    private PathUtility pathUtility;
    @Mock
    private UserSession userSession;

    private FileActionsController controller;

    @BeforeEach
    void setUp() {
        controller = new FileActionsController(informationRepository, fileSystemRepository, pathUtility, userSession);
    }

    @Test
    void updateFileName_Success_ReturnsOk() throws Exception {
        UpdateFileNameDTO dto = new UpdateFileNameDTO();
        dto.setFile_id(10L);
        dto.setName("newname.txt");

        FileMetadata metadata = new FileMetadata("encoded", 1L, 1L, "text/plain", 100L);
        metadata.setId(10L);
        when(informationRepository.getFileMetadata(10L)).thenReturn(metadata);
        when(fileSystemRepository.updateFileName("newname.txt", metadata)).thenReturn("path/newname.txt");

        ResponseEntity<?> result = controller.updateFileName(dto);

        assertEquals(200, result.getStatusCode().value());
    }

    @Test
    void updateFileName_NotFound_ReturnsBadRequest() throws Exception {
        UpdateFileNameDTO dto = new UpdateFileNameDTO();
        dto.setFile_id(999L);
        dto.setName("newname.txt");

        when(informationRepository.getFileMetadata(999L))
                .thenThrow(new java.io.FileNotFoundException("Not found"));

        ResponseEntity<?> result = controller.updateFileName(dto);

        assertEquals(400, result.getStatusCode().value());
    }

    @Test
    void moveFile_Success_ReturnsOk() throws Exception {
        UpdateFilePathDTO dto = new UpdateFilePathDTO();
        dto.setFile_id(10L);
        dto.setFolder_id(0L);

        FileMetadata metadata = new FileMetadata("encoded", 1L, 1L, "text/plain", 100L);
        metadata.setId(10L);
        when(informationRepository.getFileMetadata(10L)).thenReturn(metadata);
        when(userSession.getName()).thenReturn("testuser");
        when(fileSystemRepository.moveFile(metadata, 0L)).thenReturn("dest_path");

        ResponseEntity<?> result = controller.moveFile(dto);

        assertEquals(200, result.getStatusCode().value());
    }

    @Test
    void removeFile_Success_ReturnsOk() throws Exception {
        FileMetadata metadata = new FileMetadata("encoded", 1L, 1L, "text/plain", 100L);
        metadata.setId(10L);
        when(informationRepository.getFileMetadata(10L)).thenReturn(metadata);
        when(fileSystemRepository.removeFile(metadata)).thenReturn("path/encoded");

        ResponseEntity<?> result = controller.removeFile(10L);

        assertEquals(200, result.getStatusCode().value());
    }

    @Test
    void removeFile_NotFound_ReturnsBadRequest() throws Exception {
        when(informationRepository.getFileMetadata(999L))
                .thenThrow(new java.io.FileNotFoundException("Not found"));

        ResponseEntity<?> result = controller.removeFile(999L);

        assertEquals(400, result.getStatusCode().value());
    }
}
