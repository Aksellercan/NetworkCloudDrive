package com.cloud.repository;

import com.cloud.entity.FileDetails;
import org.springframework.stereotype.Repository;

import java.io.File;

@Repository
// Just name parameters search in current directory
public interface FileRepository {
    //get file type
    String getFileType(String name);
    String getFileType(String name, String path);
    //Files
    File getFile(String name);
    FileDetails getFile(String name, String path);
    //Folders
    File getFolder(String name);
    File getFolder(String name, String path);
}
