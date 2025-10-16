package com.cloud.entity;

import com.cloud.enumurator.FileTypes;

/*
Idea is: a folder is a file, therefore has the same attributes as a file. EXCEPT its filetype is a folder

file (parent) -> folder (child)
 */
public class Folders extends Files {
    public Folders (String name, String path) {
        super(name, path, FileTypes.jpeg);
    }
}
