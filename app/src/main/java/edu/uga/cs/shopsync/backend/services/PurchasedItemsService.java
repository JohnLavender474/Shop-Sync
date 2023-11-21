package edu.uga.cs.shopsync.backend.services;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;

import javax.inject.Inject;
import javax.inject.Singleton;

import edu.uga.cs.shopsync.backend.firebase.PurchasedItemsFirebaseReference;
import edu.uga.cs.shopsync.backend.models.PurchasedItemModel;

/**
 * Service class for purchased items.
 */
@Singleton
public class PurchasedItemsService {

    private static final String TAG = "PurchasedItemsService";

    private final PurchasedItemsFirebaseReference purchasedItemsFirebaseReference;

    @Inject
    public PurchasedItemsService(@NonNull PurchasedItemsFirebaseReference purchasedItemsFirebaseReference) {
        this.purchasedItemsFirebaseReference = purchasedItemsFirebaseReference;
        Log.d(TAG, "PurchasedItemsService: created");
    }

    /**
     * Adds a purchased item with the given user id and item id.
     *
     * @param userId the user id
     * @param itemId the item id
     * @return the purchased item model
     */
    public PurchasedItemModel addPurchasedItem(String userId, String itemId) {
        return purchasedItemsFirebaseReference.addPurchasedItem(userId, itemId);
    }

    /**
     * Returns the task that attempts to get the purchased item with the given uid.
     *
     * @param itemId the uid of the purchased item
     * @return the task that attempts to get the purchased item with the given uid
     */
    public Task<DataSnapshot> getPurchasedItemWithId(String itemId) {
        return purchasedItemsFirebaseReference.getPurchasedItemWithId(itemId);
    }

    /**
     * Returns the task that attempts to get the purchased items with the given user id.
     *
     * @param userId the user id
     * @return the task that attempts to get the purchased items with the given user id
     */
    public Task<DataSnapshot> getPurchasedItemsWithUserId(String userId) {
        return purchasedItemsFirebaseReference.getPurchasedItemsWithUserId(userId);
    }

    /**
     * Returns the task that attempts to get the purchased items with the given item id.
     *
     * @param itemId the item id
     * @return the task that attempts to get the purchased items with the given item id
     */
    public Task<DataSnapshot> getPurchasedItemsWithItemId(String itemId) {
        return purchasedItemsFirebaseReference.getPurchasedItemsWithItemId(itemId);
    }

    /**
     * Returns the task that attempts to update the purchased item with the given purchased item.
     *
     * @param updatedPurchasedItem the updated purchased item
     * @return the task that attempts to update the purchased item with the given purchased item
     */
    public Task<Void> updatePurchasedItem(PurchasedItemModel updatedPurchasedItem) {
        return purchasedItemsFirebaseReference.updatePurchasedItem(updatedPurchasedItem);
    }

    /**
     * Returns the task that attempts to delete the purchased item with the given item id.
     *
     * @param itemId the item id
     * @return the task that attempts to delete the purchased item with the given item id
     */
    public Task<Void> deletePurchasedItem(String itemId) {
        return purchasedItemsFirebaseReference.deletePurchasedItem(itemId);
    }
}

