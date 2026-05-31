package com.cloud.NetworkCloudDrive.Configuration;

import com.cloud.NetworkCloudDrive.Models.DTO.CurrentUserDTO;
import com.cloud.NetworkCloudDrive.Models.Response.JSONObjectResponse;
import com.cloud.NetworkCloudDrive.Models.Response.JSONResponse;
import com.cloud.NetworkCloudDrive.Models.UserEntity;
import com.cloud.NetworkCloudDrive.Persistence.SQLiteDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Date;

public class AuthenticationHandler implements AuthenticationSuccessHandler, AuthenticationFailureHandler {
    @Autowired
    private SQLiteDAO sqLiteDAO;

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
            throws IOException, ServletException {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().print(new ObjectMapper().
                writeValueAsString(new JSONResponse(false, "Login failure. %s", exception.getMessage())));
        response.setStatus(401);
        response.flushBuffer();
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException {
        UserEntity user = sqLiteDAO.findUserByMail(authentication.getName());
        user.setLastLogin(new Date().toInstant());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        user.setLastLogin(new Date().toInstant());
        response.getWriter().print(new ObjectMapper()
                .writeValueAsString(new JSONObjectResponse(new CurrentUserDTO(sqLiteDAO.saveUser(user)), "Login Success")));
        response.flushBuffer();
    }
}
