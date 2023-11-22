package edu.uga.cs.shopsync.backend.firebase;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;

import javax.inject.Inject;

import edu.uga.cs.shopsync.backend.models.ShoppingBasketModel;
import edu.uga.cs.shopsync.backend.models.ShoppingItemModel;

/**
 * Firebase reference for shopping baskets.
 */
public class ShoppingBasketsFirebaseReference {

    private static final String TAG = "ShoppingBasketsFirebaseReference";
    private static final String SHOPPING_BASKETS_COLLECTION = "shopping_baskets";
    private static final String SHOPPING_ITEMS_FIELD = "shoppingItems";

    private final DatabaseReference shoppingBasketsCollection;

    /**
     * Constructs a new ShoppingBasketsFirebaseReference. Empty constructor required for injection.
     */
    @Inject
    public ShoppingBasketsFirebaseReference() {
        shoppingBasketsCollection =
                FirebaseDatabase.getInstance().getReference(SHOPPING_BASKETS_COLLECTION);
    }

    /**
     * Constructs a new ShoppingBasketsFirebaseReference with the given shopping baskets collection.
     * Used for testing only.
     *
     * @param shoppingBasketsCollection the shopping baskets collection
     */
    ShoppingBasketsFirebaseReference(DatabaseReference shoppingBasketsCollection) {
        this.shoppingBasketsCollection = shoppingBasketsCollection;
    }

    /**
     * Adds a shopping basket with the given user uid and shop sync uid.
     *
     * @param userUid     the user uid
     * @param shopSyncUid the shop sync uid
     * @return the shopping basket model
     */
    public ShoppingBasketModel addShoppingBasket(String userUid, String shopSyncUid) {
        ShoppingBasketModel newShoppingBasket = new ShoppingBasketModel(userUid,
                                                                        shopSyncUid,
                                                                        new HashMap<>());
        String key = createKey(userUid, shopSyncUid);
        shoppingBasketsCollection.child(key).setValue(newShoppingBasket);
        return newShoppingBasket;
    }

    /**
     * Sets the shopping item in the shopping basket with the given user uid and shop sync uid.
     * If the shopping item already exists in the shopping basket, it will be overwritten.
     *
     * @param userUid      the user uid
     * @param shopSyncUid  the shop sync uid
     * @param shoppingItem the shopping item
     */
    public void setItemInShoppingBasket(String userUid, String shopSyncUid,
                                        ShoppingItemModel shoppingItem) {
        String key = createKey(userUid, shopSyncUid);
        shoppingBasketsCollection.child(key).child(SHOPPING_ITEMS_FIELD)
                .child(shoppingItem.getUid()).setValue(shoppingItem);
    }

    /**
     * Removes the shopping item from the shopping basket with the given user uid and shop sync uid.
     * If the shopping item does not exist in the shopping basket, nothing will happen.
     *
     * @param userUid         the user uid
     * @param shopSyncUid     the shop sync uid
     * @param shoppingItemUid the shopping item uid
     */
    public void removeItemFromShoppingBasket(String userUid, String shopSyncUid,
                                             String shoppingItemUid) {
        String key = createKey(userUid, shopSyncUid);
        shoppingBasketsCollection.child(key).child(SHOPPING_ITEMS_FIELD)
                .child(shoppingItemUid).removeValue();
    }

    /**
     * Returns the task that attempts to get the shopping basket with the given user uid and
     * shop sync uid.
     *
     * @param userUid     the user uid
     * @param shopSyncUid the shop sync uid
     * @return the task that attempts to get the shopping basket with the given uid
     */
    public Task<DataSnapshot> getShoppingBasket(String userUid, String shopSyncUid) {
        String key = createKey(userUid, shopSyncUid);
        return shoppingBasketsCollection.child(key).get();
    }

    /**
     * Deletes the shopping basket with the given user uid and shop sync uid.
     *
     * @param userUid     the user uid
     * @param shopSyncUid the shop sync uid
     */
    public void deleteShoppingBasket(String userUid, String shopSyncUid) {
        String key = createKey(userUid, shopSyncUid);
        shoppingBasketsCollection.child(key).removeValue();
    }

    /**
     * Returns the key that corresponds to the shopping basket with the given user uid and shop sync
     * uid. This key is used to store and access the shopping basket in the database.
     *
     * @param userUid     the user uid
     * @param shopSyncUid the shop sync uid
     * @return the key that corresponds to the shopping basket
     */
    public static String createKey(String userUid, String shopSyncUid) {
        return userUid + "_" + shopSyncUid;
    }

}
