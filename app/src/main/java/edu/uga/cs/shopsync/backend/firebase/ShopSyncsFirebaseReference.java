package edu.uga.cs.shopsync.backend.firebase;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.inject.Singleton;

import edu.uga.cs.shopsync.backend.models.BasketItemModel;
import edu.uga.cs.shopsync.backend.models.PurchasedItemModel;
import edu.uga.cs.shopsync.backend.models.ShopSyncModel;
import edu.uga.cs.shopsync.backend.models.ShoppingBasketModel;
import edu.uga.cs.shopsync.backend.models.ShoppingItemModel;
import edu.uga.cs.shopsync.utils.ErrorHandle;
import edu.uga.cs.shopsync.utils.ErrorType;

/**
 * Provides methods to modify the shop_syncs collection and its nested collections.
 */
@Singleton
public class ShopSyncsFirebaseReference {

    private static final String TAG = "ShopSyncsFirebaseReference";
    public static final String SHOP_SYNCS_COLLECTION = "shop_syncs";
    public static final String SHOPPING_ITEMS_NESTED_COLLECTION = "shopping_items";
    public static final String SHOPPING_BASKETS_NESTED_COLLECTION = "shopping_baskets";
    public static final String PURCHASED_ITEMS_NESTED_COLLECTION = "purchased_items";
    public static final String NAME_FIELD = "name";
    public static final String USER_UID_FIELD = "userUid";

    private final DatabaseReference shopSyncsCollection;

    /**
     * Constructs a new ShopSyncsFirebaseReference. Empty constructor required for injection.
     */
    @Inject
    public ShopSyncsFirebaseReference() {
        shopSyncsCollection = FirebaseDatabase.getInstance().getReference(SHOP_SYNCS_COLLECTION);
        Log.d("ShopSyncsFirebaseReference", "ShopSyncsFirebaseReference: created");
    }

    /**
     * Constructs a new ShopSyncsFirebaseReference. Used for testing only.
     *
     * @param shopSyncsCollection the reference to the shop syncs collection
     */
    ShopSyncsFirebaseReference(@NonNull DatabaseReference shopSyncsCollection) {
        this.shopSyncsCollection = shopSyncsCollection;
        Log.d("ShopSyncsFirebaseReference", "ShopSyncsFirebaseReference: created");
    }

    /**
     * Returns the shopping baskets collection for the shop sync with the given uid.
     *
     * @param shopSyncUid the uid of the shop sync
     * @return the shopping baskets collection for the shop sync with the given uid
     */
    public DatabaseReference getShoppingItemsCollection(@NonNull String shopSyncUid) {
        Log.d("ShopSyncsFirebaseReference", "getShoppingItemsCollection: shop sync uid (" +
                shopSyncUid + ")");
        return shopSyncsCollection.child(shopSyncUid).child(SHOPPING_ITEMS_NESTED_COLLECTION);
    }

    /**
     * Returns the shopping baskets collection for the shop sync with the given uid.
     *
     * @param shopSyncUid the uid of the shop sync
     * @return the shopping baskets collection for the shop sync with the given uid
     */
    public DatabaseReference getShoppingBasketsCollection(@NonNull String shopSyncUid) {
        Log.d("ShopSyncsFirebaseReference", "getShoppingBasketsCollection: shop sync uid (" +
                shopSyncUid + ")");
        return shopSyncsCollection.child(shopSyncUid).child(SHOPPING_BASKETS_NESTED_COLLECTION);
    }

    /**
     * Returns the purchased items collection for the shop sync with the given uid.
     *
     * @param shopSyncUid the uid of the shop sync
     * @return the purchased items collection for the shop sync with the given uid
     */
    public DatabaseReference getPurchasedItemsCollection(@NonNull String shopSyncUid) {
        Log.d("ShopSyncsFirebaseReference", "getPurchasedItemsCollection: shop sync uid (" +
                shopSyncUid + ")");
        return shopSyncsCollection.child(shopSyncUid).child(PURCHASED_ITEMS_NESTED_COLLECTION);
    }

    /**
     * Adds a shop sync with the given name, description, and user uids.
     *
     * @param name        the name of the shop sync
     * @param description the description of the shop sync
     * @return the uid of the shop sync
     */
    public ShopSyncModel addShopSync(@NonNull String name, @Nullable String description,
                                     @Nullable Collection<ShoppingItemModel> shoppingItems,
                                     @Nullable Map<String, ShoppingBasketModel> shoppingBaskets,
                                     @Nullable Collection<PurchasedItemModel> purchasedItems) {
        Log.d("ShopSyncsFirebaseReference", "addShopSync: name (" + name + "), description (" +
                description + ")");

        String uid = shopSyncsCollection.push().getKey();
        if (uid == null) {
            return null;
        }

        if (description == null) {
            description = "";
        }

        Map<String, ShoppingItemModel> shoppingItemsMap = new HashMap<>();
        if (shoppingItems != null) {
            shoppingItems.forEach(shoppingItem -> shoppingItemsMap.put(
                    shoppingItem.getShoppingItemUid(), shoppingItem));
        }

        if (shoppingBaskets == null) {
            shoppingBaskets = new HashMap<>();
        }

        Map<String, PurchasedItemModel> purchasedItemsMap = new HashMap<>();
        if (purchasedItems != null) {
            purchasedItems.forEach(purchasedItem -> purchasedItemsMap.put(purchasedItem.getPurchasedItemUid(),
                                                                          purchasedItem));
        }

        ShopSyncModel newShopSync = new ShopSyncModel(uid, name, description, shoppingItemsMap,
                                                      shoppingBaskets, purchasedItemsMap);
        shopSyncsCollection.child(uid).setValue(newShopSync);

        return newShopSync;
    }

    /**
     * Returns the task that attempts to get the shop sync with the given uid.
     *
     * @param uid the uid of the shop sync
     * @return the task that attempts to get the shop sync with the given uid
     */
    public Task<DataSnapshot> getShopSyncWithUid(@NonNull String uid) {
        Log.d("ShopSyncsFirebaseReference", "getShopSyncWithUid: uid (" + uid + ")");
        return shopSyncsCollection.child(uid).get();
    }

    /**
     * Returns the task that attempts to update the shop sync.
     *
     * @param updatedShopSync the updated shop sync
     * @return the task that attempts to update the shop sync
     */
    public Task<Void> updateShopSync(@NonNull ShopSyncModel updatedShopSync) {
        Log.d("ShopSyncsFirebaseReference", "updateShopSync: updated shop sync (" +
                updatedShopSync + ")");

        String shopSyncUid = updatedShopSync.getUid();
        Map<String, Object> shopSyncValues = updatedShopSync.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/" + shopSyncUid, shopSyncValues);

        return shopSyncsCollection.updateChildren(childUpdates);
    }

    /**
     * Returns the task that attempts to delete the shop sync with the given uid.
     *
     * @param shopSyncUid the uid of the shop sync to delete
     * @return the task that attempts to delete the shop sync with the given uid
     */
    public Task<Void> deleteShopSync(@NonNull String shopSyncUid) {
        Log.d("ShopSyncsFirebaseReference", "deleteShopSync: shop sync uid (" + shopSyncUid + ")");
        return shopSyncsCollection.child(shopSyncUid).removeValue();
    }

    /**
     * Adds a shopping item to the shop sync with the given uid, then returns the newly created
     * {@link ShoppingItemModel}.
     *
     * @param shopSyncUid the uid of the shop sync
     * @param name        the name of the shopping item
     * @param inBasket    if the item is in a user's basket
     * @return the newly created {@link ShoppingItemModel}
     */
    public ShoppingItemModel addShoppingItem(@NonNull String shopSyncUid, @NonNull String name,
                                             boolean inBasket) {
        Log.d("ShopSyncsFirebaseReference", "addShoppingItem: shop sync uid (" + shopSyncUid +
                "), name (" + name + "), in basket (" + inBasket + ")");

        DatabaseReference shoppingItemsCollection = getShoppingItemsCollection(shopSyncUid);

        String uid = shoppingItemsCollection.push().getKey();
        if (uid == null) {
            Log.e("ShopSyncsFirebaseReference", "addShoppingItem: uid is null");
            return null;
        }

        ShoppingItemModel newShoppingItem = new ShoppingItemModel(uid, name, inBasket);
        shoppingItemsCollection.child(uid).setValue(newShoppingItem);

        Log.d("ShopSyncsFirebaseReference", "addShoppingItem: added shopping item with " + "name "
                + name + ", in basket " + inBasket + ", and uid (" + uid + ")");
        return newShoppingItem;
    }

    /**
     * Returns the task that attempts to get the shopping item with the given uid.
     *
     * @param shopSyncUid the uid of the shop sync
     * @param uid         the uid of the shopping item
     * @return the task that attempts to get the shopping item with the given uid
     */
    public Task<DataSnapshot> getShoppingItemWithUid(@NonNull String shopSyncUid,
                                                     @NonNull String uid) {
        Log.d("ShopSyncsFirebaseReference", "getShoppingItemWithUid: shop sync uid (" +
                shopSyncUid + "), uid (" + uid + ")");
        return getShoppingItemsCollection(shopSyncUid).child(uid).get();
    }

    /**
     * Returns the task that attempts to get the shopping items with the given shop sync uid.
     *
     * @param shopSyncUid the uid of the shop sync
     * @return the task that attempts to get the shopping items with the given shop sync uid
     */
    public Task<DataSnapshot> getShoppingItemsWithShopSyncUid(@NonNull String shopSyncUid) {
        Log.d("ShopSyncsFirebaseReference", "getShoppingItemsWithShopSyncUid: shop sync uid (" +
                shopSyncUid + ")");
        return getShoppingItemsCollection(shopSyncUid).get();
    }

    /**
     * Returns the task that attempts to get the shopping items with the given name.
     *
     * @param shopSyncUid the uid of the shop sync
     * @param name        the name of the shopping item
     * @return the task that attempts to get the shopping items with the given name
     */
    public Task<DataSnapshot> getShoppingItemsWithName(@NonNull String shopSyncUid,
                                                       @NonNull String name) {
        Log.d("ShopSyncsFirebaseReference", "getShoppingItemsWithName: shop sync uid (" +
                shopSyncUid + "), name (" + name + ")");
        return getShoppingItemsCollection(shopSyncUid).orderByChild(NAME_FIELD).equalTo(name).get();
    }

    /**
     * Returns the task that attempts to update the shopping item.
     *
     * @param shopSyncUid         the uid of the shop sync
     * @param updatedShoppingItem the updated shopping item
     * @return the task that attempts to get the shopping items with the given price per unit
     */
    public Task<Void> updateShoppingItem(@NonNull String shopSyncUid,
                                         @NonNull ShoppingItemModel updatedShoppingItem) {
        Log.d("ShopSyncsFirebaseReference", "updateShoppingItem: shop sync uid (" + shopSyncUid +
                "), updated shopping item (" + updatedShoppingItem + ")");

        String uid = updatedShoppingItem.getShoppingItemUid();
        Map<String, Object> shoppingItemValues = updatedShoppingItem.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/" + uid, shoppingItemValues);

        return getShoppingItemsCollection(shopSyncUid).updateChildren(childUpdates);
    }

    /**
     * Returns the task that attempts to delete the shopping item with the given uid.
     *
     * @param shopSyncUid the uid of the shop sync
     * @param uid         the uid of the shopping item
     * @return the task that attempts to delete the shopping item with the given uid
     */
    public Task<Void> deleteShoppingItem(@NonNull String shopSyncUid, @NonNull String uid) {
        Log.d("ShopSyncsFirebaseReference", "deleteShoppingItem: shop sync uid (" + shopSyncUid +
                "), uid (" + uid + ")");
        return getShoppingItemsCollection(shopSyncUid).child(uid).removeValue();
    }

    /**
     * Checks if a shopping basket exists for the given user uid.
     *
     * @param shopSyncUid    the uid of the shop sync
     * @param userUid        the uid of the user
     * @param resultConsumer the consumer that accepts the result
     */
    public void checkIfShoppingBasketExists(@NonNull String shopSyncUid, @NonNull String userUid,
                                            @NonNull Consumer<Boolean> resultConsumer) {
        Log.d("ShopSyncsFirebaseReference", "checkIfShoppingBasketExists: shop sync uid (" +
                shopSyncUid + "), user uid (" + userUid + ")");

        DatabaseReference shoppingBasketsCollection = getShoppingBasketsCollection(shopSyncUid);
        shoppingBasketsCollection.child(userUid).get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot dataSnapshot = task.getResult();
                resultConsumer.accept(dataSnapshot != null && dataSnapshot.exists());
            } else {
                Log.e(TAG, "checkIfShoppingBasketExists: failed to get shopping basket");
            }
        });
    }

    /**
     * Adds a shopping basket to the shop sync with the given uid, then returns the newly created
     * {@link ShoppingBasketModel}. The uid of the shopping basket is the same as the uid of the
     * user who owns the shopping basket.
     *
     * @param shopSyncUid the uid of the shop sync
     * @param userUid     the uid of the user
     * @return the newly created {@link ShoppingBasketModel}
     */
    public ShoppingBasketModel addShoppingBasket(@NonNull String shopSyncUid,
                                                 @NonNull String userUid) {
        Log.d("ShopSyncsFirebaseReference", "addShoppingBasket: shop sync uid (" + shopSyncUid +
                "), user uid (" + userUid + ")");

        DatabaseReference shoppingBasketsCollection = getShoppingBasketsCollection(shopSyncUid);

        ShoppingBasketModel newShoppingBasket = new ShoppingBasketModel(userUid, new HashMap<>());
        shoppingBasketsCollection.child(userUid).setValue(newShoppingBasket);

        Log.d("ShopSyncsFirebaseReference", "addShoppingBasket: added shopping basket with " +
                "uid (" + userUid + ")");
        return newShoppingBasket;
    }

    /**
     * Returns the task that attempts to get the shopping basket with the given uid.
     *
     * @param shopSyncUid       the uid of the shop sync
     * @param shoppingBasketUid the uid of the shopping basket, same as the uid of the user who
     *                          owns the shopping basket
     * @return the task that attempts to get the shopping basket with the given uid
     */
    public Task<DataSnapshot> getShoppingBasketWithUid(@NonNull String shopSyncUid,
                                                       @NonNull String shoppingBasketUid) {
        Log.d("ShopSyncsFirebaseReference", "getShoppingBasketWithUid: shop sync uid (" +
                shopSyncUid + "), shopping basket uid (" + shoppingBasketUid + ")");
        return getShoppingBasketsCollection(shopSyncUid).child(shoppingBasketUid).get();
    }

    /**
     * Returns the task that attempts to update the shopping basket.
     *
     * @param shopSyncUid           the uid of the shop sync
     * @param updatedShoppingBasket the updated shopping basket
     * @return the task that attempts to update the shopping basket
     */
    public Task<Void> updateShoppingBasket(@NonNull String shopSyncUid,
                                           @NonNull ShoppingBasketModel updatedShoppingBasket) {
        Log.d("ShopSyncsFirebaseReference", "updateShoppingBasket: shop sync uid (" + shopSyncUid +
                "), updated shopping basket (" + updatedShoppingBasket + ")");

        String userUid = updatedShoppingBasket.getUid();
        Map<String, Object> shoppingBasketValues = updatedShoppingBasket.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/" + userUid, shoppingBasketValues);

        return getShoppingBasketsCollection(shopSyncUid).updateChildren(childUpdates);
    }

    /**
     * Returns the task that attempts to delete the shopping basket with the given uid.
     *
     * @param shopSyncUid       the uid of the shop sync
     * @param shoppingBasketUid the uid of the shopping basket, same as the uid of the user who
     *                          owns the shopping basket.
     */
    public void deleteShoppingBasket(@NonNull String shopSyncUid, @NonNull String shoppingBasketUid,
                                     @Nullable Runnable onSuccess,
                                     @Nullable Consumer<ErrorHandle> onFailure) {
        Log.d("ShopSyncsFirebaseReference", "deleteShoppingBasket: shop sync uid (" + shopSyncUid +
                "), shopping basket uid (" + shoppingBasketUid + ")");

        getShoppingBasketWithUid(shopSyncUid, shoppingBasketUid).addOnSuccessListener(data -> {
            ShoppingBasketModel shoppingBasket = data.getValue(ShoppingBasketModel.class);
            if (shoppingBasket == null) {
                Log.e(TAG, "deleteShoppingBasket: shopping basket is null");
                return;
            }

            // set corresponding shopping items to not in basket
            shoppingBasket.getBasketItems()
                    .forEach((key, basketItem) -> getShoppingItemsCollection(shopSyncUid)
                            .child(basketItem.getShoppingItemUid()).child("inBasket")
                            .setValue(false));

            // delete shopping basket
            getShoppingBasketsCollection(shopSyncUid).child(shoppingBasketUid).removeValue()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && onSuccess != null) {
                            onSuccess.run();
                        } else if (onFailure != null) {
                            onFailure.accept(new ErrorHandle(ErrorType.TASK_FAILED,
                                                             "Failed to delete shopping basket"));
                        }
                    });
        });
    }

    /**
     * Adds a basket item to the shop sync with the given uid, then returns the newly created
     * {@link BasketItemModel}.
     *
     * @param shopSyncUid       the uid of the shop sync
     * @param shoppingBasketUid the uid of the shopping basket, same as the uid of the suer who
     *                          owns teh shopping basket
     * @param shoppingItemUid   the uid of the shopping item
     * @param quantity          the quantity of the basket item
     * @param pricePerUnit      the price per unit of the basket item
     * @param onSuccess         the consumer that accepts the newly created basket item
     *                          if the operation is successful
     * @param onFailure         the consumer that accepts the error handle if the operation fails
     */
    public void addBasketItem(@NonNull String shopSyncUid, @NonNull String shoppingBasketUid,
                              @NonNull String shoppingItemUid, long quantity,
                              double pricePerUnit, @Nullable Consumer<BasketItemModel> onSuccess,
                              @Nullable Consumer<ErrorHandle> onFailure) {
        Log.d("ShopSyncsFirebaseReference", "addBasketItem: shop sync uid (" + shopSyncUid +
                "), shopping basket uid (" + shoppingBasketUid + "), shopping item uid (" +
                shoppingItemUid + "), quantity (" + quantity + "), price per unit (" +
                pricePerUnit + ")");

        getShoppingBasketWithUid(shopSyncUid, shoppingBasketUid).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot dataSnapshot = task.getResult();
                ShoppingBasketModel shoppingBasket =
                        dataSnapshot.getValue(ShoppingBasketModel.class);

                if (shoppingBasket == null) {
                    Log.e(TAG, "addBasketItem: shopping basket is null");
                    if (onFailure != null) {
                        onFailure.accept(new ErrorHandle(ErrorType.ILLEGAL_NULL_VALUE,
                                                         "Shopping basket is null"));
                    }
                    return;
                }

                // check if basket item already exists
                if (shoppingBasket.getBasketItems().containsKey(shoppingItemUid)) {
                    Log.e(TAG, "addBasketItem: basket item already exists");
                    if (onFailure != null) {
                        onFailure.accept(new ErrorHandle(ErrorType.ENTITY_ALREADY_EXISTS,
                                                         "Basket item already exists"));
                    }
                    return;
                }

                // add the basket item to the shopping cart
                BasketItemModel newBasketItem = new BasketItemModel(shoppingBasketUid,
                                                                    shoppingItemUid,
                                                                    quantity, pricePerUnit);
                shoppingBasket.getBasketItems().put(shoppingItemUid, newBasketItem);
                // update the shopping cart
                updateShoppingBasket(shopSyncUid, shoppingBasket);

                // set the shopping item's "in basket" flag to true
                getShoppingItemsCollection(shopSyncUid).child(shoppingItemUid).child("inBasket")
                        .setValue(true);

                if (onSuccess != null) {
                    onSuccess.accept(newBasketItem);
                }
            } else if (onFailure != null) {
                onFailure.accept(new ErrorHandle(ErrorType.TASK_FAILED,
                                                 "Failed to get shopping basket"));
            }
        });
    }

    /**
     * Checks if a purchased item exists for the given shop sync uid and basket item uid. The result
     * is passed to the given consumer.
     *
     * @param shopSyncUid    the uid of the shop sync
     * @param basketItemUid  the uid of the basket item
     * @param resultConsumer the consumer that accepts the result
     */
    public void checkIfPurchasedItemExistsForBasketItem(@NonNull String shopSyncUid,
                                                        @NonNull String basketItemUid,
                                                        @NonNull Consumer<Boolean> resultConsumer) {
        Log.d("ShopSyncsFirebaseReference", "checkIfPurchasedItemExistsForBasketItem: shop sync " +
                "uid (" + shopSyncUid + "), basket item uid (" + basketItemUid + ")");

        DatabaseReference purchasedItemsCollection = getPurchasedItemsCollection(shopSyncUid);
        purchasedItemsCollection.orderByChild("basketItem/uid").equalTo(basketItemUid)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DataSnapshot dataSnapshot = task.getResult();
                        resultConsumer.accept(dataSnapshot != null && dataSnapshot.exists());
                    } else {
                        Log.e(TAG, "checkIfPurchasedItemExists: failed to get purchased item");
                    }
                });
    }

    /**
     * Checks if a purchased item exists for the given shop sync uid and shopping item uid. The
     * result is passed to the given consumer.
     *
     * @param shopSyncUid     the uid of the shop sync
     * @param shoppingItemUid the uid of the shopping item
     * @param resultConsumer  the consumer that accepts the result
     */
    public void checkIfPurchasedItemExistsForShoppingItem(@NonNull String shopSyncUid,
                                                          @NonNull String shoppingItemUid,
                                                          @NonNull Consumer<Boolean> resultConsumer) {
        Log.d("ShopSyncsFirebaseReference", "checkIfPurchasedItemExistsForShoppingItem: shop " +
                "sync uid (" + shopSyncUid + "), shopping item uid (" + shoppingItemUid + ")");

        DatabaseReference purchasedItemsCollection = getPurchasedItemsCollection(shopSyncUid);
        purchasedItemsCollection.orderByChild("shoppingItem/uid").equalTo(shoppingItemUid)
                .get().addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DataSnapshot dataSnapshot = task.getResult();
                        resultConsumer.accept(dataSnapshot != null && dataSnapshot.exists());
                    } else {
                        Log.e(TAG, "checkIfPurchasedItemExists: failed to get purchased item");
                    }
                });
    }

    /**
     * Adds a purchased item to the shop sync with the given uid, then returns the newly created
     * {@link PurchasedItemModel}.
     *
     * @param shopSyncUid       the uid of the shop sync
     * @param shoppingBasketUid the uid of the shopping basket that contains the basket item; this
     *                          value is equal to the uid of the user that owns the shopping basket
     * @param basketItem        the basket item
     * @param resultConsumer    the consumer that accepts the result if the operation is successful
     * @param onFailure         the consumer that accepts the error handle if the operation fails
     */
    public void addPurchasedItem(@NonNull String shopSyncUid, @NonNull String shoppingBasketUid,
                                 @NonNull BasketItemModel basketItem, @NonNull String userEmail,
                                 @Nullable Consumer<PurchasedItemModel> resultConsumer,
                                 @Nullable Consumer<ErrorHandle> onFailure) {
        Log.d("ShopSyncsFirebaseReference", "addPurchasedItem: shop sync uid (" + shopSyncUid +
                "), user uid (" + shoppingBasketUid + "), basket item (" + basketItem + ")");

        // get shopping item
        getShoppingItemWithUid(shopSyncUid, basketItem.getShoppingItemUid())
                .addOnSuccessListener(shoppingItemData -> {
                    ShoppingItemModel shoppingItem = shoppingItemData.getValue(
                            ShoppingItemModel.class);

                    if (shoppingItem == null) {
                        Log.e(TAG, "addPurchasedItem: shopping item is null");
                        if (onFailure != null) {
                            onFailure.accept(new ErrorHandle(ErrorType.ILLEGAL_NULL_VALUE,
                                                             "Shopping item is null"));
                        }
                        return;
                    }

                    DatabaseReference purchasedItemsCollection =
                            getPurchasedItemsCollection(shopSyncUid);

                    // generate new uid for the purchased item
                    String uid = purchasedItemsCollection.push().getKey();
                    if (uid == null) {
                        Log.e("ShopSyncsFirebaseReference", "addPurchasedItem: uid is" +
                                " null");
                        if (onFailure != null) {
                            onFailure.accept(new ErrorHandle(ErrorType.ILLEGAL_NULL_VALUE,
                                                             "Uid for new purchased item is null"));
                        }
                        return;
                    }

                    // delete the corresponding basket item from the shopping basket
                    getShoppingBasketWithUid(shopSyncUid, shoppingBasketUid).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DataSnapshot dataSnapshot = task.getResult();
                            ShoppingBasketModel shoppingBasket =
                                    dataSnapshot.getValue(ShoppingBasketModel.class);

                            if (shoppingBasket == null) {
                                Log.e(TAG, "addPurchasedItem: shopping basket is null");
                                return;
                            }

                            shoppingBasket.getBasketItems().remove(basketItem.getShoppingItemUid());
                            updateShoppingBasket(shopSyncUid, shoppingBasket);
                        } else {
                            Log.e(TAG, "addPurchasedItem: failed to get shopping basket");
                            if (onFailure != null) {
                                onFailure.accept(new ErrorHandle(ErrorType.TASK_FAILED,
                                                                 "Failed to get shopping basket"));
                            }
                        }
                    });

                    // delete the shopping item
                    getShoppingItemsCollection(shopSyncUid).child(basketItem.getShoppingItemUid())
                            .removeValue();

                    // create new purchased item
                    PurchasedItemModel newPurchasedItem = new PurchasedItemModel(
                            uid, userEmail, shoppingItem, basketItem);
                    purchasedItemsCollection.child(uid).setValue(newPurchasedItem);

                    if (resultConsumer != null) {
                        resultConsumer.accept(newPurchasedItem);
                    }
                });
    }

    /**
     * Returns the task that attempts to get the purchased item with the given uid.
     *
     * @param shopSyncUid the uid of the shop sync
     * @param uid         the uid of the purchased item
     * @return the task that attempts to get the purchased item with the given uid
     */
    public Task<DataSnapshot> getPurchasedItemWithUid(@NonNull String shopSyncUid,
                                                      @NonNull String uid) {
        Log.d("ShopSyncsFirebaseReference", "getPurchasedItemWithUid: shop sync uid (" +
                shopSyncUid + "), uid (" + uid + ")");
        return getPurchasedItemsCollection(shopSyncUid).child(uid).get();
    }

    /**
     * Returns the task that attempts to get the purchased items with the given shop sync uid.
     *
     * @param shopSyncUid the uid of the shop sync
     * @return the task that attempts to get the purchased items with the given shop sync uid
     */
    public Task<DataSnapshot> getPurchasedItemsWithShopSyncUid(String shopSyncUid) {
        Log.d("ShopSyncsFirebaseReference", "getPurchasedItemsWithShopSyncUid: shop sync uid (" +
                shopSyncUid + ")");
        return getPurchasedItemsCollection(shopSyncUid).get();
    }

    /**
     * Returns the task that attempts to get the purchased items with the given shop sync uid
     * and user uid.
     *
     * @param shopSyncUid the uid of the shop sync
     * @param userUid     the uid of the user
     * @return the task that attempts to get the purchased items with the given user uid
     */
    public Task<DataSnapshot> getPurchasedItemsWithUserUid(@NonNull String shopSyncUid,
                                                           @NonNull String userUid) {
        Log.d("ShopSyncsFirebaseReference", "getPurchasedItemsWithUserUid: shop sync uid (" +
                shopSyncUid + "), user uid (" + userUid + ")");
        return getPurchasedItemsCollection(shopSyncUid).orderByChild(USER_UID_FIELD)
                .equalTo(userUid).get();
    }

    /**
     * Returns the task that attempts to update the purchased item.
     *
     * @param shopSyncUid          the uid of the shop sync
     * @param updatedPurchasedItem the updated purchased item
     * @return the task that attempts to update the purchased item
     */
    public Task<Void> updatePurchasedItem(@NonNull String shopSyncUid,
                                          @NonNull PurchasedItemModel updatedPurchasedItem) {
        Log.d("ShopSyncsFirebaseReference", "updatePurchasedItem: shop sync uid (" + shopSyncUid +
                "), updated purchased item (" + updatedPurchasedItem + ")");

        String uid = updatedPurchasedItem.getPurchasedItemUid();
        Map<String, Object> purchasedItemValues = updatedPurchasedItem.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/" + uid, purchasedItemValues);

        return getPurchasedItemsCollection(shopSyncUid).updateChildren(childUpdates);
    }

    /**
     * Returns the task that attempts to delete the purchased item with the given uid.
     *
     * @param shopSyncUid      the uid of the shop sync
     * @param purchasedItemUid the uid of the purchased item
     * @return the task that attempts to delete the purchased item with the given uid
     */
    public Task<Void> deletePurchasedItem(@NonNull String shopSyncUid,
                                          @NonNull String purchasedItemUid) {
        Log.d("ShopSyncsFirebaseReference", "deletePurchasedItem: shop sync uid (" + shopSyncUid +
                "), uid (" + purchasedItemUid + ")");
        return getPurchasedItemsCollection(shopSyncUid).child(purchasedItemUid).removeValue();
    }

}