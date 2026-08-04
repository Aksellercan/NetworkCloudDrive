package com.cloud.NetworkCloudDrive.Models.Response;

public class JSONObjectArrayResponse extends JSONResponse {
    private Object[] objects;

    public JSONObjectArrayResponse(Object objects[], boolean success, String message) {
        super(message, success);
        this.objects = objects;
    }

    public JSONObjectArrayResponse(Object objects[], String message) {
        super(message, true);
        this.objects = objects;
    }

    public JSONObjectArrayResponse(Object objects[], String formattedString, Object... args) {
        super(formattedString, args);
        this.objects = objects;
    }

    public JSONObjectArrayResponse(Object[] objects, boolean success, String formattedString, Object... args) {
        super(success, formattedString, args);
        this.objects = objects;
    }

    public Object getObject() {
        return objects;
    }

    public void setObject(Object objects[]) {
        this.objects = objects;
    }
}
