package com.cloud.service;

import com.cloud.entity.FileDetails;
import com.cloud.repository.FileRepository;
import org.springframework.stereotype.Service;

import java.io.File;

@Service
public class FileService implements FileRepository {

    @Override
    public String getFileType(String name) {
        return null;
    }

    @Override
    public String getFileType(String name, String path) {
        return null;
    }

    @Override
    public File getFile(String name) {
        File getFile = new File(name);
        if (!getFile.exists()) {
            return null;
        }
        return getFile;
    }

    @Override
    public FileDetails getFile(String name, String path) {
        File getFile = new File(path + File.separator + name);
        if (!getFile.exists()) {
            System.out.println("File could not be found! File: " + name + ", path: " + path);
            return null;
        }
        FileDetails returned = new FileDetails(getFile.getName(), getFile.getPath(), !getFile.isFile());
        returned.setSize(getFile.length()); //bytes
        return returned;
    }

    @Override
    public File getFolder(String name) {
        File getFolder = new File(name);
        if (!getFolder.exists() && getFolder.isFile()) {
            return null;
        }
        return getFolder;
    }

    @Override
    public File getFolder(String name, String path) {
        File getFolder = new File(path);
        if (!getFolder.exists() && getFolder.isFile()) {
            return null;
        }
        return getFolder;
    }
}
