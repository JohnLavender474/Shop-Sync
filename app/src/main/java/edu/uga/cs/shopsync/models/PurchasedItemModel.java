package edu.uga.cs.shopsync.models;

import java.util.HashMap;
import java.util.Map;

public class PurchasedItemModel {

    private String uid = "";
    private String userId = "";
    private String itemId = "";

    public PurchasedItemModel() {
    }

    public PurchasedItemModel(String uid, String userId, String itemId) {
        this.uid = uid;
        this.userId = userId;
        this.itemId = itemId;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", uid);
        map.put("userId", userId);
        map.put("itemId", itemId);
        return map;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }
}
