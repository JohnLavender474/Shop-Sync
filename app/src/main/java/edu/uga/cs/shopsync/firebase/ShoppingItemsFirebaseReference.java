package edu.uga.cs.shopsync.firebase;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import edu.uga.cs.shopsync.models.ShoppingItemModel;

/**
 * Provides methods to modify the shopping_items collection.
 */
public class ShoppingItemsFirebaseReference {

    private static final String SHOPPING_ITEMS_COLLECTION = "shopping_items";

    private final DatabaseReference shoppingItemsCollection = FirebaseDatabase.getInstance()
            .getReference(SHOPPING_ITEMS_COLLECTION);

    /**
     * Adds a shopping item with the given name, quantity, and price per unit.
     *
     * @param name         the name of the shopping item
     * @param quantity     the quantity of the shopping item
     * @param pricePerUnit the price per unit of the shopping item
     * @return the shopping item model
     */
    public ShoppingItemModel addShoppingItem(String name, long quantity, double pricePerUnit) {
        String uid = shoppingItemsCollection.push().getKey();
        if (uid == null) {
            return null;
        }
        ShoppingItemModel newShoppingItem = new ShoppingItemModel(uid, name, quantity,
                                                                  pricePerUnit);
        shoppingItemsCollection.child(uid).setValue(newShoppingItem);
        return newShoppingItem;
    }

    /**
     * Returns the task that attempts to get the shopping item with the given uid.
     *
     * @param itemId the uid of the shopping item
     * @return the task that attempts to get the shopping item with the given uid
     */
    public Task<DataSnapshot> getShoppingItemWithId(String itemId) {
        return shoppingItemsCollection.child(itemId).get();
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
     * @param itemId the uid of the shopping item
     * @return the task that attempts to delete the shopping item with the given uid
     */
    public Task<Void> deleteShoppingItem(String itemId) {
        return shoppingItemsCollection.child(itemId).removeValue();
    }
}

