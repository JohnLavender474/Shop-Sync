package edu.uga.cs.shopsync.backend.models;

import java.util.HashMap;
import java.util.Map;

public class ShoppingBasketModel {

    private String userUid;
    private String shopSyncUid;
    private Map<String, ShoppingItemModel> shoppingItems;

    public ShoppingBasketModel() {
        userUid = "";
        shopSyncUid = "";
        shoppingItems = new HashMap<>();
    }

    public ShoppingBasketModel(String userUid, String shopSyncUid, Map<String,
            ShoppingItemModel> shoppingItems) {
        this.userUid = userUid;
        this.shopSyncUid = shopSyncUid;
        this.shoppingItems = shoppingItems;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("userUid", userUid);
        result.put("shopSyncUid", shopSyncUid);
        result.put("shoppingItems", shoppingItems);
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

    public Map<String, ShoppingItemModel> getShoppingItems() {
        return shoppingItems;
    }

    public void setShoppingItems(Map<String, ShoppingItemModel> shoppingItems) {
        this.shoppingItems = shoppingItems;
    }
}
