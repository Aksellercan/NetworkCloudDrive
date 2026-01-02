package com.cloud.NetworkCloudDrive.Configuration;

import com.cloud.NetworkCloudDrive.DAO.SQLiteDAO;
import com.cloud.NetworkCloudDrive.Models.JSONObjectResponse;
import com.cloud.NetworkCloudDrive.Models.JSONResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;

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
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().print(new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .writeValueAsString(new JSONObjectResponse(
                        sqLiteDAO.getUserIDNameAndRoleByMail(authentication.getName()), "Login Success")));
        response.flushBuffer();
    }
}
