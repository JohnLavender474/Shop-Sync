package edu.uga.cs.shopsync.frontend.dtos;

import java.util.ArrayList;
import java.util.List;

public class UserDto {

    private String uid = "";
    private String email = "";
    private String username = "";
    private List<ShopSyncDto> myShopSyncs = new ArrayList<>();

    public UserDto() {
    }

    public UserDto(String uid, String email, String username, List<ShopSyncDto> myShopSyncs) {
        this.uid = uid;
        this.email = email;
        this.username = username;
        this.myShopSyncs = myShopSyncs;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public List<ShopSyncDto> getMyShopSyncs() {
        return myShopSyncs;
    }

    public void setMyShopSyncs(List<ShopSyncDto> myShopSyncs) {
        this.myShopSyncs = myShopSyncs;
    }
}
