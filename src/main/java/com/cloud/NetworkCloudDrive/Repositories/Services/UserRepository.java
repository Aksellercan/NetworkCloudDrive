package com.cloud.NetworkCloudDrive.Repositories.Services;

import com.cloud.NetworkCloudDrive.Models.DTO.CurrentUserDTO;
import com.cloud.NetworkCloudDrive.Models.Data.DeletionResults;
import com.cloud.NetworkCloudDrive.Models.UserEntity;

import java.io.IOException;
import java.sql.SQLException;

public interface UserRepository {
    boolean loginUser(String name, String mail, String password) throws SQLException;
    UserEntity registerUser(String name, String mail, String password) throws SecurityException;
    CurrentUserDTO currentUserDetails(String mail);
    CurrentUserDTO updatePassword(UserEntity user, String newPassword);
    CurrentUserDTO updateMail(UserEntity user, String newMail) throws IOException;
    CurrentUserDTO updateName(UserEntity user, String newName) throws IOException;
    DeletionResults deleteUser(UserEntity user) throws IOException, SQLException;
    boolean elevateUserPrivileges();
}
