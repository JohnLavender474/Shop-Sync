package edu.uga.cs.shopsync.backend.models;

import java.util.HashMap;
import java.util.Map;

public class ShoppingItemModel {

    private String uid;
    private String shopSyncUid;
    private String name;
    private boolean inBasket;

    public ShoppingItemModel() {
        uid = "";
        shopSyncUid = "";
        name = "";
        inBasket = false;
    }

    public ShoppingItemModel(String uid, String shopSyncUid, String name, boolean inBasket) {
        this.uid = uid;
        this.shopSyncUid = shopSyncUid;
        this.name = name;
        this.inBasket = inBasket;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("uid", uid);
        map.put("shopSyncUid", shopSyncUid);
        map.put("name", name);
        map.put("inBasket", inBasket);
        return map;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getShopSyncUid() {
        return shopSyncUid;
    }

    public void setShopSyncUid(String shopSyncUid) {
        this.shopSyncUid = shopSyncUid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isInBasket() {
        return inBasket;
    }

    public void setInBasket(boolean inBasket) {
        this.inBasket = inBasket;
    }
}
