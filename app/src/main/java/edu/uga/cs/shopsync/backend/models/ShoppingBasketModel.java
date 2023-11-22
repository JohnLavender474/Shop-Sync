package edu.uga.cs.shopsync.backend.models;

import java.util.HashMap;
import java.util.Map;

public class ShoppingBasketModel {

    private String userUid;
    private String shopSyncUid;
    private Map<String, BasketItemModel> basketItems;

    public ShoppingBasketModel() {
        userUid = "";
        shopSyncUid = "";
        basketItems = new HashMap<>();
    }

    public ShoppingBasketModel(String userUid, String shopSyncUid, Map<String,
            BasketItemModel> basketItems) {
        this.userUid = userUid;
        this.shopSyncUid = shopSyncUid;
        this.basketItems = basketItems;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("userUid", userUid);
        result.put("shopSyncUid", shopSyncUid);
        result.put("basketItems", basketItems);
        return result;
    }

    public String getUserUid() {
        return userUid;
    }

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }

    public String getShopSyncUid() {
        return shopSyncUid;
    }

    public void setShopSyncUid(String shopSyncUid) {
        this.shopSyncUid = shopSyncUid;
    }

    public Map<String, BasketItemModel> getBasketItems() {
        return basketItems;
    }

    public void setBasketItems(Map<String, BasketItemModel> basketItems) {
        this.basketItems = basketItems;
    }
}
