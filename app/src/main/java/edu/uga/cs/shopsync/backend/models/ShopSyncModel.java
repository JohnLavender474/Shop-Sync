package edu.uga.cs.shopsync.backend.models;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public class ShopSyncModel {

    private String uid;
    private String name;
    private String description;
    private Map<String, ShoppingItemModel> shoppingItems;
    private Map<String, ShoppingBasketModel> shoppingBaskets;
    private Map<String, PurchasedItemModel> purchasedItems;

    public ShopSyncModel() {
        uid = "";
        name = "";
        description = "";
        shoppingItems = new HashMap<>();
        shoppingBaskets = new HashMap<>();
        purchasedItems = new HashMap<>();
    }

    public ShopSyncModel(String uid, String name, String description,
                         Map<String, ShoppingItemModel> shoppingItems,
                         Map<String, ShoppingBasketModel> shoppingBaskets,
                         Map<String, PurchasedItemModel> purchasedItems) {
        this.uid = uid;
        this.name = name;
        this.description = description;
        this.shoppingItems = shoppingItems;
        this.shoppingBaskets = shoppingBaskets;
        this.purchasedItems = purchasedItems;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("name", name);
        result.put("description", description);
        result.put("shoppingItems", shoppingItems);
        result.put("shoppingBaskets", shoppingBaskets);
        result.put("purchasedItems", purchasedItems);
        return result;
    }

    @SuppressWarnings("unchecked")
    public void fromMap(@NonNull Map<String, Object> map) {
        String _uid = (String) map.get("uid");
        if (_uid != null) {
            uid = _uid;
        }

        String _name = (String) map.get("name");
        if (_name != null) {
            name = _name;
        }

        String _description = (String) map.get("description");
        if (_description != null) {
            description = _description;
        }

        Object _shoppingItems = map.get("shoppingItems");
        if (_shoppingItems instanceof Map) {
            shoppingItems = (Map<String, ShoppingItemModel>) _shoppingItems;
        }

        Object _shoppingBaskets = map.get("shoppingBaskets");
        if (_shoppingBaskets instanceof Map) {
            shoppingBaskets = (Map<String, ShoppingBasketModel>) _shoppingBaskets;
        }

        Object _purchasedItems = map.get("purchasedItems");
        if (_purchasedItems instanceof Map) {
            purchasedItems = (Map<String, PurchasedItemModel>) _purchasedItems;
        }
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<String, ShoppingItemModel> getShoppingItems() {
        return shoppingItems;
    }

    public void setShoppingItems(Map<String, ShoppingItemModel> shoppingItems) {
        this.shoppingItems = shoppingItems;
    }

    public Map<String, ShoppingBasketModel> getShoppingBaskets() {
        return shoppingBaskets;
    }

    public void setShoppingBaskets(Map<String, ShoppingBasketModel> shoppingBaskets) {
        this.shoppingBaskets = shoppingBaskets;
    }

    public Map<String, PurchasedItemModel> getPurchasedItems() {
        return purchasedItems;
    }

    public void setPurchasedItems(Map<String, PurchasedItemModel> purchasedItems) {
        this.purchasedItems = purchasedItems;
    }

    @Override
    public int hashCode() {
        return uid.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof ShopSyncModel s && s.uid.equals(uid);
    }

    @NonNull
    @Override
    public String toString() {
        return "ShopSyncModel{" +
                "uid='" + uid + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}
