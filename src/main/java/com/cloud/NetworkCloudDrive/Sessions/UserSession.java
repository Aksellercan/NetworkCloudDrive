package com.cloud.NetworkCloudDrive.Sessions;

import com.cloud.NetworkCloudDrive.DAO.SQLiteDAO;
import com.cloud.NetworkCloudDrive.Models.DTO.CurrentUserDTO;
import com.cloud.NetworkCloudDrive.Models.Enum.UserRole;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.env.Environment;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.annotation.RequestScope;

//TODO temporarily use RequestScope instead of SessionScope (Reason: Session won't update on Postman/Yaak/Bruno. Should work for browser)
@RequestScope
//@SessionScope
@Component
public class UserSession {
    private long id;
    private String name;
    private String mail;
    private UserRole role;
    private final SQLiteDAO sqLiteDAO;
    private final Logger logger = LoggerFactory.getLogger(UserSession.class);
    private final Environment env;


    public UserSession(SQLiteDAO sqLiteDAO, Environment env) {
        this.sqLiteDAO = sqLiteDAO;
        this.env = env;
    }

    @PostConstruct
    @Transactional
    public CurrentUserDTO initializeUserSessionDetails() throws UsernameNotFoundException {
        if (Boolean.parseBoolean(env.getProperty("unit-test"))) return new CurrentUserDTO();
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) throw new UsernameNotFoundException("Invalid authentication");
        if (this.name != null) {
            if (this.name.equals(auth.getName())) {
                return new CurrentUserDTO(this.id, this.name, this.mail, this.role);
            }
        }
        CurrentUserDTO userDetailsDTO = sqLiteDAO.getUserIDNameAndRoleByMail(auth.getName());
        this.id = userDetailsDTO.getId();
        this.name = userDetailsDTO.getName();
        this.mail = userDetailsDTO.getMail();
        this.role = userDetailsDTO.getRole();
        logger.debug("SESSION INFO: ID={} NAME={} MAIL={} ROLE={}", //debug
                userDetailsDTO.getId(), userDetailsDTO.getName(), userDetailsDTO.getMail(), userDetailsDTO.getRole());
        return userDetailsDTO;
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
}
