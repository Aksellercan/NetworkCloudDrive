package com.cloud.NetworkCloudDrive.Controllers;

import com.cloud.NetworkCloudDrive.DAO.SQLiteDAO;
import com.cloud.NetworkCloudDrive.Models.DTO.UpdateUserDTO;
import com.cloud.NetworkCloudDrive.Models.DTO.UserDTO;
import com.cloud.NetworkCloudDrive.Models.*;
import com.cloud.NetworkCloudDrive.Models.Responses.JSONErrorResponse;
import com.cloud.NetworkCloudDrive.Models.Responses.JSONMapResponse;
import com.cloud.NetworkCloudDrive.Models.Responses.JSONObjectResponse;
import com.cloud.NetworkCloudDrive.Models.Responses.JSONResponse;
import com.cloud.NetworkCloudDrive.Services.UserService;
import com.cloud.NetworkCloudDrive.Sessions.UserSession;
import com.cloud.NetworkCloudDrive.Utilities.FileUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping(path = "/api/user")
public class UserController {
    private final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final UserService userService;
    private final UserSession userSession;
    private final SQLiteDAO sqLiteDAO;
    private final FileUtility fileUtility;

    public UserController(UserService userService, UserSession userSession, SQLiteDAO sqLiteDAO, FileUtility fileUtility) {
        this.userService = userService;
        this.userSession = userSession;
        this.sqLiteDAO = sqLiteDAO;
        this.fileUtility = fileUtility;
    }

    @PostMapping("register")
    public @ResponseBody ResponseEntity<?> register(@RequestBody UserDTO userDTO) {
        try {
            UserEntity registeredUserEntity = userService.registerUser(userDTO.getName(), userDTO.getMail(), userDTO.getPassword());
            //create user directory
            fileUtility.createUserDirectory(registeredUserEntity.getId(), registeredUserEntity.getName(), registeredUserEntity.getMail());
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONMapResponse(Map.of(
                            "id", registeredUserEntity.getId(),
                            "username", registeredUserEntity.getName(),
                            "mail", registeredUserEntity.getMail(),
                            "role", registeredUserEntity.getRole(),
                            "registeredAt", registeredUserEntity.getRegisteredAt()
                    ), "User successfully registered"));
        } catch (SecurityException e) {
            logger.error("Failed to register user reason: {}", e.getMessage());
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONErrorResponse(e, "Failed to register user, reason: %s", e.getMessage()));
        } catch (IOException e) {
            logger.error("Failed to create user directory reason: {}", e.getMessage());
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONErrorResponse(e, "Failed to create user directory reason, reason: %s", e.getMessage()));
        }
    }

    @PatchMapping("update/mail")
    public @ResponseBody ResponseEntity<?> updateMail(@RequestBody UpdateUserDTO updateUserDTO) {
        try {
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONObjectResponse(
                            userService.updateMail(sqLiteDAO.findUserByMail(userSession.getMail()),
                                    updateUserDTO.getUpdate()), "Successfully updated user mail"));
        } catch (Exception e) {
            logger.error("Failed to update user mail reason: {}", e.getMessage());
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONErrorResponse(e, "Failed to update user mail, reason: %s", e.getMessage()));
        }
    }

    @PatchMapping("update/name")
    public @ResponseBody ResponseEntity<?> updateName(@RequestBody UpdateUserDTO updateUserDTO) {
        try {
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONObjectResponse(userService.updateName(sqLiteDAO.findUserByMail(userSession.getMail()),
                            updateUserDTO.getUpdate()), "Successfully updated user name"));
        } catch (Exception e) {
            logger.error("Failed to update user name reason: {}", e.getMessage());
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONErrorResponse(e, "Failed to update user name, reason: %s", e.getMessage()));
        }
    }

    @PatchMapping("update/password")
    public @ResponseBody ResponseEntity<?> updatePassword(@RequestBody UpdateUserDTO updateUserDTO) {
        try {
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONObjectResponse(userService.updatePassword(sqLiteDAO.findUserByMail(userSession.getMail()),
                            updateUserDTO.getUpdate()), "Successfully updated user password"));
        } catch (Exception e) {
            logger.error("Failed to update user password reason: {}", e.getMessage());
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONErrorResponse(e, "Failed to update user password, reason: %s", e.getMessage()));
        }
    }

    @DeleteMapping("delete")
    public @ResponseBody ResponseEntity<?> deleteUser() {
        try {
            userService.deleteUser(sqLiteDAO.findUserByMail(userSession.getMail()));
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONResponse("Successfully deleted user"));
        } catch (Exception e) {
            logger.error("Failed to delete user reason: {}", e.getMessage());
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONErrorResponse(e, "Failed to delete user, reason: %s", e.getMessage()));
        }
    }

    @GetMapping("info")
    public @ResponseBody ResponseEntity<?> info() {
        try {
            return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONObjectResponse(
                            userService.currentUserDetails(userSession.getMail()), "Currently authenticated user info"));
        } catch (UsernameNotFoundException e) {
            return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON).
                    body(new JSONErrorResponse(e, "Failed to get user details, reason: %s", e.getMessage()));
        }
    }
}
