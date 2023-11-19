package edu.uga.cs.shopsync.services;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import edu.uga.cs.shopsync.firebase.ShopSyncsFirebaseReference;
import edu.uga.cs.shopsync.models.ShopSyncModel;

/**
 * Service class for shop syncs.
 */
@Singleton
public class ShopSyncsService {

    private static final String TAG = "ShopSyncsService";

    private final ShopSyncsFirebaseReference shopSyncsFirebaseReference;

    @Inject
    public ShopSyncsService(@NonNull ShopSyncsFirebaseReference shopSyncsFirebaseReference) {
        this.shopSyncsFirebaseReference = shopSyncsFirebaseReference;
        Log.d(TAG, "ShopSyncsService: created");
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
        return shopSyncsFirebaseReference.addShopSync(name, description, userUids);
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
        return shopSyncsFirebaseReference.addShopSync(name, description, userUids);
    }

    /**
     * Returns the task that attempts to get the shop sync with the given uid.
     *
     * @param uid the uid of the shop sync
     * @return the task that attempts to get the shop sync with the given uid
     */
    public Task<DataSnapshot> getShopSyncWithUid(String uid) {
        return shopSyncsFirebaseReference.getShopSyncWithUid(uid);
    }

    /**
     * Returns the task that attempts to get the shop syncs that contain the given user uid.
     *
     * @param userUid the user uid contained in the user uids field of the shop sync
     * @return the task that attempts to get the shop syncs that contain the given user uid
     */
    public Task<DataSnapshot> getShopSyncsWithUserUid(String userUid) {
        return shopSyncsFirebaseReference.getShopSyncsWithUserUid(userUid);
    }

    /**
     * Returns the task that attempts to update the shop sync.
     *
     * @param updatedShopSync the updated shop sync
     * @return the task that attempts to update the shop sync
     */
    public Task<Void> updateShopSync(ShopSyncModel updatedShopSync) {
        return shopSyncsFirebaseReference.updateShopSync(updatedShopSync);
    }

    /**
     * Returns the task that attempts to delete the shop sync with the given uid.
     *
     * @param shopSyncUid the uid of the shop sync to delete
     * @return the task that attempts to delete the shop sync with the given uid
     */
    public Task<Void> deleteShopSync(String shopSyncUid) {
        return shopSyncsFirebaseReference.deleteShopSync(shopSyncUid);
    }
}

