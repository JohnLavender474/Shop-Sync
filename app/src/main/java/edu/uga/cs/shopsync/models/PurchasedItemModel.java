package edu.uga.cs.shopsync.models;

import java.util.HashMap;
import java.util.Map;

public class PurchasedItemModel {

    private String uid = "";
    private String userUid = "";
    private String itemUid = "";

    public PurchasedItemModel() {
    }

    public PurchasedItemModel(String uid, String userUid, String itemUid) {
        this.uid = uid;
        this.userUid = userUid;
        this.itemUid = itemUid;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", uid);
        map.put("userUid", userUid);
        map.put("itemUid", itemUid);
        return map;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUserUid() {
        return userUid;
    }

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }

    public String getItemUid() {
        return itemUid;
    }

    public void setItemUid(String itemUid) {
        this.itemUid = itemUid;
    }
}
