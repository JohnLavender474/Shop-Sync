package edu.uga.cs.shopsync.frontend.dtos;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import edu.uga.cs.shopsync.backend.models.ShopSyncModel;

public class ShopSyncDto {

    private String uid = "";
    private String name = "";
    private String description = "";
    private List<String> userUids = new ArrayList<>();

    public ShopSyncDto() {
    }

    public ShopSyncDto(String uid, String name, String description, List<String> userUids) {
        this.uid = uid;
        this.name = name;
        this.description = description;
        this.userUids = userUids;
    }

    public static ShopSyncDto fromModel(ShopSyncModel model) {
        return new ShopSyncDto(model.getUid(), model.getName(), model.getDescription(),
                               new ArrayList<>());
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

    @NonNull
    @Override
    public String toString() {
        return "ShopSyncDto{" + "uid=" + uid + ", name=" + name + ", description=" + description +
                ", userUids=" + userUids + '}';
    }
}
