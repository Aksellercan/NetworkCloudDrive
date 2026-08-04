package com.cloud.NetworkCloudDrive.Utilities;

import com.cloud.NetworkCloudDrive.Models.DTO.FileListItemDTO;
import com.cloud.NetworkCloudDrive.Models.DTO.FolderListItemDTO;
import com.cloud.NetworkCloudDrive.Models.Enum.FilterListEnum;
import com.cloud.NetworkCloudDrive.Models.Enum.SortListEnum;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.stream.Stream;

@Component
public class SortAndFilterUtility {

    public SortAndFilterUtility() {
    }

    public Map<String, List<?>> sortFileList(SortListEnum sortListEnum, Stream<FileListItemDTO> fileList, Stream<FolderListItemDTO> folderList) {
        Comparator<FileListItemDTO> fileListItemDTOComparator = null;
        Comparator<FolderListItemDTO> folderListItemDTOComparator = null;
        switch (sortListEnum) {
            case ALPHABETICAL:
                fileListItemDTOComparator = Comparator.comparing(f -> f.getName().toLowerCase());
                folderListItemDTOComparator = Comparator.comparing(fl -> fl.getName().toLowerCase());
                break;
            case REVERSE_ALPHABETICAL:
                fileListItemDTOComparator = Comparator.comparing(f -> f.getName().toLowerCase(), Comparator.reverseOrder());
                folderListItemDTOComparator = Comparator.comparing(fl -> fl.getName().toLowerCase(), Comparator.reverseOrder());
                break;
            case NEWEST:
                fileListItemDTOComparator = Comparator.comparing(FileListItemDTO::getCreatedAt, Comparator.reverseOrder());
                folderListItemDTOComparator = Comparator.comparing(FolderListItemDTO::getCreatedAt, Comparator.reverseOrder());
                break;
            case OLDEST:
                fileListItemDTOComparator = Comparator.comparing(FileListItemDTO::getCreatedAt);
                folderListItemDTOComparator = Comparator.comparing(FolderListItemDTO::getCreatedAt);
                break;
            case ACCESSED_AFTER:
                fileListItemDTOComparator = Comparator.comparing(FileListItemDTO::getLastAccessedAt, Comparator.reverseOrder());
                folderListItemDTOComparator = Comparator.comparing(FolderListItemDTO::getLastAccessedAt, Comparator.reverseOrder());
                break;
            case ACCESSED_BEFORE:
                fileListItemDTOComparator = Comparator.comparing(FileListItemDTO::getLastAccessedAt);
                folderListItemDTOComparator = Comparator.comparing(FolderListItemDTO::getLastAccessedAt);
                break;
            case FOLDERS_FIRST:
                LinkedHashMap<String, List<?>> linkedHashMap = new LinkedHashMap<>();
                linkedHashMap.put("folders", folderList.toList());
                linkedHashMap.put("files", fileList.toList());
                return linkedHashMap;
            case SIZE:
                fileListItemDTOComparator = Comparator.comparingLong(FileListItemDTO::getSize).reversed();
                break;
            case SIZE_LOWEST:
                fileListItemDTOComparator = Comparator.comparingLong(FileListItemDTO::getSize);
                break;
            default:
                return Map.of(
                        "files", fileList.toList(),
                        "folders", folderList.toList()
                );
        }
        return Map.of(
                "files", (fileListItemDTOComparator != null ? fileList.sorted(fileListItemDTOComparator).toList() : fileList.toList()),
                "folders", (folderListItemDTOComparator != null ? folderList.sorted(folderListItemDTOComparator).toList() : folderList.toList())
        );
    }

    public Map<String, List<?>> filterFileList(FilterListEnum filterListEnum, Stream<FileListItemDTO> fileList, Stream<FolderListItemDTO> folderList, String filterCase) {
        Predicate<FileListItemDTO> fileListItemDTOPredicate = null;
        Predicate<FolderListItemDTO> folderListItemDTOPredicate = null;
        switch (filterListEnum) {
            case FILES_ONLY:
                return Map.of("files", fileList.toList());
            case FOLDERS_ONLY:
                return Map.of("folders", folderList.toList());
            case TYPE:
                fileListItemDTOPredicate = (f -> f.getMimeType().equals(filterCase));
                break;
            case KEYWORD:
                fileListItemDTOPredicate = (f -> f.getName().contains(filterCase));
                folderListItemDTOPredicate = (fl -> fl.getName().contains(filterCase));
                break;
            default:
                return Map.of(
                        "files", fileList.toList(),
                        "folders", folderList.toList()
                );
        }
        return Map.of(
                "files", (fileListItemDTOPredicate != null ? fileList.filter(fileListItemDTOPredicate).toList() : fileList.toList()),
                "folders", (folderListItemDTOPredicate != null ? folderList.filter(folderListItemDTOPredicate).toList() : folderList.toList())
        );
    }
}
