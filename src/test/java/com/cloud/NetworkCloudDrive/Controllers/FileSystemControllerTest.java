package com.cloud.NetworkCloudDrive.Controllers;

import com.cloud.NetworkCloudDrive.Models.DTO.UpdateFileNameDTO;
import com.cloud.NetworkCloudDrive.Models.DTO.UpdateFilePathDTO;
import com.cloud.NetworkCloudDrive.Models.DTO.UpdateFolderNameDTO;
import com.cloud.NetworkCloudDrive.Models.DTO.UpdateFolderPathDTO;
import com.cloud.NetworkCloudDrive.Models.Enum.SortListEnum;
import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import com.cloud.NetworkCloudDrive.Models.Response.JSONObjectArrayResponse;
import com.cloud.NetworkCloudDrive.Repositories.FileSystemRepository;
import com.cloud.NetworkCloudDrive.Repositories.InformationRepository;
import com.cloud.NetworkCloudDrive.Security.EncodingUtility;
import com.cloud.NetworkCloudDrive.Sessions.UserSession;
import com.cloud.NetworkCloudDrive.Utilities.FileUtility;
import com.cloud.NetworkCloudDrive.Utilities.PathUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FileSystemControllerTest {

    @Mock
    private FileSystemRepository fileSystemRepository;
    @Mock
    private FileUtility fileUtility;
    @Mock
    private InformationRepository informationRepository;
    @Mock
    private UserSession userSession;
    @Mock
    private EncodingUtility encodingUtility;
    @Mock
    private PathUtility pathUtility;

    private FileSystemController controller;

    @BeforeEach
    void setUp() {
        controller = new FileSystemController(informationRepository, userSession, fileUtility, encodingUtility, pathUtility, fileSystemRepository);
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
    void removeFile_Success_ReturnsOk() throws Exception {
        FileMetadata metadata = new FileMetadata("encoded", 1L, 1L, "text/plain", 100L);
        metadata.setId(10L);
        when(informationRepository.getFileMetadata(10L)).thenReturn(metadata);
        when(fileSystemRepository.removeFile(metadata)).thenReturn("path/encoded");

        ResponseEntity<?> result = controller.removeFile(10L);

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

    @Test
    void removeFile_NotFound_ReturnsBadRequest() throws Exception {
        when(informationRepository.getFileMetadata(999L))
                .thenThrow(new java.io.FileNotFoundException("Not found"));

        ResponseEntity<?> result = controller.removeFile(999L);

        assertEquals(400, result.getStatusCode().value());
    }

    @Test
    void listRecents_Success_ReturnsOk() {
        Map<String, List<?>> expected = Map.of("files", List.of(), "folders", List.of());
        when(fileSystemRepository.collectAllRecents()).thenReturn(expected);

        ResponseEntity<?> result = controller.listRecents();

        assertEquals(200, result.getStatusCode().value());
        assertEquals(expected, result.getBody());
    }

    @Test
    void listRecents_WithPaging_ReturnsOk() {
        Map<String, List<?>> expected = Map.of("files", List.of(), "folders", List.of());
        when(fileSystemRepository.collectAllRecentsPageable(any(Pageable.class))).thenReturn(expected);

        ResponseEntity<?> result = controller.listRecents(0, 10);

        assertEquals(200, result.getStatusCode().value());
        assertInstanceOf(JSONObjectArrayResponse.class, result.getBody());
        JSONObjectArrayResponse response = (JSONObjectArrayResponse) result.getBody();
        Object[] objects = (Object[]) response.getObject();
        assertEquals(expected, objects[0]);
        assertInstanceOf(Pageable.class, objects[1]);
        Pageable pageable = (Pageable) objects[1];
        assertEquals(0, pageable.getPageNumber());
        assertEquals(10, pageable.getPageSize());
    }

    @Test
    void listRecents_RepositoryError_ReturnsBadRequest() {
        when(fileSystemRepository.collectAllRecents()).thenThrow(new RuntimeException("boom"));

        ResponseEntity<?> result = controller.listRecents();

        assertEquals(400, result.getStatusCode().value());
    }
}
