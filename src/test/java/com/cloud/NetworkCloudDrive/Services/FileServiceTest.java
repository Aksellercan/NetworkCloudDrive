package com.cloud.NetworkCloudDrive.Services;

import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Persistence.SQLiteDAO;
import com.cloud.NetworkCloudDrive.Properties.ThumbnailProperties;
import com.cloud.NetworkCloudDrive.Repositories.Maintenance.ThumbnailRepository;
import com.cloud.NetworkCloudDrive.Security.EncodingUtility;
import com.cloud.NetworkCloudDrive.Sessions.UserSession;
import com.cloud.NetworkCloudDrive.Utilities.FileUtility;
import com.cloud.NetworkCloudDrive.Utilities.PathUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.scheduling.annotation.Async;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileServiceTest {

    @Mock
    private SQLiteDAO sqLiteDAO;
    @Mock
    private UserSession userSession;
    @Mock
    private FileUtility fileUtility;
    @Mock
    private EncodingUtility encodingUtility;
    @Mock
    private PathUtility pathUtility;
    @Mock
    private ThumbnailProperties thumbnailProperties;
    @Mock
    private ThumbnailRepository thumbnailRepository;

    private FileService fileService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        fileService = new FileService(sqLiteDAO, userSession, fileUtility,
                encodingUtility, pathUtility, thumbnailProperties, thumbnailRepository);
    }

    @Test
    void storeFile_CreatesDirectoriesAndWritesFile() throws Exception {
        String fileName = "encoded_test_file.abc";
        String parentPath = "user_folder_encoded";
        String content = "test file content";
        InputStream inputStream = new ByteArrayInputStream(content.getBytes());

        Path userDirectory = tempDir.resolve(parentPath);
        Path filePath = userDirectory.resolve(fileName);

        when(pathUtility.getBasePath()).thenReturn(tempDir);
        when(pathUtility.isPathAllowed(filePath)).thenReturn(true);
        when(pathUtility.isFilenameAllowed(fileName)).thenReturn(true);

        Path result = fileService.storeFile(inputStream, fileName, parentPath).get();

        assertTrue(Files.exists(filePath));
        assertEquals(content, Files.readString(filePath));
        assertEquals(tempDir.relativize(filePath), result);
    }

    @Test
    void storeFile_WhenPathNotAllowed_ThrowsIOException() throws Exception {
        String fileName = "test.txt";
        String parentPath = "user_folder";

        when(pathUtility.getBasePath()).thenReturn(tempDir);
        when(pathUtility.isPathAllowed(any(Path.class))).thenReturn(false);

        assertThrows(IOException.class,
                () -> fileService.storeFile(new ByteArrayInputStream(new byte[0]), fileName, parentPath));
    }

    @Test
    void storeFile_WhenFilenameNotAllowed_ThrowsIOException() throws Exception {
        String fileName = "../evil.txt";
        String parentPath = "user_folder";

        when(pathUtility.getBasePath()).thenReturn(tempDir);
        when(pathUtility.isPathAllowed(any(Path.class))).thenReturn(true);
        when(pathUtility.isFilenameAllowed(fileName)).thenReturn(false);

        assertThrows(IOException.class,
                () -> fileService.storeFile(new ByteArrayInputStream(new byte[0]), fileName, parentPath));
    }

    @Test
    void storeFile_IsAnnotatedWithAsync() throws Exception {
        java.lang.reflect.Method method = FileService.class.getMethod(
                "storeFile", InputStream.class, String.class, String.class);
        assertNotNull(method.getAnnotation(Async.class));
    }

    @Test
    void storeFile_ReturnsCompletableFuture() throws Exception {
        when(pathUtility.getBasePath()).thenReturn(tempDir);
        when(pathUtility.isPathAllowed(any(Path.class))).thenReturn(true);
        when(pathUtility.isFilenameAllowed(anyString())).thenReturn(true);

        CompletableFuture<Path> future = fileService.storeFile(
                new ByteArrayInputStream("data".getBytes()), "file.txt", "dir");

        assertNotNull(future);
        assertTrue(future.isDone(), "completedFuture should already be done");
    }

    @Test
    void getFile_WhenFileExists_ReturnsResource() throws Exception {
        String fileName = "encoded_file.xyz";
        String path = "user_folder";
        String content = "test data";

        Path fullPath = tempDir.resolve(path).resolve(fileName);
        Files.createDirectories(fullPath.getParent());
        Files.writeString(fullPath, content);

        when(pathUtility.getFullPathToString(path)).thenReturn(tempDir.resolve(path).toString());
        when(pathUtility.getBasePath()).thenReturn(Path.of("/unrelated/base"));

        FileMetadata fileMetadata = new FileMetadata(fileName, 0L, 1L, "text/plain", 9L);

        Resource resource = fileService.getFile(fileMetadata, path).get();

        assertNotNull(resource);
        assertTrue(resource.exists());
        assertEquals(content, new String(resource.getInputStream().readAllBytes()));
    }

    @Test
    void getFile_WhenFileDoesNotExist_ThrowsException() throws Exception {
        String fileName = "nonexistent.txt";
        String path = "user_folder";

        when(pathUtility.getFullPathToString(path)).thenReturn(tempDir.resolve(path).toString());
        when(pathUtility.getBasePath()).thenReturn(Path.of("/unrelated/base"));

        FileMetadata fileMetadata = new FileMetadata(fileName, 0L, 1L, "text/plain", 0L);

        assertThrows(Exception.class,
                () -> fileService.getFile(fileMetadata, path));
    }

    @Test
    void getFile_WhenUnauthorized_ThrowsSecurityException() throws Exception {
        String fileName = "test.txt";
        String path = "subdir";

        when(pathUtility.getFullPathToString(path)).thenReturn(tempDir.resolve(path).toString());
        when(pathUtility.getBasePath()).thenReturn(tempDir);

        Path filePath = Files.createDirectories(tempDir.resolve(path));
        Files.writeString(filePath.resolve(fileName), "data");

        FileMetadata fileMetadata = new FileMetadata(fileName, 0L, 1L, "text/plain", 4L);

        assertThrows(SecurityException.class,
                () -> fileService.getFile(fileMetadata, path));
    }

    @Test
    void getFile_IsAnnotatedWithAsync() throws Exception {
        java.lang.reflect.Method method = FileService.class.getMethod(
                "getFile", FileMetadata.class, String.class);
        assertNotNull(method.getAnnotation(Async.class));
    }

    @Test
    void getFile_ReturnsCompletableFuture() throws Exception {
        String fileName = "existing.txt";
        String path = "some_dir";

        Path dir = Files.createDirectories(tempDir.resolve(path));
        Files.writeString(dir.resolve(fileName), "content");

        when(pathUtility.getFullPathToString(path)).thenReturn(dir.toString());
        when(pathUtility.getBasePath()).thenReturn(Path.of("/unrelated/base"));

        FileMetadata fileMetadata = new FileMetadata(fileName, 0L, 1L, "text/plain", 7L);

        CompletableFuture<Resource> future = fileService.getFile(fileMetadata, path);

        assertNotNull(future);
        assertTrue(future.isDone(), "completedFuture should already be done");
    }
}
