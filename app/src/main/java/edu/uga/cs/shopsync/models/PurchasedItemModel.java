package edu.uga.cs.shopsync.models;

public class PurchasedItemModel {

    private String id = "";
    private String userId = "";
    private String itemId = "";

    public PurchasedItemModel() {
    }

    public PurchasedItemModel(String id, String userId, String itemId) {
        this.id = id;
        this.userId = userId;
        this.itemId = itemId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }
}
