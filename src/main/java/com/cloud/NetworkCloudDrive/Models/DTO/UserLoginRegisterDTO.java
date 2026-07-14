package com.cloud.NetworkCloudDrive.Models.DTO;

public class UserLoginRegisterDTO {
    private String name;
    private String mail;
    private String password;

    public UserLoginRegisterDTO() {
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
