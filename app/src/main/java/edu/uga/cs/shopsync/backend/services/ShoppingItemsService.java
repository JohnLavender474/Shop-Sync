package edu.uga.cs.shopsync.backend.services;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;

import javax.inject.Inject;
import javax.inject.Singleton;

import edu.uga.cs.shopsync.backend.firebase.ShoppingItemsFirebaseReference;
import edu.uga.cs.shopsync.backend.models.ShoppingItemModel;

/**
 * Service class for shopping items.
 */
@Singleton
public class ShoppingItemsService {

    private static final String TAG = "ShoppingItemsService";

    private final ShoppingItemsFirebaseReference shoppingItemsFirebaseReference;

    @Inject
    public ShoppingItemsService(@NonNull ShoppingItemsFirebaseReference shoppingItemsFirebaseReference) {
        this.shoppingItemsFirebaseReference = shoppingItemsFirebaseReference;
        Log.d(TAG, "ShoppingItemsService: created");
    }

    /**
     * Adds a shopping item with the given name, quantity, and price per unit.
     *
     * @param name         the name of the shopping item
     * @param quantity     the quantity of the shopping item
     * @param pricePerUnit the price per unit of the shopping item
     * @return the shopping item model
     */
    public ShoppingItemModel addShoppingItem(String name, long quantity, double pricePerUnit) {
        return shoppingItemsFirebaseReference.addShoppingItem(name, quantity, pricePerUnit);
    }

    /**
     * Returns the task that attempts to get the shopping item with the given uid.
     *
     * @param itemId the uid of the shopping item
     * @return the task that attempts to get the shopping item with the given uid
     */
    public Task<DataSnapshot> getShoppingItemWithId(String itemId) {
        return shoppingItemsFirebaseReference.getShoppingItemWithId(itemId);
    }

    /**
     * Returns the task that attempts to get the shopping items with the given price per unit.
     *
     * @param updatedShoppingItem the updated shopping item
     * @return the task that attempts to get the shopping items with the given price per unit
     */
    public Task<Void> updateShoppingItem(ShoppingItemModel updatedShoppingItem) {
        return shoppingItemsFirebaseReference.updateShoppingItem(updatedShoppingItem);
    }

    /**
     * Returns the task that attempts to delete the shopping item with the given uid.
     *
     * @param itemId the uid of the shopping item
     * @return the task that attempts to delete the shopping item with the given uid
     */
    public Task<Void> deleteShoppingItem(String itemId) {
        return shoppingItemsFirebaseReference.deleteShoppingItem(itemId);
    }
}

