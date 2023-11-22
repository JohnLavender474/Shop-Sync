package edu.uga.cs.shopsync.backend.models;

public class BasketItemModel {

    private ShoppingItemModel shoppingItem;
    private String shoppingBasketUid;
    private long quantity;
    private double pricePerUnit;

    public BasketItemModel() {
        shoppingItem = null;
        shoppingBasketUid = "";
        quantity = 0;
        pricePerUnit = 0;
    }

    public BasketItemModel(ShoppingItemModel shoppingItem, String shoppingBasketUid,
                           long quantity, double pricePerUnit) {
        this.shoppingItem = shoppingItem;
        this.shoppingBasketUid = shoppingBasketUid;
        this.quantity = quantity;
        this.pricePerUnit = pricePerUnit;
    }

    public ShoppingItemModel getShoppingItem() {
        return shoppingItem;
    }

    public void setShoppingItem(ShoppingItemModel shoppingItem) {
        this.shoppingItem = shoppingItem;
    }

    public String getShoppingBasketUid() {
        return shoppingBasketUid;
    }

    public void setShoppingBasketUid(String shoppingBasketUid) {
        this.shoppingBasketUid = shoppingBasketUid;
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
