package edu.uga.cs.shopsync.models;

import java.math.BigDecimal;

public class ShoppingItemModel {

    private String id = "";
    private String name = "";
    private long quantity = 0;
    private BigDecimal pricePerUnit = BigDecimal.ZERO;

    public ShoppingItemModel() {
    }

    public ShoppingItemModel(String id, String name, long quantity, BigDecimal pricePerUnit) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.pricePerUnit = pricePerUnit;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public BigDecimal getPricePerUnit() {
        return pricePerUnit;
    }

    public void setPricePerUnit(BigDecimal pricePerUnit) {
        this.pricePerUnit = pricePerUnit;
    }
}
