package edu.uga.cs.shopsync.backend.models;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public class PurchasedItemModel {

    private String uid;
    private String userUid;

    // TODO:
    // include shopping item and basket item so that if user undoes purchase, the items
    // can be added back to the shopping list and basket respectively in the database
    private ShoppingItemModel shoppingItem;
    private BasketItemModel basketItem;

    public PurchasedItemModel() {
        uid = "";
        userUid = "";
        shoppingItem = null;
        basketItem = null;
    }

    public PurchasedItemModel(String uid, String userUid, ShoppingItemModel shoppingItem,
                              BasketItemModel basketItem) {
        this.uid = uid;
        this.userUid = userUid;
        this.shoppingItem = shoppingItem;
        this.basketItem = basketItem;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("uid", uid);
        map.put("userUid", userUid);
        map.put("shoppingItem", shoppingItem.toMap());
        map.put("basketItem", basketItem.toMap());
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

    public ShoppingItemModel getShoppingItem() {
        return shoppingItem;
    }

    public void setShoppingItem(ShoppingItemModel shoppingItem) {
        this.shoppingItem = shoppingItem;
    }

    public BasketItemModel getBasketItem() {
        return basketItem;
    }

    public void setBasketItem(BasketItemModel basketItem) {
        this.basketItem = basketItem;
    }

    @Override
    public int hashCode() {
        return uid.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof PurchasedItemModel p && p.uid.equals(uid);
    }

    @NonNull
    @Override
    public String toString() {
        return "PurchasedItemModel{" +
                "uid='" + uid + '\'' +
                ", userUid='" + userUid + '\'' +
                ", shoppingItem=" + shoppingItem +
                ", basketItem=" + basketItem +
                '}';
    }
}
