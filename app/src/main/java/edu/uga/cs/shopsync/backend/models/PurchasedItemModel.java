package edu.uga.cs.shopsync.backend.models;

import java.util.HashMap;
import java.util.Map;

public class PurchasedItemModel {

    private String uid;
    private String userUid;
    private String basketItemUid;

    public PurchasedItemModel() {
        uid = "";
        userUid = "";
        basketItemUid = "";
    }

    public PurchasedItemModel(String uid, String userUid, String basketItemUid) {
        this.uid = uid;
        this.userUid = userUid;
        this.basketItemUid = basketItemUid;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", uid);
        map.put("userUid", userUid);
        map.put("basketItemUid", basketItemUid);
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

    public String getBasketItemUid() {
        return basketItemUid;
    }

    public void setBasketItemUid(String basketItemUid) {
        this.basketItemUid = basketItemUid;
    }
}
