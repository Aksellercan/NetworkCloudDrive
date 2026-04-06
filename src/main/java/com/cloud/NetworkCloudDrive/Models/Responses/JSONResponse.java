package com.cloud.NetworkCloudDrive.Models.Responses;

import java.time.Instant;

public class JSONResponse {
    private String message;
    private final Instant date_time = Instant.now();
    private boolean success;

    public JSONResponse(boolean success, String message) {
        this.message = message;
        this.success = success;
    }

    public JSONResponse(String message) {
        this.message = message;
        this.success = true;
    }

    public JSONResponse(String formattedString, Object... args) {
        this.message = String.format(formattedString, args);
        this.success = true;
    }

    public JSONResponse(boolean success, String formattedString, Object... args) {
        this.message = String.format(formattedString, args);
        this.success = success;
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
    public Instant getDate_time() {
        return date_time;
    }
}
