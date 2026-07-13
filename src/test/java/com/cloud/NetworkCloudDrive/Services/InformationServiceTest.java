package com.cloud.NetworkCloudDrive.Services;

import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import com.cloud.NetworkCloudDrive.Persistence.SQLiteDAO;
import com.cloud.NetworkCloudDrive.Sessions.UserSession;
import com.cloud.NetworkCloudDrive.Utilities.FileUtility;
import com.cloud.NetworkCloudDrive.Utilities.PathUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.FileSystemException;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InformationServiceTest {

    @Mock
    private FileUtility fileUtility;
    @Mock
    private SQLiteDAO sqLiteDAO;
    @Mock
    private UserSession userSession;
    @Mock
    private PathUtility pathUtility;

    private InformationService informationService;

    @BeforeEach
    void setUp() {
        informationService = new InformationService(fileUtility, sqLiteDAO, userSession, pathUtility);
    }

    @Test
    void getFileMetadata_FileExists_ReturnsMetadataWithSize() throws Exception {
        FileMetadata metadata = new FileMetadata("test.txt", 1L, 1L, "text/plain", 100L);
        metadata.setId(10L);
        when(sqLiteDAO.queryFileMetadata(10L, 0L)).thenReturn(metadata);
        when(userSession.getId()).thenReturn(0L);
        when(pathUtility.getFolderPath(1L)).thenReturn("user_folder");
        File mockFile = mock(File.class);
        when(mockFile.length()).thenReturn(200L);
        when(fileUtility.returnFileIfItExists(anyString())).thenReturn(mockFile);

        FileMetadata result = informationService.getFileMetadata(10L);

        assertEquals(10L, result.getId());
        assertEquals(200L, result.getSize());
        verify(sqLiteDAO).queryFileMetadata(10L, 0L);
        verify(fileUtility).returnFileIfItExists(anyString());
    }

    @Test
    void getFileMetadata_FileNotFound_ThrowsException() throws Exception {
        when(sqLiteDAO.queryFileMetadata(anyLong(), anyLong())).thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class, () -> informationService.getFileMetadata(999L));
    }

    @Test
    void getFolderMetadata_FolderExists_ReturnsFolder() throws Exception {
        FolderMetadata folder = new FolderMetadata("myfolder", "0/1/2");
        folder.setId(5L);
        folder.setUserid(1L);
        when(sqLiteDAO.queryFolderMetadata(5L, 0L)).thenReturn(folder);
        when(userSession.getId()).thenReturn(0L);
        when(pathUtility.resolvePathFromIdString("0/1/2")).thenReturn("user_folder/myfolder");

        FolderMetadata result = informationService.getFolderMetadata(5L);

        assertEquals(5L, result.getId());
        assertEquals("myfolder", result.getName());
        verify(sqLiteDAO).queryFolderMetadata(5L, 0L);
    }

    @Test
    void getFolderMetadataByFolderIdAndName_FindsCorrectFolder() throws Exception {
        long folderId = 1L;
        String name = "target";
        List<Long> skipList = List.of();

        FolderMetadata folder1 = new FolderMetadata("other", "0/1/3");
        folder1.setId(3L);
        FolderMetadata folder2 = new FolderMetadata("target", "0/1/4");
        folder2.setId(4L);

        when(sqLiteDAO.getIdPath(1L, 0L)).thenReturn("0/1");
        when(userSession.getId()).thenReturn(0L);
        when(sqLiteDAO.findAllContainingSectionOfIdPathIgnoreCase("0/1", 0L))
                .thenReturn(List.of(folder1, folder2));

        FolderMetadata result = informationService.getFolderMetadataByFolderIdAndName(folderId, name, skipList);

        assertNotNull(result);
    }

    @Test
    void getFolderMetadataByFolderIdAndName_NoFoldersFound_ThrowsException() throws Exception {
        when(sqLiteDAO.getIdPath(1L, 0L)).thenReturn("0/1");
        when(userSession.getId()).thenReturn(0L);
        when(sqLiteDAO.findAllContainingSectionOfIdPathIgnoreCase("0/1", 0L))
                .thenReturn(List.of());

        assertThrows(FileSystemException.class,
                () -> informationService.getFolderMetadataByFolderIdAndName(1L, "name", List.of()));
    }

    @Test
    void getFolderMetadataByFolderIdAndName_SkipListExcludesFolder() throws Exception {
        long folderId = 1L;
        List<Long> skipList = List.of(4L);

        FolderMetadata folder1 = new FolderMetadata("folder1", "0/1/3");
        folder1.setId(3L);
        FolderMetadata folder2 = new FolderMetadata("folder2", "0/1/4");
        folder2.setId(4L);

        when(sqLiteDAO.getIdPath(1L, 0L)).thenReturn("0/1");
        when(userSession.getId()).thenReturn(0L);
        when(sqLiteDAO.findAllContainingSectionOfIdPathIgnoreCase("0/1", 0L))
                .thenReturn(List.of(folder1, folder2));

        FolderMetadata result = informationService.getFolderMetadataByFolderIdAndName(folderId, "folder2", skipList);

        assertEquals(3L, result.getId());
    }
}
