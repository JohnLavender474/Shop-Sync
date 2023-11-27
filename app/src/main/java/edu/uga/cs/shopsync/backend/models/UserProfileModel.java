package edu.uga.cs.shopsync.backend.models;

import androidx.annotation.NonNull;

public class UserProfileModel {

    private String userUid;
    private String email;
    private String username;

    public UserProfileModel() {
        userUid = "";
        email = "";
        username = "";
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

    @Override
    public int hashCode() {
        return userUid.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof UserProfileModel u && u.userUid.equals(userUid);
    }

    @NonNull
    @Override
    public String toString() {
        return "UserProfileModel{" +
                "userUid='" + userUid + '\'' +
                ", email='" + email + '\'' +
                ", username='" + username + '\'' +
                '}';
    }

}
