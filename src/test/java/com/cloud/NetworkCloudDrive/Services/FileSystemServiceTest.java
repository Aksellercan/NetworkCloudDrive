package com.cloud.NetworkCloudDrive.Services;

import com.cloud.NetworkCloudDrive.Models.Enum.FilterListEnum;
import com.cloud.NetworkCloudDrive.Models.Enum.SortListEnum;
import com.cloud.NetworkCloudDrive.Models.FileMetadata;
import com.cloud.NetworkCloudDrive.Models.FolderMetadata;
import com.cloud.NetworkCloudDrive.Persistence.SQLiteDAO;
import com.cloud.NetworkCloudDrive.Repositories.Maintenance.ThumbnailRepository;
import com.cloud.NetworkCloudDrive.Security.EncodingUtility;
import com.cloud.NetworkCloudDrive.Sessions.UserSession;
import com.cloud.NetworkCloudDrive.Utilities.FileUtility;
import com.cloud.NetworkCloudDrive.Utilities.PathUtility;
import com.cloud.NetworkCloudDrive.Utilities.SortAndFilterUtility;
import com.cloud.NetworkCloudDrive.Utilities.UserUtility;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileSystemServiceTest {

    @Mock
    private FileUtility fileUtility;
    @Mock
    private UserSession userSession;
    @Mock
    private SQLiteDAO sqLiteDAO;
    @Mock
    private EncodingUtility encodingUtility;
    @Mock
    private UserUtility userUtility;
    @Mock
    private PathUtility pathUtility;
    @Mock
    private ThumbnailRepository thumbnailRepository;
    @Mock
    private SortAndFilterUtility sortAndFilterUtility;

    private FileSystemService fileSystemService;

    @TempDir
    Path tempDir;

    private static String encode(long id, String name, long userId) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString((id + ":" + name + ":" + userId).getBytes());
    }

    @BeforeEach
    void setUp() {
        fileSystemService = new FileSystemService(
                userSession, fileUtility, sqLiteDAO, encodingUtility,
                userUtility, pathUtility, thumbnailRepository, sortAndFilterUtility);
    }

    @Test
    void getListOfMetadataFromPath_ReturnsFilesAndFolders() throws Exception {
        Path filePath = Files.createFile(tempDir.resolve("encoded_file1"));
        Path folderPath = Files.createDirectory(tempDir.resolve("encoded_folder1"));
        List<Path> paths = List.of(filePath, folderPath);

        when(encodingUtility.decodedBase32SplitArray("encoded_file1")).thenReturn(new String[]{"1", "document.txt", "1"});
        when(encodingUtility.decodedBase32SplitArray("encoded_folder1")).thenReturn(new String[]{"2", "myfolder", "1"});
        when(fileUtility.isIgnoredFile(anyString())).thenReturn(false);

        FileMetadata fileMetadata = new FileMetadata("1:document.txt:1", 1L, 1L, "text/plain", 100L);
        fileMetadata.setId(1L);
        FolderMetadata folderMetadata = new FolderMetadata("2:myfolder:1", "0/2");
        folderMetadata.setId(2L);

        when(sqLiteDAO.queryFileMetadata(1L, 1L)).thenReturn(fileMetadata);
        when(sqLiteDAO.queryFolderMetadata(2L, 1L)).thenReturn(folderMetadata);
        when(userSession.getId()).thenReturn(1L);

        Map<String, List<?>> result = fileSystemService.getListOfMetadataFromPath(paths);

        assertTrue(result.containsKey("files"));
        assertTrue(result.containsKey("folders"));
        assertEquals(1, ((List<?>) result.get("files")).size());
        assertEquals(1, ((List<?>) result.get("folders")).size());
    }

    @Test
    void getListOfMetadataFromPath_WithIgnoredFile_SkipsIt() throws Exception {
        Path ignoredPath = Path.of(".DS_Store");
        List<Path> paths = List.of(ignoredPath);

        when(fileUtility.isIgnoredFile(".DS_Store")).thenReturn(true);

        Map<String, List<?>> result = fileSystemService.getListOfMetadataFromPath(paths);

        assertTrue(((List<?>) result.get("files")).isEmpty());
        assertTrue(((List<?>) result.get("folders")).isEmpty());
        verifyNoInteractions(encodingUtility);
    }

    @Test
    void getListOfMetadataFromPath_WithSort_DelegatesToSortUtility() throws Exception {
        Path filePath = Files.createFile(tempDir.resolve("encoded_file1"));
        List<Path> paths = List.of(filePath);

        when(encodingUtility.decodedBase32SplitArray("encoded_file1")).thenReturn(new String[]{"1", "doc.txt", "1"});
        when(fileUtility.isIgnoredFile(anyString())).thenReturn(false);
        when(userSession.getId()).thenReturn(1L);

        FileMetadata fileMetadata = new FileMetadata("encoded", 1L, 1L, "text/plain", 100L);
        fileMetadata.setId(1L);
        when(sqLiteDAO.queryFileMetadata(1L, 1L)).thenReturn(fileMetadata);

        Map<String, List<?>> expected = Map.of("files", List.of(), "folders", List.of());
        when(sortAndFilterUtility.sortFileList(eq(SortListEnum.ALPHABETICAL), any(), any()))
                .thenReturn((Map) expected);

        Map<String, List<?>> result = fileSystemService.getListOfMetadataFromPath(paths, SortListEnum.ALPHABETICAL);

        assertNotNull(result);
        verify(sortAndFilterUtility).sortFileList(eq(SortListEnum.ALPHABETICAL), any(), any());
    }

    @Test
    void removeFile_WhenExists_DeletesAndReturnsPath() throws Exception {
        FileMetadata file = new FileMetadata("encoded_name.bin", 5L, 1L, "application/octet-stream", 200L);
        file.setId(10L);

        Path userPath = Files.createDirectories(tempDir.resolve("user/path"));
        Path realFile = Files.writeString(userPath.resolve("encoded_name.bin"), "data");

        when(pathUtility.getFolderPath(5L)).thenReturn("user/path");
        when(fileUtility.returnPathIfItExists(Paths.get("user", "path", "encoded_name.bin").toString())).thenReturn(realFile);

        String result = fileSystemService.removeFile(file);

        assertNotNull(result);
        assertTrue(Files.notExists(realFile));
        verify(sqLiteDAO).deleteFile(file);
        verify(thumbnailRepository).deleteThumbnailByFileID(10L);
    }

    @Test
    void removeFile_WhenDeleteFails_ThrowsException() throws Exception {
        FileMetadata file = new FileMetadata("enc.txt", 1L, 1L, "text/plain", 50L);

        Path userPath = Files.createDirectories(tempDir.resolve("user/path"));
        Path realFile = userPath.resolve("enc.txt");

        when(pathUtility.getFolderPath(1L)).thenReturn("user/path");
        when(fileUtility.returnPathIfItExists(Paths.get("user", "path", "enc.txt").toString())).thenReturn(realFile);

        assertThrows(java.nio.file.FileSystemException.class, () -> fileSystemService.removeFile(file));
    }

    @Test
    void updateFileName_Success_ReturnsNewPath() throws Exception {
        String encodedOldName = encode(1, "oldname.txt", 1);
        String encodedNewName = encode(1, "newname.txt", 1);
        FileMetadata file = new FileMetadata(encodedOldName, 1L, 1L, "text/plain", 100L);
        file.setId(1L);

        Path folderPath = Files.createDirectories(tempDir.resolve("user/folder"));
        Files.writeString(folderPath.resolve(encodedOldName), "content");

        when(pathUtility.getFolderPath(1L)).thenReturn("user/folder");
        when(pathUtility.getFullPathToString("user/folder")).thenReturn(folderPath.toString());
        when(fileUtility.hasFileExtension("newname")).thenReturn(false);
        when(encodingUtility.decodedBase32SplitArray(encodedOldName)).thenReturn(new String[]{"1", "oldname.txt", "1"});
        when(fileUtility.getFileExtension("oldname.txt")).thenReturn(".txt");
        when(encodingUtility.encodeBase32FolderName(1L, "newname.txt", 1L)).thenReturn(encodedNewName);
        when(fileUtility.checkIfFileExistsDecodeNames("user/folder", "newname.txt")).thenReturn(false);
        when(fileUtility.getMimeTypeFromExtensionUsingTikaCore(any())).thenReturn("text/plain");
        when(fileUtility.getFileExtension("oldname.txt")).thenReturn(".txt");

        String result = fileSystemService.updateFileName("newname", file);

        assertNotNull(result);
        verify(sqLiteDAO).saveFile(file);
    }

    @Test
    void updateFolderName_Success_ReturnsNewPath() throws Exception {
        String encodedOldFolder = encode(2, "oldfolder", 1);
        String encodedNewFolder = encode(2, "newname", 1);
        FolderMetadata folder = new FolderMetadata(encodedOldFolder, "0/5");
        folder.setId(5L);
        folder.setUserid(1L);

        Path userDir = Files.createDirectories(tempDir.resolve("user"));
        Path oldDir = Files.createDirectory(userDir.resolve(encodedOldFolder));

        String userFolderPath = Paths.get("user", encodedOldFolder).toString();
        when(pathUtility.resolvePathFromIdString("0/5")).thenReturn(userFolderPath);
        when(fileUtility.returnPathIfItExists(userFolderPath)).thenReturn(oldDir);
        when(pathUtility.returnParentFolderPathFromFolderID(5L)).thenReturn("user");
        when(fileUtility.checkIfFileExistsDecodeNames("user", "newname")).thenReturn(false);
        when(encodingUtility.encodeBase32FolderName(5L, "newname", 1L)).thenReturn(encodedNewFolder);
        when(fileUtility.returnPathIfItsNotADuplicate(anyString())).thenReturn(userDir.resolve(encodedNewFolder));

        String result = fileSystemService.updateFolderName("newname", folder);

        assertNotNull(result);
        verify(sqLiteDAO).saveFolder(folder);
    }

    @Test
    void updateFolderName_Duplicate_ThrowsException() throws Exception {
        FolderMetadata folder = new FolderMetadata("enc", "0/5");
        folder.setId(5L);

        when(pathUtility.resolvePathFromIdString("0/5")).thenReturn(Paths.get("user", "folder").toString());
        when(fileUtility.returnPathIfItExists(Paths.get("user", "folder").toString())).thenReturn(Path.of("/root/user/folder"));
        when(pathUtility.returnParentFolderPathFromFolderID(5L)).thenReturn("user");
        when(fileUtility.checkIfFileExistsDecodeNames("user", "existing")).thenReturn(true);

        assertThrows(Exception.class,
                () -> fileSystemService.updateFolderName("existing", folder));
    }

    @Test
    void moveFile_Success_ReturnsDestination() throws Exception {
        FileMetadata file = new FileMetadata("enc.txt", 5L, 1L, "text/plain", 100L);
        file.setId(10L);

        Path sourceDir = Files.createDirectories(tempDir.resolve("source/path"));
        Path destDir = Files.createDirectories(tempDir.resolve("dest/path"));
        Path sourceFile = Files.writeString(sourceDir.resolve("enc.txt"), "movable content");

        when(pathUtility.getFolderPath(5L)).thenReturn("source/path");
        when(pathUtility.getFolderPath(10L)).thenReturn("dest/path");
        when(pathUtility.getBasePathToString()).thenReturn(tempDir.toString());
        when(pathUtility.getFullPathToString("dest/path")).thenReturn(destDir.toString());

        String result = fileSystemService.moveFile(file, 10L);

        assertNotNull(result);
        assertTrue(Files.exists(destDir.resolve("enc.txt")));
        assertTrue(Files.notExists(sourceFile));
        verify(sqLiteDAO).saveFile(file);
    }

    @Test
    void moveFile_SourceNotFound_ThrowsException() throws Exception {
        FileMetadata file = new FileMetadata("missing.txt", 5L, 1L, "text/plain", 100L);

        when(pathUtility.getFolderPath(10L)).thenReturn("dest/path");
        when(pathUtility.getFolderPath(5L)).thenReturn("source/path");
        when(pathUtility.getBasePathToString()).thenReturn(tempDir.toString());
        when(pathUtility.getFullPathToString("dest/path")).thenReturn(tempDir.resolve("dest/path").toString());

        assertThrows(java.io.FileNotFoundException.class,
                () -> fileSystemService.moveFile(file, 10L));
    }

    @Test
    void moveFolder_Success_ReturnsNewPath() throws Exception {
        FolderMetadata folder = new FolderMetadata("enc_folder", "0/5/3");
        folder.setId(3L);
        folder.setUserid(1L);

        Path sourceDir = Files.createDirectories(tempDir.resolve("source/path/enc_folder"));
        Path destDir = Files.createDirectories(tempDir.resolve("dest/path"));

        when(pathUtility.getFolderPath(3L)).thenReturn(Paths.get("source", "path", "enc_folder").toString());
        when(fileUtility.returnPathIfItExists(Paths.get("source", "path", "enc_folder").toString())).thenReturn(sourceDir);
        when(pathUtility.getFolderPath(10L)).thenReturn(Paths.get("dest", "path").toString());
        when(fileUtility.returnPathIfItExists(Paths.get("dest", "path").toString())).thenReturn(destDir);
        when(sqLiteDAO.findAllStartsWithIdPath("0/5/3/", 0L)).thenReturn(List.of());
        when(sqLiteDAO.getIdPath(10L, 0L)).thenReturn("0/10");

        String result = fileSystemService.moveFolder(folder, 10L);

        assertNotNull(result);
        verify(sqLiteDAO).saveAllFolders(anyList());
    }

    @Test
    void getListOfMetadataFromPath_WithFilter_DelegatesToFilterUtility() throws Exception {
        Path filePath = Files.createFile(tempDir.resolve("encoded_f1"));
        List<Path> paths = List.of(filePath);

        when(encodingUtility.decodedBase32SplitArray("encoded_f1")).thenReturn(new String[]{"1", "doc.pdf", "1"});
        when(fileUtility.isIgnoredFile(anyString())).thenReturn(false);
        when(userSession.getId()).thenReturn(1L);

        FileMetadata fileMetadata = new FileMetadata("enc", 1L, 1L, "application/pdf", 200L);
        fileMetadata.setId(1L);
        when(sqLiteDAO.queryFileMetadata(1L, 1L)).thenReturn(fileMetadata);

        Map<String, List<?>> expected = Map.of("files", List.of(), "folders", List.of());
        when(sortAndFilterUtility.filterFileList(any(), any(), any(), anyString()))
                .thenReturn((Map) expected);

        Map<String, List<?>> result = fileSystemService.getListOfMetadataFromPath(
                paths, FilterListEnum.FILES_ONLY);

        assertNotNull(result);
        verify(sortAndFilterUtility).filterFileList(any(), any(), any(), anyString());
    }
}
