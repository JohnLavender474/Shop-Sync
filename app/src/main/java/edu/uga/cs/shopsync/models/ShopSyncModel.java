package edu.uga.cs.shopsync.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopSyncModel {

    private String uid = "";
    private String name = "";
    private String description = "";
    private List<String> userUids = new ArrayList<>();

    public ShopSyncModel() {
    }

    public ShopSyncModel(String uid, String name, String description, List<String> userUids) {
        this.uid = uid;
        this.name = name;
        this.description = description;
        this.userUids = userUids;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("name", name);
        result.put("description", description);
        result.put("userUids", userUids);
        return result;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<String> getUserUids() {
        return userUids;
    }

    public void setUserUids(List<String> userUids) {
        this.userUids = userUids;
    }
}
