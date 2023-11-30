package edu.uga.cs.shopsync.backend.models;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public class PurchasedItemModel {

    private String purchasedItemUid;
    private String userEmail;

    // TODO:
    // include shopping item and basket item so that if user undoes purchase, the items
    // can be added back to the shopping list and basket respectively in the database
    private ShoppingItemModel shoppingItem;
    private BasketItemModel basketItem;

    public PurchasedItemModel() {
        purchasedItemUid = null;
        userEmail = null;
        shoppingItem = null;
        basketItem = null;
    }

    public PurchasedItemModel(String purchasedItemUid, String userEmail,
                              ShoppingItemModel shoppingItem,
                              BasketItemModel basketItem) {
        this.purchasedItemUid = purchasedItemUid;
        this.userEmail = userEmail;
        this.shoppingItem = shoppingItem;
        this.basketItem = basketItem;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("purchasedItemUid", purchasedItemUid);
        map.put("userEmail", userEmail);
        map.put("shoppingItem", shoppingItem == null ? null : shoppingItem.toMap());
        map.put("basketItem", basketItem == null ? null : basketItem.toMap());
        return map;
    }

    public String getPurchasedItemUid() {
        return purchasedItemUid;
    }

    public void setPurchasedItemUid(String purchasedItemUid) {
        this.purchasedItemUid = purchasedItemUid;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
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
        return purchasedItemUid.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof PurchasedItemModel p && p.purchasedItemUid != null &&
                p.purchasedItemUid.equals(purchasedItemUid);
    }

    @NonNull
    @Override
    public String toString() {
        return "PurchasedItemModel{" +
                "purchasedItemUid='" + purchasedItemUid + '\'' +
                ", userEmail='" + userEmail + '\'' +
                ", shoppingItem=" + shoppingItem +
                ", basketItem=" + basketItem +
                '}';
    }
}
