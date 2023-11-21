package edu.uga.cs.shopsync.frontend.dtos;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class ShopSyncDto {

    private String uid = "";
    private String name = "";
    private String description = "";
    private List<UserDto> users = new ArrayList<>();

    public ShopSyncDto() {
    }

    public ShopSyncDto(String uid, String name, String description, List<UserDto> users) {
        this.uid = uid;
        this.name = name;
        this.description = description;
        this.users = users;
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

    public List<UserDto> getUsers() {
        return users;
    }

    public void setUsers(List<UserDto> users) {
        this.users = users;
    }

    @NonNull
    @Override
    public String toString() {
        return "ShopSyncDto{" + "uid=" + uid + ", name=" + name + ", description=" + description +
                ", users=" + users + '}';
    }
}
