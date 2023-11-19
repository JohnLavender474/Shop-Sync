package edu.uga.cs.shopsync.models;

import java.util.HashMap;
import java.util.Map;

public class ShoppingItemModel {

    private String uid = "";
    private String name = "";
    private long quantity = 0;
    private double pricePerUnit = 0;

    public ShoppingItemModel() {
    }

    public ShoppingItemModel(String uid, String name, long quantity, double pricePerUnit) {
        this.uid = uid;
        this.name = name;
        this.quantity = quantity;
        this.pricePerUnit = pricePerUnit;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", uid);
        map.put("name", name);
        map.put("quantity", quantity);
        map.put("pricePerUnit", pricePerUnit);
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

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    public double getPricePerUnit() {
        return pricePerUnit;
    }

    public void setPricePerUnit(double pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
    }
}
