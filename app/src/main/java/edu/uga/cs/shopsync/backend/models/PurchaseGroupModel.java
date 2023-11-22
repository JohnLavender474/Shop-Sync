package edu.uga.cs.shopsync.backend.models;

import androidx.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

public class PurchaseGroupModel {

    private String username;
    private double totalCost;
    private String timeOfPurchase;
    private Map<String, PurchasedItemModel> purchasedItems;

    public PurchaseGroupModel() {
        username = "";
        totalCost = 0;
        timeOfPurchase = "";
        purchasedItems = new HashMap<>();
    }

    public PurchaseGroupModel(@NonNull String username, double totalCost,
                              @NonNull String timeOfPurchase,
                              @NonNull Map<String, PurchasedItemModel> purchasedItems) {
        this.username = username;
        this.totalCost = totalCost;
        this.timeOfPurchase = timeOfPurchase;
        this.purchasedItems = purchasedItems;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(@NonNull String username) {
        this.username = username;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    public String getTimeOfPurchase() {
        return timeOfPurchase;
    }

    public void setTimeOfPurchase(@NonNull String timeOfPurchase) {
        this.timeOfPurchase = timeOfPurchase;
    }

    public Map<String, PurchasedItemModel> getPurchasedItems() {
        return purchasedItems;
    }

    public void setPurchasedItems(@NonNull Map<String, PurchasedItemModel> purchasedItems) {
        this.purchasedItems = purchasedItems;
    }
}

