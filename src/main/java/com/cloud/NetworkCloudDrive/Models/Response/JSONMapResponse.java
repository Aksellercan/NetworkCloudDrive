package com.cloud.NetworkCloudDrive.Models.Response;

import java.util.Map;

public class JSONMapResponse extends JSONResponse {
    private Map<String, ?> map_of;

    public JSONMapResponse(Map<String, ?> map_of, boolean success, String message) {
        super(success, message);
        this.map_of = map_of;
    }

    public JSONMapResponse(Map<String, ?> map_of, String message) {
        super(true, message);
        this.map_of = map_of;
    }

    public JSONMapResponse(Map<String, ?> map_of, String formattedString, Object... args) {
        super(formattedString, args);
        this.map_of = map_of;
    }

    public JSONMapResponse(Map<String, ?> map_of, boolean success, String formattedString, Object... args) {
        super(success, formattedString, args);
        this.map_of = map_of;
    }

    public Map<String, ?> getMap_of() {
        return map_of;
    }

    public void setMap_of(Map<String, ?> map_of) {
        this.map_of = map_of;
    }
}
