package com.cloud.NetworkCloudDrive.Services;

import com.cloud.NetworkCloudDrive.Models.DTO.CurrentUserDTO;
import com.cloud.NetworkCloudDrive.Models.Enum.UserRole;
import com.cloud.NetworkCloudDrive.Models.UserEntity;
import com.cloud.NetworkCloudDrive.Repositories.UserRepository;
import com.cloud.NetworkCloudDrive.DAO.SQLiteDAO;
import com.cloud.NetworkCloudDrive.Utilities.EncodingUtility;
import com.cloud.NetworkCloudDrive.Utilities.FileUtility;
import com.cloud.NetworkCloudDrive.Utilities.UserUtility;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.SQLException;

@Service
public class UserService implements UserRepository {
    private final SQLiteDAO sqLiteDAO;
    private final FileUtility fileUtility;
    private final EncodingUtility encodingUtility;
    private final PasswordEncoder passwordEncoder;
    private final UserUtility userUtility;

    public UserService(
            SQLiteDAO sqLiteDAO,
            PasswordEncoder passwordEncoder,
            FileUtility fileUtility,
            EncodingUtility encodingUtility, UserUtility userUtility) {
        this.sqLiteDAO = sqLiteDAO;
        this.encodingUtility = encodingUtility;
        this.passwordEncoder = passwordEncoder;
        this.fileUtility = fileUtility;
        this.userUtility = userUtility;
    }

    @Override
    public CurrentUserDTO currentUserDetails(String mail) {
        UserEntity user = sqLiteDAO.findUserByMail(mail);
        return new CurrentUserDTO(user.getId(), user.getName(), user.getMail(), user.getRole(), user.getLastLogin());
    }

    @Override
    public UserEntity registerUser(String name, String mail, String password) throws SecurityException {
        UserEntity userEntityLogin = new UserEntity();
        userEntityLogin.setName(name);
        userEntityLogin.setMail(mail.toLowerCase());
        userEntityLogin.setPassword(passwordEncoder.encode(password));
        userEntityLogin.setRole(UserRole.GUEST);
        if (sqLiteDAO.checkIfUserExistsByMail(userEntityLogin.getMail())) {
            throw new SecurityException(String.format("User with name %s and mail %s already exists", name, mail));
        }
        return sqLiteDAO.saveUser(userEntityLogin);
    }

    @Override
    public boolean loginUser(String name, String mail, String password) throws SQLException {
        return passwordEncoder.matches(password, sqLiteDAO.findUserByMail(mail).getPassword());
    }

    @Override
    public CurrentUserDTO updatePassword(UserEntity user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        sqLiteDAO.saveUser(user);
        return new CurrentUserDTO(user.getId(), user.getName(), user.getMail(), user.getRole(), user.getLastLogin());
    }

    @Override
    public CurrentUserDTO updateName(UserEntity user, String newName) throws IOException {
        String oldEncoding = encodingUtility.encodeBase32UserFolderName(user.getId(), user.getName(), user.getMail());
        user.setName(newName);
        userUtility.updateUserDirectoryName(user.getId(), user.getName(), user.getMail(), oldEncoding);
        sqLiteDAO.saveUser(user);
        return new CurrentUserDTO(user.getId(), user.getName(), user.getMail(), user.getRole(), user.getLastLogin());
    }

    @Override
    public CurrentUserDTO updateMail(UserEntity user, String newMail) throws IOException {
        String oldEncoding = encodingUtility.encodeBase32UserFolderName(user.getId(), user.getName(), user.getMail());
        user.setMail(newMail);
        userUtility.updateUserDirectoryName(user.getId(), user.getName(), user.getMail(), oldEncoding);
        sqLiteDAO.saveUser(user);
        return new CurrentUserDTO(user.getId(), user.getName(), user.getMail(), user.getRole(), user.getLastLogin());
    }

    //TODO delete function should delete user folder
    @Override
    public void deleteUser(UserEntity user) {
        sqLiteDAO.deleteUser(user);
    }

    @Override
    public boolean elevateUserPrivileges() {
        return false;
    }
}
