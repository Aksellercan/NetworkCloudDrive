package com.cloud.NetworkCloudDrive.Services.Maintenance;

import com.cloud.NetworkCloudDrive.Models.Domain.DeletionResults;
import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.ThumbnailMetadata;
import com.cloud.NetworkCloudDrive.Persistence.SQLiteDAO;
import com.cloud.NetworkCloudDrive.Sessions.UserSession;
import com.cloud.NetworkCloudDrive.Utilities.FileUtility;
import com.cloud.NetworkCloudDrive.Utilities.ImageUtility;
import com.cloud.NetworkCloudDrive.Utilities.PathUtility;
import com.cloud.NetworkCloudDrive.Utilities.UserUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ThumbnailServiceTest {

    @Mock
    private UserUtility userUtility;
    @Mock
    private UserSession userSession;
    @Mock
    private FileUtility fileUtility;
    @Mock
    private SQLiteDAO sqLiteDAO;
    @Mock
    private PathUtility pathUtility;
    @Mock
    private ImageUtility imageUtility;

    private ThumbnailService thumbnailService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        thumbnailService = new ThumbnailService(
                userUtility, userSession, fileUtility, sqLiteDAO, pathUtility, imageUtility);
    }

    @Test
    void getThumbnailByID_ReturnsMetadata() throws Exception {
        ThumbnailMetadata expected = new ThumbnailMetadata("thumb.jpg", 1L, "image/jpeg", 1024L, 10L, true);
        expected.setId(5L);
        when(sqLiteDAO.queryThumbnailMetadata(5L, 1L)).thenReturn(expected);
        when(userSession.getId()).thenReturn(1L);

        ThumbnailMetadata result = thumbnailService.getThumbnailByID(5L);

        assertNotNull(result);
        assertEquals(5L, result.getId());
        assertEquals("thumb.jpg", result.getFileName());
    }

    @Test
    void getThumbnailByID_WhenNotFound_ReturnsNull() throws Exception {
        when(sqLiteDAO.queryThumbnailMetadata(99L, 1L)).thenReturn(null);
        when(userSession.getId()).thenReturn(1L);

        ThumbnailMetadata result = thumbnailService.getThumbnailByID(99L);

        assertNull(result);
    }

    @Test
    void getThumbnailByFileID_ReturnsMetadata() throws Exception {
        ThumbnailMetadata expected = new ThumbnailMetadata("file_thumb.jpg", 1L, "image/jpeg", 2048L, 10L, false);
        expected.setId(3L);
        when(sqLiteDAO.queryThumbnailMetadataUsingFileId(10L, 1L)).thenReturn(expected);
        when(userSession.getId()).thenReturn(1L);

        ThumbnailMetadata result = thumbnailService.getThumbnailByFileID(10L);

        assertNotNull(result);
        assertEquals(3L, result.getId());
    }

    @Test
    void deleteThumbnailByFileID_DeletesFromDBAndDisk() throws Exception {
        ThumbnailMetadata metadata = new ThumbnailMetadata("thumb.jpg", 1L, "image/jpeg", 512L, 10L, true);
        metadata.setId(7L);
        when(sqLiteDAO.queryThumbnailMetadataUsingFileId(10L, 1L)).thenReturn(metadata);
        when(userSession.getId()).thenReturn(1L);
        when(imageUtility.getThumbnailPath(true)).thenReturn(tempDir.resolve("portrait"));

        Path thumbDir = Files.createDirectories(tempDir.resolve("portrait"));
        Path thumbFile = Files.writeString(thumbDir.resolve("thumb.jpg"), "data");

        thumbnailService.deleteThumbnailByFileID(10L);

        verify(sqLiteDAO).deleteThumbnail(metadata);
        assertTrue(Files.notExists(thumbFile));
    }

    @Test
    void deleteThumbnailByFileIDAndSetThumbnailStatus_UpdatesFileMetadata() throws Exception {
        ThumbnailMetadata metadata = new ThumbnailMetadata("thumb.jpg", 1L, "image/jpeg", 512L, 10L, true);
        metadata.setId(7L);
        FileMetadata fileMetadata = new FileMetadata("test.txt", 1L, 1L, "text/plain", 100L);
        fileMetadata.setId(10L);
        fileMetadata.setHasThumbnail(true);

        when(sqLiteDAO.queryThumbnailMetadataUsingFileId(10L, 1L)).thenReturn(metadata);
        when(userSession.getId()).thenReturn(1L);
        when(imageUtility.getThumbnailPath(true)).thenReturn(tempDir.resolve("portrait"));
        when(sqLiteDAO.queryFileMetadata(10L, 1L)).thenReturn(fileMetadata);

        Path thumbDir = Files.createDirectories(tempDir.resolve("portrait"));
        Files.writeString(thumbDir.resolve("thumb.jpg"), "data");

        thumbnailService.deleteThumbnailByFileIDAndSetThumbnailStatus(10L);

        verify(sqLiteDAO).deleteThumbnail(metadata);
        verify(sqLiteDAO).saveFile(fileMetadata);
        assertFalse(fileMetadata.isHasThumbnail());
    }

    @Test
    void deleteThumbnailByFileID_WhenFileNotFound_ThrowsException() throws Exception {
        ThumbnailMetadata metadata = new ThumbnailMetadata("missing.jpg", 1L, "image/jpeg", 512L, 10L, true);
        metadata.setId(7L);
        when(sqLiteDAO.queryThumbnailMetadataUsingFileId(10L, 1L)).thenReturn(metadata);
        when(userSession.getId()).thenReturn(1L);
        when(imageUtility.getThumbnailPath(true)).thenReturn(tempDir.resolve("portrait"));

        assertThrows(java.io.FileNotFoundException.class,
                () -> thumbnailService.deleteThumbnailByFileID(10L));
    }

    @Test
    void getThumbnail_WhenFileExists_ReturnsResource() throws Exception {
        Path thumbDir = Files.createDirectories(tempDir.resolve("portrait"));
        Path thumbFile = Files.writeString(thumbDir.resolve("test_thumb.jpg"), "image data");

        when(imageUtility.getThumbnailPath(true)).thenReturn(thumbDir);
        when(pathUtility.getBasePath()).thenReturn(Path.of("/unrelated/base"));

        Resource resource = thumbnailService.getThumbnail("test_thumb.jpg", true);

        assertNotNull(resource);
        assertTrue(resource.exists());
    }

    @Test
    void getThumbnail_WhenFileDoesNotExist_ThrowsException() throws Exception {
        when(imageUtility.getThumbnailPath(true)).thenReturn(tempDir.resolve("portrait"));
        when(pathUtility.getBasePath()).thenReturn(Path.of("/unrelated/base"));

        assertThrows(Exception.class,
                () -> thumbnailService.getThumbnail("nonexistent.jpg", true));
    }

    @Test
    void deleteAllThumbnails_DeletesAndReturnsResults() throws Exception {
        ThumbnailMetadata t1 = new ThumbnailMetadata("t1.jpg", 1L, "image/jpeg", 100L, 1L, true);
        ThumbnailMetadata t2 = new ThumbnailMetadata("t2.jpg", 1L, "image/png", 200L, 2L, false);
        FileMetadata f1 = new FileMetadata("f1.txt", 1L, 1L, "text/plain", 50L);
        f1.setId(1L);
        f1.setHasThumbnail(true);
        FileMetadata f2 = new FileMetadata("f2.txt", 1L, 1L, "text/plain", 60L);
        f2.setId(2L);
        f2.setHasThumbnail(true);

        when(userSession.getId()).thenReturn(1L);
        when(sqLiteDAO.findAllThumbnailsByUserID(1L)).thenReturn(List.of(t1, t2));
        when(sqLiteDAO.findFileMetadataById(1L)).thenReturn(f1);
        when(sqLiteDAO.findFileMetadataById(2L)).thenReturn(f2);
        when(imageUtility.getThumbnailPath()).thenReturn(tempDir.resolve(".thumbnails"));
        DeletionResults deletionResults = new DeletionResults();
        when(fileUtility.deleteFolders(any(Path.class))).thenReturn(deletionResults);

        DeletionResults result = thumbnailService.deleteAllThumbnails();

        assertNotNull(result);
        verify(sqLiteDAO).saveAllFiles(anyList());
        verify(fileUtility).deleteFolders(any(Path.class));
        verify(sqLiteDAO).deleteAllThumbnails(anyList());
        assertFalse(f1.isHasThumbnail());
        assertFalse(f2.isHasThumbnail());
    }
}
