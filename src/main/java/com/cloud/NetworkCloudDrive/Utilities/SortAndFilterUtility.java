package com.cloud.NetworkCloudDrive.Utilities;

import com.cloud.NetworkCloudDrive.Models.DTO.FileListItemDTO;
import com.cloud.NetworkCloudDrive.Models.DTO.FolderListItemDTO;
import com.cloud.NetworkCloudDrive.Models.Enum.FilterListEnum;
import com.cloud.NetworkCloudDrive.Models.Enum.SortListEnum;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class SortAndFilterUtility {
    public SortAndFilterUtility() {}

    public Map<String, List<?>> sortFileList(SortListEnum sortListEnum, List<FileListItemDTO> fileList, List<FolderListItemDTO> folderList) {
        switch (sortListEnum) {
            case ALPHABETICAL, REVERSE_ALPHABETICAL:
                return sortName(sortListEnum, fileList, folderList);
            case NEWEST:
                fileList.sort(Comparator.comparing(FileListItemDTO::getCreatedAt).reversed());
                folderList.sort(Comparator.comparing(FolderListItemDTO::getCreatedAt).reversed());
                break;
            case OLDEST:
                fileList.sort(Comparator.comparing(FileListItemDTO::getCreatedAt));
                folderList.sort(Comparator.comparing(FolderListItemDTO::getCreatedAt));
                break;
            case FOLDERS_FIRST:
                LinkedHashMap<String, List<?>> linkedHashMap = new LinkedHashMap<>();
                linkedHashMap.put("folders", folderList);
                linkedHashMap.put("files", fileList);
                return linkedHashMap;
            case SIZE:
                fileList.sort(Comparator.comparing(FileListItemDTO::getCreatedAt).reversed());
                break;
            case SIZE_LOWEST:
                fileList.sort(Comparator.comparing(FileListItemDTO::getCreatedAt));
                break;
        }
        return returnMappedLists(fileList, folderList);
    }

    private Map<String, List<?>> returnMappedLists(List<FileListItemDTO> fileListItemDTO, List<FolderListItemDTO> folderListItemDTO) {
        return Map.of(
                "files", fileListItemDTO,
                "folders", folderListItemDTO
        );
    }

    private Map<String, List<?>> sortName(SortListEnum sortOption, List<FileListItemDTO> fileListItemDTO, List<FolderListItemDTO> folderListItemDTO) {

        switch (sortOption) {
            case ALPHABETICAL:
                fileListItemDTO.sort(Comparator.naturalOrder());
                folderListItemDTO.sort(Comparator.naturalOrder());
                break;
            case REVERSE_ALPHABETICAL:
                fileListItemDTO.sort(Comparator.reverseOrder());
                folderListItemDTO.sort(Comparator.reverseOrder());
                break;
        }
        return returnMappedLists(fileListItemDTO, folderListItemDTO);
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
