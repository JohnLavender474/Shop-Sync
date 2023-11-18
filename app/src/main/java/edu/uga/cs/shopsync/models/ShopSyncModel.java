package edu.uga.cs.shopsync.models;

import java.util.ArrayList;
import java.util.List;

public class ShopSyncModel {

    private String id = "";
    private String name = "";
    private String description = "";
    private List<String> userUids = new ArrayList<>();

    public ShopSyncModel() {
    }

    public ShopSyncModel(String id, String name, String description, List<String> userUids) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.userUids = userUids;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
