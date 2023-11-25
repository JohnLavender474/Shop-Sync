package edu.uga.cs.shopsync.backend.models;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public class ShoppingItemModel {

    private String uid;
    private String name;
    private boolean inBasket;

    public ShoppingItemModel() {
        uid = "";
        name = "";
        inBasket = false;
    }

    public ShoppingItemModel(String uid, String name, boolean inBasket) {
        this.uid = uid;
        this.name = name;
        this.inBasket = inBasket;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("uid", uid);
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

    @Override
    public int hashCode() {
        return uid.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ShoppingItemModel s && s.uid.equals(uid);
    }

    @NonNull
    @Override
    public String toString() {
        return "ShoppingItemModel{" +
                "uid='" + uid + '\'' +
                ", name='" + name + '\'' +
                ", inBasket=" + inBasket +
                '}';
    }
}
