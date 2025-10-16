package com.cloud.repository;

import com.cloud.entity.Files;
import com.cloud.enumurator.FileTypes;

// Just name parameters search in current directory
public interface FileRepository {
    //get file type
    FileTypes getFileType(String name);
    FileTypes getFileTypes(String name, String path);
    //Files
    Files getFile(String name);
    Files getFile(String name, String path);
    //Folders
    Files getFolder(String name);
    Files getFolder(String name, String path);
}
