package com.cloud.NetworkCloudDrive.Controllers;

import com.cloud.NetworkCloudDrive.Repositories.FileSystemRepository;
import com.cloud.NetworkCloudDrive.Repositories.InformationRepository;
import com.cloud.NetworkCloudDrive.Security.EncodingUtility;
import com.cloud.NetworkCloudDrive.Sessions.UserSession;
import com.cloud.NetworkCloudDrive.Utilities.FileUtility;
import com.cloud.NetworkCloudDrive.Utilities.PathUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@TestPropertySource(locations = "classpath:/application-test.properties")
class FileSystemControllerVersioningTest {

    @Autowired
    private WebApplicationContext context;

    @MockitoBean
    private FileSystemRepository fileSystemRepository;
    @MockitoBean
    private FileUtility fileUtility;
    @MockitoBean
    private InformationRepository informationRepository;
    @MockitoBean
    private UserSession userSession;
    @MockitoBean
    private EncodingUtility encodingUtility;
    @MockitoBean
    private PathUtility pathUtility;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    void recents_Version1_ReturnsOk() throws Exception {
        when(fileSystemRepository.collectAllRecents())
                .thenReturn(Map.of("files", List.of(), "folders", List.of()));

        mockMvc.perform(get("/api/v1/filesystem/recents").with(user("testuser")))
                .andExpect(status().isOk());
    }

    @Test
    void recents_Version2_WithPaging_ReturnsOk() throws Exception {
        when(fileSystemRepository.collectAllRecentsPageable(any(Pageable.class)))
                .thenReturn(Map.of("files", List.of(), "folders", List.of()));

        mockMvc.perform(get("/api/v2/filesystem/recents").with(user("testuser"))
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk());
    }
}
