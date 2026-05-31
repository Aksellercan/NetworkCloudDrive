package com.cloud.NetworkCloudDrive.Utilities;

import com.cloud.NetworkCloudDrive.Models.DTO.FileListItemDTO;
import com.cloud.NetworkCloudDrive.Models.DTO.FolderListItemDTO;
import com.cloud.NetworkCloudDrive.Models.Enum.FilterListEnum;
import com.cloud.NetworkCloudDrive.Models.Enum.SortListEnum;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class SortAndFilterUtilityTest {

    private SortAndFilterUtility utility;
    private List<FileListItemDTO> files;
    private List<FolderListItemDTO> folders;

    @BeforeEach
    void setUp() {
        utility = new SortAndFilterUtility();

        FileListItemDTO fileA = createFile("alpha.txt", "text/plain", 100, Instant.parse("2025-01-01T00:00:00Z"));
        FileListItemDTO fileB = createFile("Beta.txt", "text/plain", 200, Instant.parse("2025-02-01T00:00:00Z"));
        FileListItemDTO fileC = createFile("gamma.txt", "image/png", 50, Instant.parse("2025-03-01T00:00:00Z"));
        files = List.of(fileA, fileB, fileC);

        FolderListItemDTO folderA = createFolder("Work", Instant.parse("2025-01-15T00:00:00Z"));
        FolderListItemDTO folderB = createFolder("archive", Instant.parse("2025-02-15T00:00:00Z"));
        folders = List.of(folderA, folderB);
    }

    private FileListItemDTO createFile(String name, String mimeType, long size, Instant createdAt) {
        FileListItemDTO f = new FileListItemDTO();
        f.setName(name);
        f.setMimeType(mimeType);
        f.setSize(size);
        f.setCreatedAt(createdAt);
        return f;
    }

    private FolderListItemDTO createFolder(String name, Instant createdAt) {
        FolderListItemDTO f = new FolderListItemDTO();
        f.setName(name);
        f.setCreatedAt(createdAt);
        return f;
    }

    @Test
    void sortDefault_ReturnsUnsorted() {
        Map<String, List<?>> result = utility.sortFileList(SortListEnum.DEFAULT, files.stream(), folders.stream());
        assertEquals(3, ((List<?>) result.get("files")).size());
        assertEquals(2, ((List<?>) result.get("folders")).size());
    }

    @Test
    void sortAlphabetical_ReturnsSortedAscending() {
        Map<String, List<?>> result = utility.sortFileList(SortListEnum.ALPHABETICAL, files.stream(), folders.stream());
        List<FileListItemDTO> sortedFiles = (List<FileListItemDTO>) result.get("files");
        assertEquals("alpha.txt", sortedFiles.get(0).getName());
        assertEquals("Beta.txt", sortedFiles.get(1).getName());
        assertEquals("gamma.txt", sortedFiles.get(2).getName());
    }

    @Test
    void sortReverseAlphabetical_ReturnsSortedDescending() {
        Map<String, List<?>> result = utility.sortFileList(SortListEnum.REVERSE_ALPHABETICAL, files.stream(), folders.stream());
        List<FileListItemDTO> sortedFiles = (List<FileListItemDTO>) result.get("files");
        assertEquals("gamma.txt", sortedFiles.get(0).getName());
        assertEquals("Beta.txt", sortedFiles.get(1).getName());
        assertEquals("alpha.txt", sortedFiles.get(2).getName());
    }

    @Test
    void sortNewest_ReturnsMostRecentFirst() {
        Map<String, List<?>> result = utility.sortFileList(SortListEnum.NEWEST, files.stream(), folders.stream());
        List<FileListItemDTO> sortedFiles = (List<FileListItemDTO>) result.get("files");
        assertEquals("gamma.txt", sortedFiles.get(0).getName());
        assertEquals("Beta.txt", sortedFiles.get(1).getName());
        assertEquals("alpha.txt", sortedFiles.get(2).getName());
    }

    @Test
    void sortOldest_ReturnsOldestFirst() {
        Map<String, List<?>> result = utility.sortFileList(SortListEnum.OLDEST, files.stream(), folders.stream());
        List<FileListItemDTO> sortedFiles = (List<FileListItemDTO>) result.get("files");
        assertEquals("alpha.txt", sortedFiles.get(0).getName());
        assertEquals("Beta.txt", sortedFiles.get(1).getName());
        assertEquals("gamma.txt", sortedFiles.get(2).getName());
    }

    @Test
    void sortFoldersFirst_ReturnsFoldersBeforeFiles() {
        Map<String, List<?>> result = utility.sortFileList(SortListEnum.FOLDERS_FIRST, files.stream(), folders.stream());
        assertTrue(result.containsKey("folders"));
        assertTrue(result.containsKey("files"));
        List<?> folderList = (List<?>) result.get("folders");
        List<?> fileList = (List<?>) result.get("files");
        assertEquals(2, folderList.size());
        assertEquals(3, fileList.size());
    }

    @Test
    void sortSize_ReturnsLargestFirst() {
        Map<String, List<?>> result = utility.sortFileList(SortListEnum.SIZE, files.stream(), folders.stream());
        List<FileListItemDTO> sortedFiles = (List<FileListItemDTO>) result.get("files");
        assertEquals(200, sortedFiles.get(0).getSize());
        assertEquals(100, sortedFiles.get(1).getSize());
        assertEquals(50, sortedFiles.get(2).getSize());
    }

    @Test
    void sortSizeLowest_ReturnsSmallestFirst() {
        Map<String, List<?>> result = utility.sortFileList(SortListEnum.SIZE_LOWEST, files.stream(), folders.stream());
        List<FileListItemDTO> sortedFiles = (List<FileListItemDTO>) result.get("files");
        assertEquals(50, sortedFiles.get(0).getSize());
        assertEquals(100, sortedFiles.get(1).getSize());
        assertEquals(200, sortedFiles.get(2).getSize());
    }

    @Test
    void filterFilesOnly_ReturnsOnlyFiles() {
        Map<String, List<?>> result = utility.filterFileList(FilterListEnum.FILES_ONLY, files.stream(), folders.stream(), "");
        assertTrue(result.containsKey("files"));
        assertFalse(result.containsKey("folders"));
        assertEquals(3, ((List<?>) result.get("files")).size());
    }

    @Test
    void filterFoldersOnly_ReturnsOnlyFolders() {
        Map<String, List<?>> result = utility.filterFileList(FilterListEnum.FOLDERS_ONLY, files.stream(), folders.stream(), "");
        assertFalse(result.containsKey("files"));
        assertTrue(result.containsKey("folders"));
        assertEquals(2, ((List<?>) result.get("folders")).size());
    }

    @Test
    void filterByType_MatchingType_ReturnsFilteredFiles() {
        Map<String, List<?>> result = utility.filterFileList(FilterListEnum.TYPE, files.stream(), folders.stream(), "image/png");
        List<FileListItemDTO> filtered = (List<FileListItemDTO>) result.get("files");
        assertEquals(1, filtered.size());
        assertEquals("gamma.txt", filtered.get(0).getName());
    }

    @Test
    void filterByType_NoMatch_ReturnsEmptyFiles() {
        Map<String, List<?>> result = utility.filterFileList(FilterListEnum.TYPE, files.stream(), folders.stream(), "application/pdf");
        List<FileListItemDTO> filtered = (List<FileListItemDTO>) result.get("files");
        assertTrue(filtered.isEmpty());
    }

    @Test
    void filterByKeyword_MatchingFilesAndFolders() {
        Map<String, List<?>> result = utility.filterFileList(FilterListEnum.KEYWORD, files.stream(), folders.stream(), "alpha");
        List<FileListItemDTO> filteredFiles = (List<FileListItemDTO>) result.get("files");
        assertEquals(1, filteredFiles.size());
        assertEquals("alpha.txt", filteredFiles.get(0).getName());
    }

    @Test
    void filterByKeyword_NoMatch_ReturnsEmpty() {
        Map<String, List<?>> result = utility.filterFileList(FilterListEnum.KEYWORD, files.stream(), folders.stream(), "nonexistent");
        assertTrue(((List<?>) result.get("files")).isEmpty());
        assertTrue(((List<?>) result.get("folders")).isEmpty());
    }

    @Test
    void sortWithEmptyLists_DoesNotThrow() {
        Map<String, List<?>> result = utility.sortFileList(SortListEnum.ALPHABETICAL, List.<FileListItemDTO>of().stream(), List.<FolderListItemDTO>of().stream());
        assertTrue(((List<?>) result.get("files")).isEmpty());
        assertTrue(((List<?>) result.get("folders")).isEmpty());
    }

    @Test
    void filterWithEmptyLists_DoesNotThrow() {
        Map<String, List<?>> result = utility.filterFileList(FilterListEnum.FILES_ONLY, List.<FileListItemDTO>of().stream(), List.<FolderListItemDTO>of().stream(), "");
        assertTrue(((List<?>) result.get("files")).isEmpty());
    }
}
