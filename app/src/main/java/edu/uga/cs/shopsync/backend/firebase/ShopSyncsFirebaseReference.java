package edu.uga.cs.shopsync.backend.firebase;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    private static final String USER_UIDS_FIELD = "userUids";

    private final DatabaseReference shopSyncsCollection = FirebaseDatabase.getInstance()
            .getReference(SHOP_SYNCS_COLLECTION);

    /**
     * Constructs a new ShopSyncsFirebaseReference. Empty constructor required for injection.
     */
    @Inject
    public ShopSyncsFirebaseReference() {
    }

    /**
     * Adds a shop sync with the given name, description, and user uids.
     *
     * @param name        the name of the shop sync
     * @param description the description of the shop sync
     * @param userUids    the user uids of the shop sync
     * @return the uid of the shop sync
     */
    public String addShopSync(String name, String description, String... userUids) {
        return addShopSync(name, description, new ArrayList<>(List.of(userUids)));
    }

    /**
     * Adds a shop sync with the given name, description, and user uids.
     *
     * @param name        the name of the shop sync
     * @param description the description of the shop sync
     * @param userUids    the user uids of the shop sync
     * @return the uid of the shop sync
     */
    public String addShopSync(String name, String description, List<String> userUids) {
        String uid = shopSyncsCollection.push().getKey();
        if (uid == null) {
            return null;
        }
        ShopSyncModel newShopSync = new ShopSyncModel(uid, name, description, userUids);
        shopSyncsCollection.child(uid).setValue(newShopSync);
        return uid;
    }

    /**
     * Returns the task that attempts to get the shop sync with the given uid.
     *
     * @param uid the uid of the shop sync
     * @return the task that attempts to get the shop sync with the given uid
     */
    public Task<DataSnapshot> getShopSyncWithUid(String uid) {
        return shopSyncsCollection.child(uid).get();
    }

    /**
     * Returns the task that attempts to get the shop syncs that contain the given user uid.
     *
     * @param userUid the user uid contained in the user uids field of the shop sync
     * @return the task that attempts to get the shop syncs that contain the given user uid
     */
    public Task<DataSnapshot> getShopSyncsWithUserUid(String userUid) {
        Query query = shopSyncsCollection.orderByChild(USER_UIDS_FIELD).equalTo(userUid);
        return query.get();
    }

    /**
     * Returns the task that attempts to update the shop sync.
     *
     * @param updatedShopSync the updated shop sync
     * @return the task that attempts to update the shop sync
     */
    public Task<Void> updateShopSync(ShopSyncModel updatedShopSync) {
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
    public Task<Void> deleteShopSync(String shopSyncUid) {
        return shopSyncsCollection.child(shopSyncUid).removeValue();
    }

}
