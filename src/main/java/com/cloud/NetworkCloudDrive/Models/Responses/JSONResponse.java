package com.cloud.NetworkCloudDrive.Models.Responses;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class JSONResponse {
    private String message;
    private final String date_time;
    private boolean success;

    public JSONResponse(boolean success, String message) {
        this.message = message;
        this.success = success;
        this.date_time = formatDateTime();
    }

    public JSONResponse(String message) {
        this.message = message;
        this.success = true;
        this.date_time = formatDateTime();
    }

    public JSONResponse(String formattedString, Object... args) {
        this.message = String.format(formattedString, args);
        this.success = true;
        this.date_time = formatDateTime();
    }

    public JSONResponse(boolean success, String formattedString, Object... args) {
        this.message = String.format(formattedString, args);
        this.success = success;
        this.date_time = formatDateTime();
    }

    private String formatDateTime() {
        LocalDateTime today = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        return today.format(formatter);
    }

    public boolean isSuccess() {
        return success;
    }
    public void setSuccess(boolean success) {
        this.success = success;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public String getDate_time() {
        return date_time;
    }
}
