package com.cloud.NetworkCloudDrive.Controllers;

import com.cloud.NetworkCloudDrive.Controllers.Filesystem.List.RecentsController;
import com.cloud.NetworkCloudDrive.Models.Response.JSONObjectArrayResponse;
import com.cloud.NetworkCloudDrive.Repositories.FileSystemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RecentsControllerTest {

    @Mock
    private FileSystemRepository fileSystemRepository;

    private RecentsController controller;

    @BeforeEach
    void setUp() {
        controller = new RecentsController(fileSystemRepository);
    }

    @Test
    void listRecents_Success_ReturnsOk() {
        Map<String, List<?>> expected = Map.of("files", List.of(), "folders", List.of());
        when(fileSystemRepository.collectAllRecents()).thenReturn(expected);

        ResponseEntity<?> result = controller.listRecents();

        assertEquals(200, result.getStatusCode().value());
        assertInstanceOf(JSONObjectArrayResponse.class, result.getBody());
        JSONObjectArrayResponse response = (JSONObjectArrayResponse) result.getBody();
        Object[] objects = (Object[]) response.getObject();
        assertEquals(expected, objects[0]);
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
