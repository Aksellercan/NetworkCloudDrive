package com.cloud.service;

import com.cloud.enumurator.FileTypes;
import com.cloud.repository.FileRepository;

import java.io.File;

public class FileService implements FileRepository {

    @Override
    public FileTypes getFileType(String name) {
        return null;
    }

    @Override
    public FileTypes getFileType(String name, String path) {
        return null;
    }

    @Override
    public File getFile(String name) {
        File getFile = new File("");
        if (!getFile.exists()) {
            return null;
        }
        return getFile;
    }

    @Override
    public File getFile(String name, String path) {
        File getFile = new File(path);
        if (!getFile.exists()) {
            return null;
        }
        return getFile;
    }

    @Override
    public File getFolder(String name) {
        File getFolder = new File("");
        if (!getFolder.exists() && !getFolder.isFile()) {
            return null;
        }
        return getFolder;
    }

    @Override
    public File getFolder(String name, String path) {
        File getFolder = new File("");
        if (!getFolder.exists() && !getFolder.isFile()) {
            return null;
        }
        return getFolder;
    }
}
