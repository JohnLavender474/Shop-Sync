package edu.uga.cs.shopsync.models;

import java.util.ArrayList;
import java.util.List;

public class UserProfileModel {

    private String userUid = "";
    private String email = "";
    private String nickname = "";
    private List<String> myShopSyncsUids = new ArrayList<>();

    public UserProfileModel() {
    }

    public UserProfileModel(String userUid, String email, String nickname) {
        this.userUid = userUid;
        this.email = email;
        this.nickname = nickname;
    }

    public String getUserUid() {
        return userUid;
    }

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public List<String> getMyShopSyncsUids() {
        return myShopSyncsUids;
    }

    public void setMyShopSyncsUids(List<String> myShopSyncsUids) {
        this.myShopSyncsUids = myShopSyncsUids;
    }
}
