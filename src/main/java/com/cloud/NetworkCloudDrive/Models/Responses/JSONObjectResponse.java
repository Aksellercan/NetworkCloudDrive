package com.cloud.NetworkCloudDrive.Models.Responses;

public class JSONObjectResponse extends JSONResponse {
    private Object object;

    public JSONObjectResponse(Object object, boolean success, String message) {
        super(message, success);
        this.object = object;
    }

    public JSONObjectResponse(Object object, String message) {
        super(message, true);
        this.object = object;
    }

    public JSONObjectResponse(Object object, String formattedString, Object... args) {
        super(formattedString, args);
        this.object = object;
    }

    public JSONObjectResponse(Object object, boolean success, String formattedString, Object... args) {
        super(success, formattedString, args);
        this.object = object;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }
}
