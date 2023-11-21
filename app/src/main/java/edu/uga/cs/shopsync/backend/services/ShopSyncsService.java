package edu.uga.cs.shopsync.backend.services;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import edu.uga.cs.shopsync.backend.firebase.ShopSyncsFirebaseReference;
import edu.uga.cs.shopsync.backend.firebase.UserShopSyncsMapFirebaseReference;

/**
 * Service class for shop syncs.
 */
@Singleton
public class ShopSyncsService {

    private static final String TAG = "ShopSyncsService";

    private final ShopSyncsFirebaseReference shopSyncsFirebaseReference;
    private final UserShopSyncsMapFirebaseReference userShopSyncsMapFirebaseReference;

    @Inject
    public ShopSyncsService(@NonNull ShopSyncsFirebaseReference shopSyncsFirebaseReference,
                            @NonNull UserShopSyncsMapFirebaseReference
                                    userShopSyncsMapFirebaseReference) {
        this.shopSyncsFirebaseReference = shopSyncsFirebaseReference;
        this.userShopSyncsMapFirebaseReference = userShopSyncsMapFirebaseReference;
        Log.d(TAG, "ShopSyncsService: created");
    }

    /**
     * Adds a shop sync with the given name, description, and user uids.
     *
     * @param name        the name of the shop sync
     * @param description the description of the shop sync
     * @param userUids    the user uids of the shop sync
     */
    public void addShopSync(@NonNull String name, @NonNull String description,
                            @NonNull List<String> userUids) {
        addShopSync(name, description, userUids, null, null);
    }

    /**
     * Adds a shop sync with the given name, description, and user uids.
     *
     * @param name        the name of the shop sync
     * @param description the description of the shop sync
     * @param userUids    the user uids of the shop sync
     */
    public void addShopSync(@NonNull String name, @NonNull String description,
                            @NonNull List<String> userUids, @Nullable Runnable onSuccess,
                            @Nullable Runnable onFailure) {
        // Add shop sync to shop syncs collection
        String shopSyncUid = shopSyncsFirebaseReference.addShopSync(name, description);

        // If the shop sync uid is null, then the shop sync was not added
        if (shopSyncUid == null) {
            Log.e(TAG, "addShopSync: failed to add shop sync");
            if (onFailure != null) {
                onFailure.run();
            }
            return;
        }

        // Map the shop sync to the users
        userUids.forEach(userUid -> userShopSyncsMapFirebaseReference
                .addShopSyncToUser(userUid, shopSyncUid));

        Log.d(TAG, "addShopSync: successfully added shop sync with uid " + shopSyncUid);
        if (onSuccess != null) {
            Log.d(TAG, "addShopSync: running on success runnable");
            onSuccess.run();
        }
    }

    /**
     * Returns the task that attempts to get the shop sync with the given uid.
     *
     * @param uid the uid of the shop sync
     * @return the task that attempts to get the shop sync with the given uid
     */
    public Task<DataSnapshot> getShopSyncWithUid(@NonNull String uid) {
        return shopSyncsFirebaseReference.getShopSyncWithUid(uid);
    }

    /**
     * Returns the task that attempts to update the shop sync.
     *
     * @param userUid the user uid
     * @return the task that attempts to update the shop sync
     */
    public Task<DataSnapshot> getShopSyncsForUser(@NonNull String userUid) {
        return userShopSyncsMapFirebaseReference.getShopSyncsAssociatedWithUser(userUid);
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

