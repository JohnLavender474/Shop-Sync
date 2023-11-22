package edu.uga.cs.shopsync.backend.firebase;

import android.util.Log;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import edu.uga.cs.shopsync.backend.models.ShoppingItemModel;

/**
 * Provides methods to modify the shopping_items collection.
 */
@Singleton
public class ShoppingItemsFirebaseReference {

    private static final String TAG = "ShoppingItemsFirebaseReference";
    private static final String SHOPPING_ITEMS_COLLECTION = "shopping_items";

    private final DatabaseReference shoppingItemsCollection;

    /**
     * Constructs a new ShoppingItemsFirebaseReference. Empty constructor required for injection.
     */
    @Inject
    public ShoppingItemsFirebaseReference() {
        shoppingItemsCollection =
                FirebaseDatabase.getInstance().getReference(SHOPPING_ITEMS_COLLECTION);
    }

    /**
     * Constructs a new ShoppingItemsFirebaseReference with the given shopping items collection.
     * Used for testing only.
     *
     * @param shoppingItemsCollection the shopping items collection
     */
    ShoppingItemsFirebaseReference(DatabaseReference shoppingItemsCollection) {
        this.shoppingItemsCollection = shoppingItemsCollection;
    }

    /**
     * Adds a shopping item with the given name, quantity, and price per unit.
     *
     * @param shopSyncUid the uid of the shop sync the shopping item is in
     * @param name        the name of the shopping item
     * @param inBasket    if the item is in a user's basket
     * @return the shopping item model
     */
    public ShoppingItemModel addShoppingItem(String shopSyncUid, String name, boolean inBasket) {
        String uid = shoppingItemsCollection.push().getKey();
        if (uid == null) {
            Log.e(TAG, "addShoppingItem: uid is null");
            return null;
        }

        ShoppingItemModel newShoppingItem = new ShoppingItemModel(uid, shopSyncUid, name, inBasket);
        shoppingItemsCollection.child(uid).setValue(newShoppingItem);

        Log.d(TAG,
              "addShoppingItem: added shopping item with shop sync uid (" + shopSyncUid + "), " +
                      "name " + name + ", in basket " + inBasket + ", and uid (" + uid + ")");
        return newShoppingItem;
    }

    /**
     * Returns the task that attempts to get the shopping item with the given uid.
     *
     * @param uid the uid of the shopping item
     * @return the task that attempts to get the shopping item with the given uid
     */
    public Task<DataSnapshot> getShoppingItem(String uid) {
        return shoppingItemsCollection.child(uid).get();
    }

    /**
     * Returns the task that attempts to get the shopping items with the given price per unit.
     *
     * @param updatedShoppingItem the updated shopping item
     * @return the task that attempts to get the shopping items with the given price per unit
     */
    public Task<Void> updateShoppingItem(ShoppingItemModel updatedShoppingItem) {
        String itemId = updatedShoppingItem.getUid();
        Map<String, Object> shoppingItemValues = updatedShoppingItem.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/" + itemId, shoppingItemValues);

        return shoppingItemsCollection.updateChildren(childUpdates);
    }

    /**
     * Returns the task that attempts to delete the shopping item with the given uid.
     *
     * @param uid the uid of the shopping item
     * @return the task that attempts to delete the shopping item with the given uid
     */
    public Task<Void> deleteShoppingItem(String uid) {
        return shoppingItemsCollection.child(uid).removeValue();
    }
}

