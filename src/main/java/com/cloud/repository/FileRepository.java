package com.cloud.repository;

import java.io.File;

// Just name parameters search in current directory
public interface FileRepository {
    //get file type
    String getFileType(String name);
    String getFileType(String name, String path);
    //Files
    File getFile(String name);
    File getFile(String name, String path);
    //Folders
    File getFolder(String name);
    File getFolder(String name, String path);
}
