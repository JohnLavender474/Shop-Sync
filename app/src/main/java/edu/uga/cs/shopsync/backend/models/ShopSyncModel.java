package edu.uga.cs.shopsync.backend.models;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public class ShopSyncModel {

    private String uid = "";
    private String name = "";
    private String description = "";
    private Map<String, ShoppingItemModel> shoppingItems = new HashMap<>();
    private Map<String, PurchasedItemModel> purchasedItems = new HashMap<>();

    public ShopSyncModel() {
    }

    public ShopSyncModel(String uid, String name, String description, Map<String,
            ShoppingItemModel> shoppingItems, Map<String, PurchasedItemModel> purchasedItems) {
        this.uid = uid;
        this.name = name;
        this.description = description;
        this.shoppingItems = shoppingItems;
        this.purchasedItems = purchasedItems;
    }

    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put("uid", uid);
        result.put("name", name);
        result.put("description", description);
        result.put("shoppingItems", shoppingItems);
        result.put("purchasedItems", purchasedItems);
        return result;
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
