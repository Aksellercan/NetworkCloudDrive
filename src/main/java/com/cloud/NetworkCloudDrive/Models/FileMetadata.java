package com.cloud.NetworkCloudDrive.Models;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;

//DONE Last updated

@Entity
public class FileMetadata {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "folderId")
    private Long folderId;

    @Column(name = "userid")
    private Long userid;

    @Column(name = "mimiType")
    private String mimiType;

    private Long size;

    @Column(name = "createdAt")
    @CreationTimestamp
    private Instant createdAt;

    @Column(name = "hasThumbnail")
    private boolean hasThumbnail = false;

    @Column(name = "lastUpdated")
    private Instant lastUpdated;

    public FileMetadata(String name, Long folderId, Long userid, String mimiType, Long size) {
        this.name = name;
        this.folderId = folderId;
        this.userid = userid;
        this.mimiType = mimiType;
        this.size = size;
    }

    public FileMetadata() {}

    public Long getUserid() {
        return userid;
    }

    public void setUserid(Long userid) {
        this.userid = userid;
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

    public Long getFolderId() {
        return folderId;
    }

    public void setFolderId(Long folderId) {
        this.folderId = folderId;
    }

    public Long getId() {
        return id;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }

    public void updateLastUpdated() {
        this.lastUpdated = Instant.now();
    }

    public void setLastUpdated(Instant lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public String getMimiType() {
        return mimiType;
    }

    public void setMimiType(String mimiType) {
        this.mimiType = mimiType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isHasThumbnail() {
        return hasThumbnail;
    }

    public void setHasThumbnail(boolean hasThumbnail) {
        this.hasThumbnail = hasThumbnail;
    }

    @Override
    public String toString() {
        return "FileMetadata{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", folderId=" + folderId +
                ", userid=" + userid +
                ", mimiType='" + mimiType + '\'' +
                ", size=" + size +
                ", createdAt=" + createdAt +
                ", hasThumbnail=" + hasThumbnail +
                ", lastUpdated=" + lastUpdated +
                '}';
    }
}
