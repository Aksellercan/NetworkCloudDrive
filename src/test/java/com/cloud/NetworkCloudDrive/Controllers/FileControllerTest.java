package com.cloud.NetworkCloudDrive.Controllers;

import com.cloud.NetworkCloudDrive.Models.DTO.CreateFolderDTO;
import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import com.cloud.NetworkCloudDrive.Repositories.FileRepository;
import com.cloud.NetworkCloudDrive.Repositories.InformationRepository;
import com.cloud.NetworkCloudDrive.Security.EncodingUtility;
import com.cloud.NetworkCloudDrive.Utilities.PathUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.nio.file.FileAlreadyExistsException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileControllerTest {

    @Mock
    private FileRepository fileRepository;
    @Mock
    private InformationRepository informationRepository;
    @Mock
    private EncodingUtility encodingUtility;
    @Mock
    private PathUtility pathUtility;

    private FileController controller;

    @BeforeEach
    void setUp() {
        controller = new FileController(informationRepository, encodingUtility, pathUtility, fileRepository);
    }

    @Test
    void uploadFile_Success_ReturnsOk() throws Exception {
        MockMultipartFile[] files = new MockMultipartFile[]{
                new MockMultipartFile("files", "test.txt", "text/plain", "data".getBytes())
        };
        when(pathUtility.getFolderPath(1L)).thenReturn("user_folder");
        Map<String, Object> response = Map.of("uploaded_file_count", 1);
        when(fileRepository.uploadFiles(any(), anyString(), eq(1L))).thenReturn((Map) response);

        ResponseEntity<?> result = controller.uploadFile(files, 1L);

        assertEquals(200, result.getStatusCode().value());
    }

    @Test
    void uploadFile_WithOptions_Success_ReturnsOk() throws Exception {
        MockMultipartFile[] files = new MockMultipartFile[]{
                new MockMultipartFile("files", "test.txt", "text/plain", "data".getBytes())
        };
        when(pathUtility.getFolderPath(1L)).thenReturn("user_folder");
        Map<String, Object> response = Map.of("uploaded_file_count", 1);
        when(fileRepository.uploadFiles(any(), anyString(), eq(1L))).thenReturn((Map) response);

        ResponseEntity<?> result = controller.uploadFile(files, 1L);

        assertEquals(200, result.getStatusCode().value());
    }
}
