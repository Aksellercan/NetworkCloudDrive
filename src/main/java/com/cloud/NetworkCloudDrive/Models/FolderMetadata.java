package com.cloud.NetworkCloudDrive.Models;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

//TODO add folder permissions
//TODO Last updated

@Entity
public class FolderMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "path")
    private String path;

    @Column(name = "userid")
    private Long userid;

    @Column(name = "createdAt")
    @CreationTimestamp
    private Instant createdAt;

    public FolderMetadata(String name, String path) {
        this.name = name;
        this.path = path;
    }

    public FolderMetadata() {}

    public Long getUserid() {
        return userid;
    }

    public void setUserid(Long userid) {
        this.userid = userid;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return String
                .format(
                        "Folder Metadata:\nid: %d\nname: %s\npath: %s\nbelongs to: %d",
                        this.id, this.name, this.path, this.userid);
    }
}
