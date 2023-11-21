package edu.uga.cs.shopsync.backend.models;

public class UserProfileModel {

    private String userUid = "";
    private String email = "";
    private String username = "";

    public UserProfileModel() {
    }

    public UserProfileModel(String userUid, String email, String username) {
        this.userUid = userUid;
        this.email = email;
        this.username = username;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
