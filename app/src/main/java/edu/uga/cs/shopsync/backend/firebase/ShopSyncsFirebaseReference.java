package edu.uga.cs.shopsync.backend.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Singleton;

import edu.uga.cs.shopsync.backend.models.ShopSyncModel;

/**
 * Provides methods to modify the shop_syncs collection.
 */
@Singleton
public class ShopSyncsFirebaseReference {

    private static final String SHOP_SYNCS_COLLECTION = "shop_syncs";

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
    ShopSyncsFirebaseReference(DatabaseReference shopSyncsCollection) {
        this.shopSyncsCollection = shopSyncsCollection;
        Log.d("ShopSyncsFirebaseReference", "ShopSyncsFirebaseReference: created");
    }

    /**
     * Adds a shop sync with the given name, description, and user uids.
     *
     * @param name        the name of the shop sync
     * @param description the description of the shop sync
     * @return the uid of the shop sync
     */
    public String addShopSync(@NonNull String name, @NonNull String description) {
        String uid = shopSyncsCollection.push().getKey();
        if (uid == null) {
            return null;
        }

        ShopSyncModel newShopSync = new ShopSyncModel(uid, name, description);
        shopSyncsCollection.child(uid).setValue(newShopSync);

        return uid;
    }

    /**
     * Returns the task that attempts to get the shop sync with the given uid.
     *
     * @param uid the uid of the shop sync
     * @return the task that attempts to get the shop sync with the given uid
     */
    public Task<DataSnapshot> getShopSyncWithUid(@NonNull String uid) {
        return shopSyncsCollection.child(uid).get();
    }

    /**
     * Returns the task that attempts to update the shop sync.
     *
     * @param updatedShopSync the updated shop sync
     * @return the task that attempts to update the shop sync
     */
    public Task<Void> updateShopSync(@NonNull ShopSyncModel updatedShopSync) {
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
        return shopSyncsCollection.child(shopSyncUid).removeValue();
    }

}
