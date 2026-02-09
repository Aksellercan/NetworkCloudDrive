package com.cloud.NetworkCloudDrive.Utilities.Security;

import com.cloud.NetworkCloudDrive.DAO.SQLiteDAO;
import com.cloud.NetworkCloudDrive.Models.Enum.UserRole;
import com.cloud.NetworkCloudDrive.Models.UserEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class SecurityUtility implements UserDetailsService {
    private final SQLiteDAO sqLiteDAO;

    public SecurityUtility(SQLiteDAO sqLiteDAO) {
        this.sqLiteDAO = sqLiteDAO;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity returnedUser = sqLiteDAO.findUserByMail(username.toLowerCase());
        return User.builder().
                username(returnedUser.getMail()).
                password(returnedUser.getPassword()).
                roles(getUserRole(returnedUser.getRole())).
                build();
    }

    private String getUserRole(UserRole userRole) {
        switch (userRole) {
            case NORMAL_USER -> {
                return "USER";
            }
            case ADMIN -> {
                return "ADMIN";
            }
        }
        return "GUEST";
    }
}
