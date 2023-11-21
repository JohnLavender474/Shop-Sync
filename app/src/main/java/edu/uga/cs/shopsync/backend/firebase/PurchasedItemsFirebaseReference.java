package edu.uga.cs.shopsync.backend.firebase;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import edu.uga.cs.shopsync.backend.models.PurchasedItemModel;

/**
 * Provides methods to modify the purchased_items collection.
 */
@Singleton
public class PurchasedItemsFirebaseReference {

    private static final String PURCHASED_ITEMS_COLLECTION = "purchased_items";
    private static final String USER_ID_FIELD = "userId";
    private static final String ITEM_ID_FIELD = "itemId";

    private final DatabaseReference purchasedItemsCollection = FirebaseDatabase.getInstance()
            .getReference(PURCHASED_ITEMS_COLLECTION);

    /**
     * Constructs a new PurchasedItemsFirebaseReference. Empty constructor required for injection.
     */
    @Inject
    public PurchasedItemsFirebaseReference() {
    }

    /**
     * Adds a purchased item with the given user id and item id.
     *
     * @param userId the user id
     * @param itemId the item id
     * @return the purchased item model
     */
    public PurchasedItemModel addPurchasedItem(String userId, String itemId) {
        String uid = purchasedItemsCollection.push().getKey();
        if (uid == null) {
            return null;
        }
        PurchasedItemModel newPurchasedItem = new PurchasedItemModel(uid, userId, itemId);
        purchasedItemsCollection.child(uid).setValue(newPurchasedItem);
        return newPurchasedItem;
    }

    /**
     * Returns the task that attempts to get the purchased item with the given uid.
     *
     * @param itemId the uid of the purchased item
     * @return the task that attempts to get the purchased item with the given uid
     */
    public Task<DataSnapshot> getPurchasedItemWithId(String itemId) {
        return purchasedItemsCollection.child(itemId).get();
    }

    /**
     * Returns the task that attempts to get the purchased items with the given user id.
     *
     * @param userId the user id
     * @return the task that attempts to get the purchased items with the given user id
     */
    public Task<DataSnapshot> getPurchasedItemsWithUserId(String userId) {
        Query query = purchasedItemsCollection.orderByChild(USER_ID_FIELD).equalTo(userId);
        return query.get();
    }

    /**
     * Returns the task that attempts to get the purchased items with the given item id.
     *
     * @param itemId the item id
     * @return the task that attempts to get the purchased items with the given item id
     */
    public Task<DataSnapshot> getPurchasedItemsWithItemId(String itemId) {
        Query query = purchasedItemsCollection.orderByChild(ITEM_ID_FIELD).equalTo(itemId);
        return query.get();
    }

    /**
     * Returns the task that attempts to update the purchased item with the given purchased item.
     *
     * @param updatedPurchasedItem the updated purchased item
     * @return the task that attempts to update the purchased item with the given purchased item
     */
    public Task<Void> updatePurchasedItem(PurchasedItemModel updatedPurchasedItem) {
        String itemId = updatedPurchasedItem.getUid();
        Map<String, Object> purchasedItemValues = updatedPurchasedItem.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/" + itemId, purchasedItemValues);

        return purchasedItemsCollection.updateChildren(childUpdates);
    }

    /**
     * Returns the task that attempts to delete the purchased item with the given item id.
     *
     * @param itemId the item id
     * @return the task that attempts to delete the purchased item with the given item id
     */
    public Task<Void> deletePurchasedItem(String itemId) {
        return purchasedItemsCollection.child(itemId).removeValue();
    }
}

