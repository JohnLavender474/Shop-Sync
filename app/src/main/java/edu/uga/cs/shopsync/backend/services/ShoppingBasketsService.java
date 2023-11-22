package edu.uga.cs.shopsync.backend.services;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;

import javax.inject.Inject;
import javax.inject.Singleton;

import edu.uga.cs.shopsync.backend.firebase.ShoppingBasketsFirebaseReference;
import edu.uga.cs.shopsync.backend.models.ShoppingBasketModel;

/**
 * Service for shopping baskets.
 */
@Singleton
public class ShoppingBasketsService {

    // TODO: add methods for adding shopping items to a shopping basket

    private final ShoppingBasketsFirebaseReference shoppingBasketsFirebaseReference;

    @Inject
    public ShoppingBasketsService(@NonNull ShoppingBasketsFirebaseReference
                                          shoppingBasketsFirebaseReference) {
        this.shoppingBasketsFirebaseReference = shoppingBasketsFirebaseReference;
    }

    /**
     * Adds a shopping basket with the given user uid and shop sync uid.
     *
     * @param userUid     the user uid
     * @param shopSyncUid the shop sync uid
     * @return the shopping basket id
     */
    public ShoppingBasketModel addShoppingBasket(String userUid, String shopSyncUid) {
        return shoppingBasketsFirebaseReference.addShoppingBasket(userUid, shopSyncUid);
    }

    /**
     * Returns the task that attempts to get the shopping basket with the given user uid and shop
     * sync uid.
     *
     * @param userUid     the user uid
     * @param shopSyncUid the shop sync uid
     * @return the task that attempts to get the shopping basket with the given user uid and shop
     * sync uid
     */
    public Task<DataSnapshot> getShoppingBasket(String userUid, String shopSyncUid) {
        return shoppingBasketsFirebaseReference.getShoppingBasket(userUid, shopSyncUid);
    }

    /**
     * Deletes the shopping basket with the given user uid and shop sync uid.
     *
     * @param userUid     the user uid
     * @param shopSyncUid the shop sync uid
     */
    public void deleteShoppingBasket(String userUid, String shopSyncUid) {
        shoppingBasketsFirebaseReference.deleteShoppingBasket(userUid, shopSyncUid);
    }

}
