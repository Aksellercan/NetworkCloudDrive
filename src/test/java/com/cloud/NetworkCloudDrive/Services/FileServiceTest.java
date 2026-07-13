package com.cloud.NetworkCloudDrive.Services;

import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import com.cloud.NetworkCloudDrive.Persistence.SQLiteDAO;
import com.cloud.NetworkCloudDrive.Properties.ThumbnailProperties;
import com.cloud.NetworkCloudDrive.Repositories.Maintenance.ThumbnailRepository;
import com.cloud.NetworkCloudDrive.Security.EncodingUtility;
import com.cloud.NetworkCloudDrive.Sessions.UserSession;
import com.cloud.NetworkCloudDrive.Tasks.SequentialJobExecutor;
import com.cloud.NetworkCloudDrive.Utilities.FileUtility;
import com.cloud.NetworkCloudDrive.Utilities.PathUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.scheduling.annotation.Async;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
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
    @Mock
    private SequentialJobExecutor sequentialJobExecutor;

    private FileService fileService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        fileService = new FileService(sqLiteDAO, sequentialJobExecutor, userSession, fileUtility,
                encodingUtility, pathUtility, thumbnailProperties);
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

    @Test
    void uploadFiles_SingleFile_ReturnsUploadedCount() throws Exception {
        MockMultipartFile file = new MockMultipartFile("files", "test.txt", "text/plain", "hello world".getBytes());
        MockMultipartFile[] files = new MockMultipartFile[]{file};

        when(pathUtility.getFullPath("user/folder")).thenReturn(tempDir.resolve("user/folder"));
        when(fileUtility.getFileAndFolderPathsFromFolder(any(Path.class))).thenReturn(List.of());
        when(pathUtility.isFilenameAllowed("test.txt")).thenReturn(true);
        when(pathUtility.isFilenameAllowed("1:test.txt:1")).thenReturn(true);
        when(encodingUtility.encodeBase32FileName(anyLong(), eq("test.txt"), anyLong())).thenReturn("1:test.txt:1");
        when(userSession.getId()).thenReturn(1L);
        when(pathUtility.getBasePath()).thenReturn(tempDir);
        when(pathUtility.isPathAllowed(any(Path.class))).thenReturn(true);
        when(thumbnailProperties.isAllowedImageFormat("text/plain")).thenReturn(false);

        doAnswer(invocation -> {
            FileMetadata m = invocation.getArgument(0);
            m.setId(1L);
            return null;
        }).when(sqLiteDAO).persistObjects(any());
        doAnswer(invocation -> invocation.getArgument(0)).when(sqLiteDAO).saveFile(any());

        Map<String, ?> result = fileService.uploadFiles(files, "user/folder", 1L);

        assertEquals(1, result.get("uploaded_file_count"));
        assertTrue(result.containsKey("files"));
    }

    @Test
    void uploadFiles_WhenDuplicate_ThrowsException() throws Exception {
        MockMultipartFile file = new MockMultipartFile("files", "test.txt", "text/plain", "data".getBytes());
        MockMultipartFile[] files = new MockMultipartFile[]{file};
        Path existingFile = Path.of("existing:already.txt:1");

        when(pathUtility.getFullPath("user/folder")).thenReturn(tempDir.resolve("user/folder"));
        when(fileUtility.getFileAndFolderPathsFromFolder(any(Path.class))).thenReturn(List.of(existingFile));
        when(pathUtility.isFilenameAllowed("test.txt")).thenReturn(true);
        when(fileUtility.checkDuplicate(anyList(), eq("test.txt"))).thenReturn(true);

        assertThrows(java.nio.file.FileAlreadyExistsException.class,
                () -> fileService.uploadFiles(files, "user/folder", 1L));
    }

    @Test
    void uploadFiles_WhenInvalidFilename_SkipsFile() throws Exception {
        MockMultipartFile valid = new MockMultipartFile("files", "valid.txt", "text/plain", "ok".getBytes());
        MockMultipartFile invalid = new MockMultipartFile("files", "../bad.txt", "text/plain", "nope".getBytes());
        MockMultipartFile[] files = new MockMultipartFile[]{valid, invalid};

        when(pathUtility.getFullPath("user/folder")).thenReturn(tempDir.resolve("user/folder"));
        when(fileUtility.getFileAndFolderPathsFromFolder(any(Path.class))).thenReturn(List.of());
        when(pathUtility.isFilenameAllowed("valid.txt")).thenReturn(true);
        when(pathUtility.isFilenameAllowed("../bad.txt")).thenReturn(false);
        when(pathUtility.isFilenameAllowed("1:valid.txt:1")).thenReturn(true);
        when(encodingUtility.encodeBase32FileName(anyLong(), eq("valid.txt"), anyLong())).thenReturn("1:valid.txt:1");
        when(userSession.getId()).thenReturn(1L);
        when(pathUtility.getBasePath()).thenReturn(tempDir);
        when(pathUtility.isPathAllowed(any(Path.class))).thenReturn(true);
        when(thumbnailProperties.isAllowedImageFormat("text/plain")).thenReturn(false);

        doAnswer(invocation -> {
            FileMetadata m = invocation.getArgument(0);
            m.setId(1L);
            return null;
        }).when(sqLiteDAO).persistObjects(any());
        doAnswer(invocation -> invocation.getArgument(0)).when(sqLiteDAO).saveFile(any());

        Map<String, ?> result = fileService.uploadFiles(files, "user/folder", 1L);

        assertEquals(1, result.get("uploaded_file_count"));
        assertTrue(result.containsKey("files"));
    }

    @Test
    void uploadFiles_AllFilesSkipped_ThrowsException() throws Exception {
        MockMultipartFile file = new MockMultipartFile("files", "../evil.txt", "text/plain", "data".getBytes());
        MockMultipartFile[] files = new MockMultipartFile[]{file};

        when(pathUtility.getFullPath("user/folder")).thenReturn(tempDir.resolve("user/folder"));
        when(fileUtility.getFileAndFolderPathsFromFolder(any(Path.class))).thenReturn(List.of());
        when(pathUtility.isFilenameAllowed("../evil.txt")).thenReturn(false);

        assertThrows(java.nio.file.FileAlreadyExistsException.class,
                () -> fileService.uploadFiles(files, "user/folder", 1L));
    }

    @Test
    void createFolder_Success_ReturnsFolderMetadata() throws Exception {
        String folderName = "myfolder";
        long folderId = 5L;

        Path parentDir = Files.createDirectories(tempDir.resolve("user/folder_5"));

        when(sqLiteDAO.getIdPath(folderId, 1L)).thenReturn("0/5");
        when(pathUtility.getFolderPath(folderId)).thenReturn("user/folder_5");
        when(pathUtility.getFullPathToString("user/folder_5")).thenReturn(parentDir.toString());
        when(pathUtility.isFilenameAllowed(folderName)).thenReturn(true);

        doAnswer(invocation -> {
            FolderMetadata m = invocation.getArgument(0);
            m.setId(10L);
            return null;
        }).when(sqLiteDAO).persistObjects(any(FolderMetadata.class));

        when(encodingUtility.encodeBase32FolderName(anyLong(), eq(folderName), anyLong())).thenReturn("10:myfolder:1");
        when(userSession.getId()).thenReturn(1L);
        when(fileUtility.checkIfFileExistsDecodeNames("user/folder_5", folderName)).thenReturn(false);
        when(pathUtility.isPathAllowed(any(Path.class))).thenReturn(true);

        FolderMetadata saved = new FolderMetadata("10:myfolder:1", "0/5/10");
        saved.setId(10L);
        saved.setUserid(1L);
        when(sqLiteDAO.saveFolder(any(FolderMetadata.class))).thenReturn(saved);

        FolderMetadata result = fileService.createFolder(folderName, folderId);

        assertNotNull(result);
        assertEquals("10:myfolder:1", result.getName());
        assertEquals(1L, result.getUserid());
        verify(sqLiteDAO).saveFolder(any(FolderMetadata.class));
    }

    @Test
    void createFolder_WhenDuplicate_ThrowsException() throws Exception {
        String folderName = "existing";

        when(sqLiteDAO.getIdPath(5L, 1L)).thenReturn("0/5");
        when(pathUtility.getFolderPath(5L)).thenReturn("user/folder");
        when(pathUtility.getFullPathToString("user/folder")).thenReturn(tempDir.resolve("user/folder").toString());
        when(pathUtility.isFilenameAllowed(folderName)).thenReturn(true);

        doAnswer(invocation -> {
            FolderMetadata m = invocation.getArgument(0);
            m.setId(10L);
            return null;
        }).when(sqLiteDAO).persistObjects(any(FolderMetadata.class));

        when(encodingUtility.encodeBase32FolderName(anyLong(), eq(folderName), anyLong())).thenReturn("enc:existing:1");
        when(userSession.getId()).thenReturn(1L);
        when(fileUtility.checkIfFileExistsDecodeNames("user/folder", folderName)).thenReturn(true);

        assertThrows(java.nio.file.FileAlreadyExistsException.class,
                () -> fileService.createFolder(folderName, 5L));
    }

    @Test
    void createFolder_WhenInvalidName_ThrowsException() throws Exception {
        when(pathUtility.isFilenameAllowed("../invalid")).thenReturn(false);

        assertThrows(SecurityException.class,
                () -> fileService.createFolder("../invalid", 5L));
    }
}
