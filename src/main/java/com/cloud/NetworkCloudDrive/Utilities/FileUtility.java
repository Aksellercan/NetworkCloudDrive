package com.cloud.NetworkCloudDrive.Utilities;

import com.cloud.NetworkCloudDrive.Properties.FileStorageProperties;
import com.cloud.NetworkCloudDrive.Properties.IgnoreFileListProperties;
import org.apache.tika.Tika;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class FileUtility {
    private final FileStorageProperties fileStorageProperties;
    private final EncodingUtility encodingUtility;
    private final Logger logger = LoggerFactory.getLogger(FileUtility.class);
    private final IgnoreFileListProperties ignoreFileListProperties;

    public FileUtility(
            FileStorageProperties fileStorageProperties,
            EncodingUtility encodingUtility,
            IgnoreFileListProperties ignoreFileListProperties) {
        this.fileStorageProperties = fileStorageProperties;
        this.encodingUtility = encodingUtility;
        this.ignoreFileListProperties = ignoreFileListProperties;
    }

    /**
     * All Paths from directory
     * @param dir   starting directory
     * @param reverse   reverse order
     * @return  List of Path's starting and including from directory
     * @throws IOException  if path is invalid or does not exist
     */
    public List<Path> walkFsTree(Path dir, boolean reverse) throws IOException {
        try (Stream<Path> fileTree = Files.walk(dir)) {
            return (reverse ? fileTree.sorted(Comparator.reverseOrder()) : fileTree).toList();
        } catch (IOException e) {
            throw new IOException("Failed to walk file tree. " + e.getMessage());
        }
    }

    public List<Path> returnFilesInDirectory(Path dir, boolean reverse, Predicate<Path> pathFilter) throws IOException {
        return walkFsTree(dir, reverse).stream().filter(pathFilter).collect(Collectors.toList());
    }

    /**
     * Returns file if it's not a duplicate
     * @param path  file path to check
     * @return  file if it's not a duplicate
     * @throws FileNotFoundException    if file is a duplicate or not found
     */
    public Path returnPathIfItsNotADuplicate(String path) throws FileNotFoundException {
        Path checkDuplicate = Paths.get(path);
        if (Files.exists(checkDuplicate))
            throw new FileNotFoundException(String.format("%s with name %s already exists at %s",
                    (Files.isRegularFile(checkDuplicate) ? "File" : "Folder"), checkDuplicate.getFileName(), checkDuplicate));
        return checkDuplicate;
    }

    /**
     * Returns if file exists at path
     * @param path  file path to check
     * @return  file if it exists
     * @throws FileNotFoundException    if file does not exist at path
     */
    public File returnFileIfItExists(String path) throws FileNotFoundException {
        File checkExists = new File(fileStorageProperties.getFullPath(path));
        if (!Files.exists(checkExists.toPath()))
            throw new FileNotFoundException(String.format("%s does not exist at path %s",
                    (checkExists.isFile() ? "File" : "Folder"),checkExists.getPath()));
        return checkExists;
    }

    /**
     * Checks if file is a duplicate
     * @param filePath  filepath to start decoding from
     * @param decodedFileName   decoded filename
     * @return  true if no match found, otherwise false
     * @throws IOException  if filepath is invalid
     */
    public boolean checkIfFileExistsDecodeNames(String filePath, String decodedFileName) throws IOException {
        return getFileAndFolderPathsFromFolder(new File(fileStorageProperties.getFullPath(filePath))).stream().
                anyMatch(file -> !ignoreFileListProperties.isInIgnoreList(file.toFile().getName())
                        &&
                        encodingUtility.decodedBase32SplitArray(file.toFile().getName())[1].equals(decodedFileName));
    }

    /**
     * Returns if file exists at path uses NIO instead of IO
     * @param path  file path to check
     * @return  file if it exists
     * @throws FileNotFoundException    if file does not exist at path
     */
    public Path returnPathIfItExists(String path) throws FileNotFoundException {
        Path checkExists = Path.of(fileStorageProperties.getFullPath(path));
        if (!Files.exists(checkExists))
            throw new FileNotFoundException(String.format("%s does not exist at path %s",
                    (Files.isRegularFile(checkExists) ? "File" : "Folder"),checkExists));
        return checkExists;
    }

    /**
     * List of folders and files inside a directory
     * @param file    parent folder path to list
     * @return  List of paths for files and folders
     * @throws IOException  if path is invalid
     */
    public List<Path> getFileAndFolderPathsFromFolder(File file) throws IOException {
        List<Path> fileList;
        logger.info("full path {}", file.getPath());
        try (Stream<Path> stream = Files.list(file.toPath())) {
            fileList = stream.toList();
        }
        return fileList;
    }

    /**
     * Returns MimeType of file uses Apache Tika-Core dependency
     * @param file  File object
     * @return  MimeType of file
     * @throws IOException  If an I/O error occurs
     */
    public String getMimeTypeFromExtensionUsingTikaCore(File file) throws IOException {
        logger.debug("[TIKA-CORE] File at path absolute {}, {}", file.getPath(), file);
        return new Tika().detect(file);
    }

    /**
     * Returns file extension
     * @param fileName  filename with extension
     * @return  Empty string if no extension is found, returns extension including "."
     */
    public String getFileExtension(String fileName) {
        int i = fileName.lastIndexOf(".");
        return i > 0 ? fileName.substring(i) : "";
    }

    /**
     * Checks if filename has extension
     * @param filename  filename to examine
     * @return  true if filename has extension, otherwise false
     */
    public boolean hasFileExtension(String filename) {
        return !getFileExtension(filename).isEmpty();
    }
}
