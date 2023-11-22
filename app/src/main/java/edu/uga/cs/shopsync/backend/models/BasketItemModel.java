package edu.uga.cs.shopsync.backend.models;

import java.util.HashMap;
import java.util.Map;

public class BasketItemModel {

    private String shoppingItemUid;
    private String shoppingBasketUid;
    private long quantity;
    private double pricePerUnit;

    public BasketItemModel() {
        shoppingBasketUid = "";
        shoppingItemUid = "";
        quantity = 0;
        pricePerUnit = 0;
    }

    public BasketItemModel(String shoppingBasketUid, String shoppingItemUid,
                           long quantity, double pricePerUnit) {
        this.shoppingBasketUid = shoppingBasketUid;
        this.shoppingItemUid = shoppingItemUid;
        this.quantity = quantity;
        this.pricePerUnit = pricePerUnit;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("shoppingBasketUid", shoppingBasketUid);
        map.put("shoppingItemUid", shoppingItemUid);
        map.put("quantity", quantity);
        map.put("pricePerUnit", pricePerUnit);
        return map;
    }

    public String getShoppingBasketUid() {
        return shoppingBasketUid;
    }

    public void setShoppingBasketUid(String shoppingBasketUid) {
        this.shoppingBasketUid = shoppingBasketUid;
    }

    public String getShoppingItemUid() {
        return shoppingItemUid;
    }

    public void setShoppingItemUid(String shoppingItemUid) {
        this.shoppingItemUid = shoppingItemUid;
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
