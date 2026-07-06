package com.cloud.NetworkCloudDrive.Models.DTO;

import com.cloud.NetworkCloudDrive.Sessions.UserSession;

public class UserDTO {
    private long userId;
    private String userName;
    private String userEmail;

    public UserDTO(long userId, String userName, String userEmail) {
        this.userId = userId;
        this.userName = userName;
        this.userEmail = userEmail;
    }

    public UserDTO(UserSession userSession) {
        this.userId = userSession.getId();
        this.userName = userSession.getName();
        this.userEmail = userSession.getMail();
    }

    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    @Override
    public String toString() {
        return "UserBackgroundTaskDTO{" +
                "userId=" + userId +
                ", userName='" + userName + '\'' +
                ", userEmail='" + userEmail + '\'' +
                '}';
    }
}
