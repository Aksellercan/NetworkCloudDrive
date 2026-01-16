package com.cloud.NetworkCloudDrive.Models.DTO;

import com.cloud.NetworkCloudDrive.Enum.UserRole;
import com.cloud.NetworkCloudDrive.Models.UserEntity;

import java.time.Instant;

public class CurrentUserDTO {
    private long id;
    private String name;
    private String mail;
    private UserRole role;
    private Instant lastLogin;

    public CurrentUserDTO(long id, String name, String mail, UserRole role, Instant lastLogin) {
        this.id = id;
        this.name = name;
        this.mail = mail;
        this.role = role;
        this.lastLogin = lastLogin;
    }

    public CurrentUserDTO(long id, String name, String mail, UserRole role) {
        this.id = id;
        this.name = name;
        this.mail = mail;
        this.role = role;
    }

    public CurrentUserDTO(UserEntity userEntity) {
        this.id = userEntity.getId();
        this.name = userEntity.getName();
        this.mail = userEntity.getMail();
        this.role = userEntity.getRole();
        this.lastLogin = userEntity.getLastLogin();
    }

    public CurrentUserDTO() {
    }

    public long getId() {
        return id;
    }
    public void setId(long id) {
        this.id = id;
    }
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getMail() {
        return mail;
    }
    public void setMail(String mail) {
        this.mail = mail;
    }
    public UserRole getRole() {
        return role;
    }
    public void setRole(UserRole role) {
        this.role = role;
    }
    public Instant getLastLogin() {
        return lastLogin;
    }
    public void setLastLogin(Instant lastLogin) {
        this.lastLogin = lastLogin;
    }
}
