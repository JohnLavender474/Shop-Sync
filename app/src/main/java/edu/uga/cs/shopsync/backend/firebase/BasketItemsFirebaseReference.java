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

import edu.uga.cs.shopsync.backend.models.BasketItemModel;
import edu.uga.cs.shopsync.backend.models.ShoppingItemModel;

/**
 * Provides methods to modify the basket_items collection.
 */
@Singleton
public class BasketItemsFirebaseReference {

    private static final String BASKET_ITEMS_COLLECTION = "basket_items";
    private static final String SHOPPING_BASKET_UID_FIELD = "shoppingBasketUid";

    private final DatabaseReference basketItemsCollection = FirebaseDatabase.getInstance()
            .getReference(BASKET_ITEMS_COLLECTION);

    /**
     * Constructs a new BasketItemsFirebaseReference. Empty constructor required for injection.
     */
    @Inject
    public BasketItemsFirebaseReference() {
    }

    /**
     * Adds a basket item with the given shopping basket uid and shopping item.
     *
     * @param shoppingBasketUid the shopping basket uid
     * @param shoppingItemModel the shopping item model
     * @param quantity          the quantity of the item
     * @param pricePerUnit      the price per unit of the item
     * @return the basket item model
     */
    public BasketItemModel addBasketItem(String shoppingBasketUid,
                                         ShoppingItemModel shoppingItemModel,
                                         long quantity, double pricePerUnit) {
        String uid = basketItemsCollection.push().getKey();
        if (uid == null) {
            return null;
        }
        BasketItemModel newBasketItem = new BasketItemModel(uid, shoppingBasketUid,
                                                            shoppingItemModel, quantity,
                                                            pricePerUnit);
        basketItemsCollection.child(uid).setValue(newBasketItem);
        return newBasketItem;
    }

    /**
     * Returns the task that attempts to get the basket item with the given uid.
     *
     * @param itemId the uid of the basket item
     * @return the task that attempts to get the basket item with the given uid
     */
    public Task<DataSnapshot> getBasketItemWithId(String itemId) {
        return basketItemsCollection.child(itemId).get();
    }

    /**
     * Returns the task that attempts to get the basket items with the given shopping basket uid.
     *
     * @param shoppingBasketUid the shopping basket uid
     * @return the task that attempts to get the basket items with the given shopping basket uid
     */
    public Task<DataSnapshot> getBasketItemsWithShoppingBasketUid(String shoppingBasketUid) {
        Query query = basketItemsCollection.orderByChild(SHOPPING_BASKET_UID_FIELD)
                .equalTo(shoppingBasketUid);
        return query.get();
    }

    /**
     * Returns the task that attempts to update the basket item with the given basket item.
     *
     * @param updatedBasketItem the updated basket item
     * @return the task that attempts to update the basket item with the given basket item
     */
    public Task<Void> updateBasketItem(BasketItemModel updatedBasketItem) {
        String itemId = updatedBasketItem.getShoppingBasketUid();
        Map<String, Object> basketItemValues = updatedBasketItem.toMap();

        Map<String, Object> childUpdates = new HashMap<>();
        childUpdates.put("/" + itemId, basketItemValues);

        return basketItemsCollection.updateChildren(childUpdates);
    }

    /**
     * Returns the task that attempts to delete the basket item with the given item id.
     *
     * @param itemId the item id
     * @return the task that attempts to delete the basket item with the given item id
     */
    public Task<Void> deleteBasketItem(String itemId) {
        return basketItemsCollection.child(itemId).removeValue();
    }
}

