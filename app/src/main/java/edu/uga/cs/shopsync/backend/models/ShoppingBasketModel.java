package edu.uga.cs.shopsync.backend.models;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public class ShoppingBasketModel {

    private String uid;
    private Map<String, BasketItemModel> basketItems;

    public ShoppingBasketModel() {
        uid = "";
        basketItems = new HashMap<>();
    }

    public ShoppingBasketModel(String uid, Map<String, BasketItemModel> basketItems) {
        this.uid = uid;
        this.basketItems = basketItems;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("basketItems", basketItems);
        return result;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public Map<String, BasketItemModel> getBasketItems() {
        return basketItems;
    }

    public void setBasketItems(Map<String, BasketItemModel> basketItems) {
        this.basketItems = basketItems;
    }

    @Override
    public int hashCode() {
        return uid.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ShoppingBasketModel s && s.uid.equals(uid);
    }

    @NonNull
    @Override
    public String toString() {
        return "ShoppingBasketModel{" +
                "uid='" + uid + '\'' +
                ", basketItems=" + basketItems +
                '}';
    }
}
