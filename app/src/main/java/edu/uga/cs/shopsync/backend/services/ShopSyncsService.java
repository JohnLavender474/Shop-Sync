package edu.uga.cs.shopsync.backend.services;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.inject.Singleton;

import edu.uga.cs.shopsync.backend.firebase.ShopSyncsFirebaseReference;
import edu.uga.cs.shopsync.backend.firebase.UserShopSyncMapFirebaseReference;
import edu.uga.cs.shopsync.backend.models.BasketItemModel;
import edu.uga.cs.shopsync.backend.models.PurchasedItemModel;
import edu.uga.cs.shopsync.backend.models.ShopSyncModel;
import edu.uga.cs.shopsync.backend.models.ShoppingBasketModel;
import edu.uga.cs.shopsync.backend.models.ShoppingItemModel;
import edu.uga.cs.shopsync.utils.ErrorHandle;
import edu.uga.cs.shopsync.utils.ErrorType;

/**
 * Service class for shop syncs.
 */
@Singleton
public class ShopSyncsService {

    private static final String TAG = "ShopSyncsService";

    private final ShopSyncsFirebaseReference shopSyncsFirebaseReference;
    private final UserShopSyncMapFirebaseReference userShopSyncMapFirebaseReference;

    @Inject
    public ShopSyncsService(@NonNull ShopSyncsFirebaseReference shopSyncsFirebaseReference,
                            @NonNull UserShopSyncMapFirebaseReference userShopSyncMapFirebaseReference) {
        this.shopSyncsFirebaseReference = shopSyncsFirebaseReference;
        this.userShopSyncMapFirebaseReference = userShopSyncMapFirebaseReference;
        Log.d(TAG, "ShopSyncsService: created");
    }

    /**
     * Returns the shop syncs firebase reference.
     *
     * @return the shop syncs firebase reference
     */
    public ShopSyncsFirebaseReference getShopSyncsFirebaseReference() {
        return shopSyncsFirebaseReference;
    }

    /**
     * Returns the user shop sync map firebase reference.
     *
     * @return the user shop sync map firebase reference
     */
    public UserShopSyncMapFirebaseReference getUserShopSyncMapFirebaseReference() {
        return userShopSyncMapFirebaseReference;
    }

    /**
     * Adds a shop sync with the given name, description, and user uids.
     *
     * @param name        the name of the shop sync
     * @param description the description of the shop sync
     * @param userUids    the user uids of the shop sync
     */
    public void addShopSync(@NonNull String name, @Nullable String description,
                            @NonNull List<String> userUids,
                            @Nullable Consumer<ShopSyncModel> onSuccess,
                            @Nullable Runnable onFailure) {
        // Add shop sync to shop syncs collection
        ShopSyncModel shopSync = shopSyncsFirebaseReference
                .addShopSync(name, description, null, null, null);

        // If the shop sync uid is null, then the shop sync was not added
        if (shopSync == null) {
            Log.e(TAG, "addShopSync: failed to add shop sync");
            if (onFailure != null) {
                onFailure.run();
            }
            return;
        }

        // Map the shop sync to the users
        userUids.forEach(userUid -> userShopSyncMapFirebaseReference
                .addShopSyncToUser(userUid, shopSync.getUid()));

        Log.d(TAG, "addShopSync: successfully added shop sync " + shopSync);
        if (onSuccess != null) {
            Log.d(TAG, "addShopSync: running on success runnable");
            onSuccess.accept(shopSync);
        }
    }

    /**
     * Returns the task that attempts to get the shop sync with the given uid.
     *
     * @param uid the uid of the shop sync
     * @return the task that attempts to get the shop sync with the given uid
     */
    public Task<DataSnapshot> getShopSyncWithUid(@NonNull String uid) {
        Log.d(TAG, "getShopSyncWithUid: getting shop sync with uid (" + uid + ")");
        return shopSyncsFirebaseReference.getShopSyncWithUid(uid);
    }

    /**
     * Returns the task that attempts to update the shop sync.
     *
     * @param userUid              the user uid
     * @param shopSyncUidsConsumer the shop sync uids consumer
     * @param onError              the on error consumer
     */
    public void getShopSyncsForUser(@NonNull String userUid,
                                    @NonNull Consumer<List<String>> shopSyncUidsConsumer,
                                    @Nullable Consumer<ErrorHandle> onError) {
        Log.d(TAG, "getShopSyncsForUser: getting shop syncs for user (" + userUid + ")");

        userShopSyncMapFirebaseReference.getShopSyncsAssociatedWithUser(userUid)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DataSnapshot dataSnapshot = task.getResult();
                        if (dataSnapshot != null) {
                            // collect the shop sync uids
                            List<String> shopSyncUids = new ArrayList<>();

                            for (DataSnapshot shopSyncSnapshot : dataSnapshot.getChildren()) {
                                String shopSyncUid = shopSyncSnapshot.getKey();
                                if (shopSyncUid != null) {
                                    shopSyncUids.add(shopSyncUid);
                                }
                            }

                            Log.d(TAG, "getShopSyncsForUser: successfully got shop syncs for user" +
                                    " (" + userUid + ") with shop sync uids " + shopSyncUids);

                            // consume the shop sync uids
                            shopSyncUidsConsumer.accept(shopSyncUids);
                        } else {
                            Log.e(TAG,
                                  "getShopSyncsForUser: failed to get shop syncs for user" +
                                          " (" + userUid + ")");

                            // consume error
                            if (onError != null) {
                                onError.accept(new ErrorHandle(ErrorType.ILLEGAL_NULL_VALUE,
                                                               "Task to get shop syncs " +
                                                                       "for user failed"));
                            }
                        }
                    } else {
                        Log.e(TAG,
                              "getShopSyncsForUser: failed to get shop syncs for user (" +
                                      userUid + ")", task.getException());

                        // consume error
                        if (onError != null) {
                            onError.accept(new ErrorHandle(ErrorType.TASK_FAILED,
                                                           "Task to get " +
                                                                   "shop syncs for user " +
                                                                   "failed"));
                        }
                    }
                });
    }

    /**
     * Returns the task that attempts to get the users associated with the given shop sync uid.
     *
     * @param shopSyncUid      the shop sync uid
     * @param userUidsConsumer the consumer that consumes the user uids
     * @param onError          the consumer that consumes the error if any
     */
    public void getUsersForShopSync(@NonNull String shopSyncUid,
                                    @NonNull Consumer<List<String>> userUidsConsumer,
                                    @Nullable Consumer<ErrorHandle> onError) {
        Log.d(TAG, "getUsersForShopSync: getting users for shop sync (" + shopSyncUid + ")");

        userShopSyncMapFirebaseReference.getUsersAssociatedWithShopSync(shopSyncUid)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DataSnapshot dataSnapshot = task.getResult();
                        if (dataSnapshot != null) {
                            // collect the user uids
                            List<String> userUids = new ArrayList<>();

                            for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                                String userUid = userSnapshot.getKey();
                                if (userUid != null) {
                                    userUids.add(userUid);
                                }
                            }

                            Log.d(TAG, "getUsersForShopSync: successfully got users for shop sync" +
                                    " (" + shopSyncUid + ") with user uids " + userUids);

                            // consume the user uids
                            userUidsConsumer.accept(userUids);
                        } else {
                            Log.e(TAG,
                                  "getUsersForShopSync: failed to get users for shop sync " +
                                          "(" + shopSyncUid + ")");

                            // consume error
                            if (onError != null) {
                                onError.accept(new ErrorHandle(ErrorType.ILLEGAL_NULL_VALUE,
                                                               "Task to get users for " +
                                                                       "shop sync failed"));
                            }
                        }
                    } else {
                        Log.e(TAG,
                              "getUsersForShopSync: failed to get users for shop sync (" +
                                      shopSyncUid + ")", task.getException());

                        // consume error
                        if (onError != null) {
                            onError.accept(new ErrorHandle(ErrorType.TASK_FAILED,
                                                           "Task to get " +
                                                                   "users for shop sync " +
                                                                   "failed"));
                        }
                    }
                });
    }

    /**
     * Returns the task that attempts to update the shop sync with the given shop sync.
     *
     * @param updatedShopSync the updated shop sync
     * @return the task that attempts to update the shop sync with the given shop sync
     */
    public Task<Void> updateShopSync(@NonNull ShopSyncModel updatedShopSync) {
        Log.d(TAG, "updateShopSync: updating shop sync with uid (" + updatedShopSync.getUid() +
                ")");
        return shopSyncsFirebaseReference.updateShopSync(updatedShopSync);
    }

    /**
     * Returns the task that attempts to delete the shop sync with the given uid.
     *
     * @param shopSyncUid the uid of the shop sync to delete
     */
    public void deleteShopSync(@NonNull String shopSyncUid) {
        Log.d(TAG, "deleteShopSync: deleting shop sync with uid (" + shopSyncUid + ")");

        // Remove the shop sync from the shop syncs collection
        shopSyncsFirebaseReference.deleteShopSync(shopSyncUid).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Remove the shop sync from the user to shop syncs map
                userShopSyncMapFirebaseReference.removeShopSync(shopSyncUid);
                Log.d(TAG,
                      "deleteShopSync: successfully deleted shop sync with uid " + shopSyncUid);
            } else {
                Log.e(TAG, "deleteShopSync: failed to delete shop sync with uid " + shopSyncUid);
            }
        });
    }

    /**
     * Adds a shopping item with the shop sync uid, name, and "in basket" flag.
     *
     * @param shopSyncUid the uid of the shop sync the shopping item is in
     * @param name        the name of the shopping item
     * @param inBasket    if the item is in a user's basket
     * @return the shopping item model
     */
    public ShoppingItemModel addShoppingItem(@NonNull String shopSyncUid,
                                             @NonNull String name, boolean inBasket) {
        return shopSyncsFirebaseReference.addShoppingItem(shopSyncUid, name, inBasket);
    }

    /**
     * Returns the task that attempts to get the shopping item with the given shop sync uid and uid.
     *
     * @param shopSyncUid the shop sync uid
     * @param uid         the uid of the shopping item
     * @return the task that attempts to get the shopping item with the given uid
     */
    public Task<DataSnapshot> getShoppingItemWithUid(@NonNull String shopSyncUid,
                                                     @NonNull String uid) {
        return shopSyncsFirebaseReference.getShoppingItemWithUid(shopSyncUid, uid);
    }

    /**
     * Returns the task that attempts to get the shopping items with the given shop sync uid.
     *
     * @param shopSyncUid the shop sync uid
     * @return the task that attempts to get the shopping items with the given shop sync uid
     */
    public Task<DataSnapshot> getShoppingItemsWithShopSyncUid(@NonNull String shopSyncUid) {
        return shopSyncsFirebaseReference.getShoppingItemsWithShopSyncUid(shopSyncUid);
    }

    /**
     * Returns the task that attempts to get the shopping items with the given name.
     *
     * @param shopSyncUid the shop sync uid
     * @param name        the name of the shopping item
     * @return the task that attempts to get the shopping items with the given name
     */
    public Task<DataSnapshot> getShoppingItemsWithName(String shopSyncUid, String name) {
        return shopSyncsFirebaseReference.getShoppingItemsWithName(shopSyncUid, name);
    }

    /**
     * Returns the task that attempts to update the shopping item with the given shopping item.
     *
     * @param shopSyncUid         the shop sync uid
     * @param updatedShoppingItem the updated shopping item
     * @return the task that attempts to update the shopping item with the given shopping item
     */
    public Task<Void> updateShoppingItem(String shopSyncUid,
                                         ShoppingItemModel updatedShoppingItem) {
        return shopSyncsFirebaseReference.updateShoppingItem(shopSyncUid, updatedShoppingItem);
    }

    /**
     * Returns the task that attempts to delete the shopping item with the given item id.
     *
     * @param shopSyncUid the shop sync uid
     * @param itemId      the item id
     * @return the task that attempts to delete the shopping item with the given item id
     */
    public Task<Void> deleteShoppingItem(String shopSyncUid, String itemId) {
        return shopSyncsFirebaseReference.deleteShoppingItem(shopSyncUid, itemId);
    }

    /**
     * Checks if the shopping basket exists with the given shop sync uid and user uid.
     *
     * @param shopSyncUid    the shop sync uid
     * @param userUid        the user uid
     * @param resultConsumer the result consumer
     */
    public void checkIfShoppingBasketExists(String shopSyncUid, String userUid,
                                            Consumer<Boolean> resultConsumer) {
        shopSyncsFirebaseReference.checkIfShoppingBasketExists(shopSyncUid, userUid,
                                                               resultConsumer);
    }

    /**
     * Adds a shopping basket with the given shop sync uid and user uid.
     *
     * @param shopSyncUid the shop sync uid
     * @param userUid     the user uid
     * @return the shopping basket model
     */
    public ShoppingBasketModel addShoppingBasket(String shopSyncUid, String userUid) {
        return shopSyncsFirebaseReference.addShoppingBasket(shopSyncUid, userUid);
    }

    /**
     * Returns the task that attempts to get the shopping basket with the given shop sync uid and
     * user uid.
     *
     * @param shopSyncUid the shop sync uid
     * @param userUid     the user uid
     * @return the task that attempts to get the shopping basket with the given shop sync uid and
     * user uid
     */
    public Task<DataSnapshot> getShoppingBasketWithUid(String shopSyncUid, String userUid) {
        return shopSyncsFirebaseReference.getShoppingBasketWithUid(shopSyncUid, userUid);
    }

    /**
     * Returns the task that attempts to get the shopping baskets with the given shop sync uid.
     *
     * @param shopSyncUid           the shop sync uid
     * @param updatedShoppingBasket the updated shopping basket
     * @return the task that attempts to get the shopping baskets with the given shop sync uid
     */
    public Task<Void> updateShoppingBasket(String shopSyncUid,
                                           ShoppingBasketModel updatedShoppingBasket) {
        return shopSyncsFirebaseReference.updateShoppingBasket(shopSyncUid, updatedShoppingBasket);
    }

    /**
     * Returns the task that attempts to delete the shopping basket with the given shop sync uid and
     * user uid.
     *
     * @param shopSyncUid the shop sync uid
     * @param userUid     the user uid
     * @param onSuccess   the on success runnable
     * @param onFailure   the on failure consumer
     */
    public void deleteShoppingBasket(@NonNull String shopSyncUid, @NonNull String userUid,
                                     @Nullable Runnable onSuccess,
                                     @Nullable Consumer<ErrorHandle> onFailure) {
        shopSyncsFirebaseReference.deleteShoppingBasket(shopSyncUid, userUid, onSuccess, onFailure);
    }

    /**
     * Adds a basket item with the given shop sync uid, shopping basket uid, and item id.
     *
     * @param shopSyncUid       the shop sync uid
     * @param shoppingBasketUid the shopping basket uid, equal to the uid of the user who owns
     *                          the shopping basket
     * @param shoppingItemUid   the shopping item id
     * @param quantity          the quantity of the item
     * @param pricePerUnit      the price per unit of the item
     * @param onSuccess         the on success consumer
     * @param onFailure         the on failure consumer
     */
    public void addBasketItem(@NonNull String shopSyncUid, @NonNull String shoppingBasketUid,
                              @NonNull String shoppingItemUid, long quantity, double pricePerUnit,
                              @Nullable Consumer<BasketItemModel> onSuccess,
                              @Nullable Consumer<ErrorHandle> onFailure) {
        shopSyncsFirebaseReference.addBasketItem(shopSyncUid, shoppingBasketUid, shoppingItemUid,
                                                 quantity, pricePerUnit, onSuccess, onFailure);
    }

    /**
     * Checks if a purchased item exists with the given shop sync uid and basket item uid. The
     * result is consumed by the given result consumer.
     *
     * @param shopSyncUid    the shop sync uid
     * @param basketItemUid  the basket item uid
     * @param resultConsumer the result consumer
     */
    public void checkIfPurchasedItemExistsForBasketItem(@NonNull String shopSyncUid,
                                                        @NonNull String basketItemUid,
                                                        @NonNull Consumer<Boolean> resultConsumer) {
        shopSyncsFirebaseReference.checkIfPurchasedItemExistsForBasketItem(
                shopSyncUid, basketItemUid, resultConsumer);
    }

    /**
     * Checks if a purchased item exists with the given shop sync uid and shopping item uid. The
     * result is consumed by the given result consumer.
     *
     * @param shopSyncUid     the shop sync uid
     * @param shoppingItemUid the shopping item uid
     * @param resultConsumer  the result consumer
     */
    public void checkIfPurchasedItemExistsForShoppingItem(@NonNull String shopSyncUid,
                                                          @NonNull String shoppingItemUid,
                                                          @NonNull Consumer<Boolean> resultConsumer) {
        shopSyncsFirebaseReference.checkIfPurchasedItemExistsForShoppingItem(
                shopSyncUid, shoppingItemUid, resultConsumer);
    }

    /**
     * Adds a purchased item using the provided basket item.
     *
     * @param shopSyncUid       the shop sync uid
     * @param shoppingBasketUid the shopping basket uid
     * @param basketItem        the basket item
     * @param resultConsumer    the result consumer
     */
    public void addPurchasedItem(@NonNull String shopSyncUid, @NonNull String shoppingBasketUid,
                                 @NonNull BasketItemModel basketItem,
                                 @Nullable Consumer<PurchasedItemModel> resultConsumer,
                                 @Nullable Consumer<ErrorHandle> onFailure) {
        Log.d(TAG,
              "addPurchasedItem: adding purchased item with shopping basket uid (" +
                      shoppingBasketUid + ") and basket item (" + basketItem + ")");
        shopSyncsFirebaseReference.addPurchasedItem(shopSyncUid, shoppingBasketUid, basketItem,
                                                    resultConsumer, onFailure);
    }

    /**
     * Returns the task that attempts to get the purchased item with the given uid.
     *
     * @param shopSyncUid the shop sync uid
     * @param uid         the uid of the purchased item
     * @return the task that attempts to get the purchased item with the given uid
     */
    public Task<DataSnapshot> getPurchasedItemsWithUid(String shopSyncUid, String uid) {
        Log.d(TAG, "getPurchasedItemsWithUid: getting purchased items with shop sync uid + (" +
                shopSyncUid + ") and uid + ");
        return shopSyncsFirebaseReference.getPurchasedItemWithUid(shopSyncUid, uid);
    }

    /**
     * Returns the task that attempts to get the purchased items with the given shop sync uid.
     *
     * @param shopSyncUid the shop sync uid
     * @return the task that attempts to get the purchased items with the given shop sync uid
     */
    public Task<DataSnapshot> getPurchasedItemsWithShopSyncUid(String shopSyncUid) {
        Log.d(TAG, "getPurchasedItemsWithShopSyncUid: getting purchased items with shop sync uid " +
                "(" + shopSyncUid + ")");
        return shopSyncsFirebaseReference.getPurchasedItemsWithShopSyncUid(shopSyncUid);
    }

    /**
     * Returns the task that attempts to get the purchased items with the given user uid.
     *
     * @param shopSyncUid the shop sync uid
     * @param userUid     the user uid
     * @return the task that attempts to get the purchased items with the given user uid
     */
    public Task<DataSnapshot> getPurchasedItemsWithUserUid(String shopSyncUid, String userUid) {
        Log.d(TAG, "getPurchasedItemsWithUserUid: getting purchased items with shop sync uid " +
                "(" + shopSyncUid + ") and user uid (" + userUid + ")");
        return shopSyncsFirebaseReference.getPurchasedItemsWithUserUid(shopSyncUid, userUid);
    }

    /**
     * Returns the task that attempts to update the purchased item with the given purchased item.
     *
     * @param shopSyncUid          the shop sync uid
     * @param updatedPurchasedItem the updated purchased item
     * @return the task that attempts to update the purchased item with the given purchased item
     */
    public Task<Void> updatePurchasedItem(String shopSyncUid,
                                          PurchasedItemModel updatedPurchasedItem) {
        Log.d(TAG, "updatePurchasedItem: updating purchased item with shop sync uid (" +
                shopSyncUid + ") and purchased item uid (" + updatedPurchasedItem.getUid() + ")");
        return shopSyncsFirebaseReference.updatePurchasedItem(shopSyncUid, updatedPurchasedItem);
    }

    /**
     * Returns the task that attempts to delete the purchased item with the given item id.
     *
     * @param shopSyncUid the shop sync uid
     * @param itemId      the item id
     * @return the task that attempts to delete the purchased item with the given item id
     */
    public Task<Void> deletePurchasedItem(String shopSyncUid, String itemId) {
        Log.d(TAG, "deletePurchasedItem: deleting purchased item with shop sync uid (" +
                shopSyncUid + ") and item id (" + itemId + ")");
        return shopSyncsFirebaseReference.deletePurchasedItem(shopSyncUid, itemId);
    }

    // TODO: un-purchase an item
    /*
    Currently, the old shopping item and basket item are in the purchased item model.
    When a user un-purchases an item, he/she has three options:
        - un-purchase and re-add to both basket and shopping items
        - un-purchase and re-add only shopping items
        - un-purchase and delete entirely

    For the first option, the shopping item model contained in the purchased item model is added
    to the db with a new uid; this new uid is then added to the basket item model contained in the
    purchased item model. The basket item model is then added to the db with a new uid. The
    purchased item model is then deleted from the db.

    For the second option, the process outline in the first option is undergone except that the
    basket item is not re-added to the db.

    For the third option, the purchased item model and its children are deleted with nothing
    added back to the db.
    */
}
