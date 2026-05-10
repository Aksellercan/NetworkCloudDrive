package com.cloud.NetworkCloudDrive.Services;

import com.cloud.NetworkCloudDrive.Models.DTO.CurrentUserDTO;
import com.cloud.NetworkCloudDrive.Models.Data.DeletionResults;
import com.cloud.NetworkCloudDrive.Models.Enum.UserRole;
import com.cloud.NetworkCloudDrive.Models.UserEntity;
import com.cloud.NetworkCloudDrive.Repositories.Services.UserRepository;
import com.cloud.NetworkCloudDrive.DAO.SQLiteDAO;
import com.cloud.NetworkCloudDrive.Repositories.Tasks.ThumbnailRepository;
import com.cloud.NetworkCloudDrive.Utilities.FileUtility;
import com.cloud.NetworkCloudDrive.Utilities.Security.EncodingUtility;
import com.cloud.NetworkCloudDrive.Utilities.UserUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.sql.SQLException;

@Service
public class UserService implements UserRepository {
    private final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final SQLiteDAO sqLiteDAO;
    private final EncodingUtility encodingUtility;
    private final PasswordEncoder passwordEncoder;
    private final UserUtility userUtility;
    private final ThumbnailRepository thumbnailRepository;
    private final FileUtility fileUtility;

    public UserService(
            SQLiteDAO sqLiteDAO,
            PasswordEncoder passwordEncoder,
            EncodingUtility encodingUtility,
            UserUtility userUtility, ThumbnailRepository thumbnailRepository, FileUtility fileUtility) {
        this.sqLiteDAO = sqLiteDAO;
        this.encodingUtility = encodingUtility;
        this.passwordEncoder = passwordEncoder;
        this.userUtility = userUtility;
        this.thumbnailRepository = thumbnailRepository;
        this.fileUtility = fileUtility;
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

    @Override
    public DeletionResults deleteUser(UserEntity user) throws IOException, SQLException {
        thumbnailRepository.deleteAllThumbnails();
        DeletionResults deletionResults = fileUtility.deleteFolders(userUtility.returnUserFolderasPath());
        sqLiteDAO.deleteAllUserRelatedEntries(user.getId());
        sqLiteDAO.deleteUser(user);
        return deletionResults;
    }

    @Override
    public boolean elevateUserPrivileges() {
        return false;
    }
}
