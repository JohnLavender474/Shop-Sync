package edu.uga.cs.shopsync.backend.models;

import java.util.HashMap;
import java.util.Map;

public class NotificationModel {

    private String uid;
    private String type;
    private String title;
    private String body;
    private Map<String, Object> data;

    public NotificationModel() {
        uid = "";
        type = "";
        title = "";
        body = "";
        data = new HashMap<>();
    }

    public NotificationModel(String uid, String type, String title, String body,
                             Map<String, Object> data) {
        this.uid = uid;
        this.type = type;
        this.title = title;
        this.body = body;
        this.data = data;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}
