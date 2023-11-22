package edu.uga.cs.shopsync.backend.models;

import java.util.HashMap;
import java.util.Map;

public class BasketItemModel {

    private String uid;
    private String shoppingBasketUid;
    private ShoppingItemModel shoppingItem;
    private long quantity;
    private double pricePerUnit;

    public BasketItemModel() {
        uid = "";
        shoppingBasketUid = "";
        shoppingItem = null;
        quantity = 0;
        pricePerUnit = 0;
    }

    public BasketItemModel(String uid, String shoppingBasketUid, ShoppingItemModel shoppingItem,
                           long quantity, double pricePerUnit) {
        this.uid = uid;
        this.shoppingBasketUid = shoppingBasketUid;
        this.shoppingItem = shoppingItem;
        this.quantity = quantity;
        this.pricePerUnit = pricePerUnit;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("uid", uid);
        map.put("shoppingBasketUid", shoppingBasketUid);
        map.put("shoppingItem", shoppingItem);
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

    public String getShoppingBasketUid() {
        return shoppingBasketUid;
    }

    public void setShoppingBasketUid(String shoppingBasketUid) {
        this.shoppingBasketUid = shoppingBasketUid;
    }

    public ShoppingItemModel getShoppingItem() {
        return shoppingItem;
    }

    public void setShoppingItem(ShoppingItemModel shoppingItem) {
        this.shoppingItem = shoppingItem;
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
