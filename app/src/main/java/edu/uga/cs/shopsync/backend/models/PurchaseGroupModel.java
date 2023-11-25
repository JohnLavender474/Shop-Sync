package edu.uga.cs.shopsync.backend.models;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

public class PurchaseGroupModel {

    private String uid;
    private String userUid;
    private String username;
    private String shopSyncUid;
    private double totalCost;
    private String timeOfPurchase;
    private List<String> purchasedItemUids;

    public PurchaseGroupModel() {
        uid = "";
        userUid = "";
        username = "";
        shopSyncUid = "";
        totalCost = 0.0;
        timeOfPurchase = "";
        purchasedItemUids = new ArrayList<>();
    }

    public PurchaseGroupModel(String uid, String userUid, String username, String shopSyncUid,
                              double totalCost, String timeOfPurchase,
                              List<String> purchasedItemUids) {
        this.uid = uid;
        this.userUid = userUid;
        this.username = username;
        this.shopSyncUid = shopSyncUid;
        this.totalCost = totalCost;
        this.timeOfPurchase = timeOfPurchase;
        this.purchasedItemUids = purchasedItemUids;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUserUid() {
        return userUid;
    }

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getShopSyncUid() {
        return shopSyncUid;
    }

    public void setShopSyncUid(String shopSyncUid) {
        this.shopSyncUid = shopSyncUid;
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

    public void setTimeOfPurchase(String timeOfPurchase) {
        this.timeOfPurchase = timeOfPurchase;
    }

    public List<String> getPurchasedItemUids() {
        return purchasedItemUids;
    }

    public void setPurchasedItemUids(List<String> purchasedItemUids) {
        this.purchasedItemUids = purchasedItemUids;
    }

    @Override
    public int hashCode() {
        return uid.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof PurchaseGroupModel p && p.uid.equals(uid);
    }

    @NonNull
    @Override
    public String toString() {
        return "PurchaseGroupModel{" +
                "uid='" + uid + '\'' +
                ", userUid='" + userUid + '\'' +
                ", username='" + username + '\'' +
                ", shopSyncUid='" + shopSyncUid + '\'' +
                ", totalCost=" + totalCost +
                ", timeOfPurchase='" + timeOfPurchase + '\'' +
                ", purchasedItemUids=" + purchasedItemUids +
                '}';
    }
}

