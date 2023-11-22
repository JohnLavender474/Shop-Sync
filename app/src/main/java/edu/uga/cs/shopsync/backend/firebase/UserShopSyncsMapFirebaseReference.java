package edu.uga.cs.shopsync.backend.firebase;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import javax.inject.Inject;

/**
 * Firebase reference for user to shop syncs map.
 */
public class UserShopSyncsMapFirebaseReference {

    private static final String TAG = "UserShopSyncsMapFirebaseReference";
    private static final String USER_TO_SHOP_SYNCS_MAP = "user_to_shop_syncs_map";
    private static final String SHOP_SYNC_TO_USERS_MAP = "shop_sync_to_users_map";

    private final DatabaseReference userToShopSyncsMapReference;
    private final DatabaseReference shopSyncToUsersMapReference;

    /**
     * Constructs a new UserShopSyncsMapFirebaseReference. Empty constructor required for
     * injection.
     */
    @Inject
    public UserShopSyncsMapFirebaseReference() {
        userToShopSyncsMapReference = FirebaseDatabase.getInstance()
                .getReference(USER_TO_SHOP_SYNCS_MAP);
        shopSyncToUsersMapReference = FirebaseDatabase.getInstance()
                .getReference(SHOP_SYNC_TO_USERS_MAP);
        Log.d(TAG, "UserShopSyncsMapFirebaseReference: created");
    }

    /**
     * Constructs a new UserShopSyncsMapFirebaseReference. Used for testing only.
     *
     * @param userToShopSyncsMapReference the reference to the user to shop syncs map
     */
    UserShopSyncsMapFirebaseReference(@NonNull DatabaseReference userToShopSyncsMapReference,
                                      @NonNull DatabaseReference shopSyncToUsersMapReference) {
        this.userToShopSyncsMapReference = userToShopSyncsMapReference;
        this.shopSyncToUsersMapReference = shopSyncToUsersMapReference;
        Log.d(TAG, "UserShopSyncsMapFirebaseReference: created");
    }

    /**
     * Adds a user to the shop sync.
     *
     * @param userId     the user id
     * @param shopSyncId the shop sync id
     */
    public void addShopSyncToUser(@NonNull String userId, @NonNull String shopSyncId) {
        Log.d(TAG, "addShopSyncToUser: adding user (" + userId + ") and shop sync (" + shopSyncId
                + ") mappings");
        userToShopSyncsMapReference.child(userId).child(shopSyncId).setValue(true);
        shopSyncToUsersMapReference.child(shopSyncId).child(userId).setValue(true);
    }

    /**
     * Returns the task that attempts to get the shop syncs associated with the given user id.
     *
     * @param userUid the user id
     * @return the task that attempts to get the shop syncs associated with the given user id
     */
    public Task<DataSnapshot> getShopSyncsAssociatedWithUser(@NonNull String userUid) {
        Log.d(TAG, "getShopSyncsAssociatedWithUser: getting shop syncs associated with user ("
                + userUid + ")");
        return userToShopSyncsMapReference.child(userUid).get();
    }

    /**
     * Returns the task that attempts to get the users associated with the given shop sync id.
     *
     * @param shopSyncUid the shop sync id
     * @return the task that attempts to get the users associated with the given shop sync id
     */
    public Task<DataSnapshot> getUsersAssociatedWithShopSync(@NonNull String shopSyncUid) {
        Log.d(TAG, "getUsersAssociatedWithShopSync: getting users associated with shop sync ("
                + shopSyncUid + ")");
        return shopSyncToUsersMapReference.child(shopSyncUid).get();
    }

    /**
     * Removes a user from the shop sync.
     *
     * @param userId     the user id
     * @param shopSyncId the shop sync id
     */
    public void removeUserShopSyncMapping(@NonNull String userId, @NonNull String shopSyncId) {
        Log.d(TAG, "removeUserShopSyncMapping: removing user (" + userId + ") and shop sync ("
                + shopSyncId + ") mappings");
        userToShopSyncsMapReference.child(userId).child(shopSyncId).removeValue();
        shopSyncToUsersMapReference.child(shopSyncId).child(userId).removeValue();
    }

    /**
     * Removes all shop sync mappings for the user.
     *
     * @param userId the user id
     */
    public void removeUser(@NonNull String userId) {
        Log.d(TAG, "removeUser: removing all shop sync mappings for user (" + userId + ")");

        // get all shop syncs associated with the user and delete the mappings
        getShopSyncsAssociatedWithUser(userId).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot dataSnapshot = task.getResult();

                // for each shop sync associated with the user, remove the user from the shop sync
                for (DataSnapshot shopSyncSnapshot : dataSnapshot.getChildren()) {
                    String shopSyncId = shopSyncSnapshot.getKey();
                    if (shopSyncId != null) {
                        removeUserShopSyncMapping(userId, shopSyncId);
                    }
                }

                // remove the user from the user to shop syncs map
                userToShopSyncsMapReference.child(userId).removeValue();

                Log.d(TAG, "removeUser: successfully removed all shop sync mappings for user");
            } else {
                Log.e(TAG, "removeUser: failed to get shop syncs associated with user",
                      task.getException());
            }
        });
    }

    /**
     * Removes all user mappings for the shop sync.
     *
     * @param shopSyncUid the shop sync uid
     */
    public void removeShopSync(@NonNull String shopSyncUid) {
        Log.d(TAG, "removeShopSync: removing all user mappings for shop sync (" + shopSyncUid +
                ")");

        // get all users associated with the shop sync and delete the mappings
        getUsersAssociatedWithShopSync(shopSyncUid).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                DataSnapshot dataSnapshot = task.getResult();

                // for each user associated with the shop sync, remove the shop sync from the user
                for (DataSnapshot userSnapshot : dataSnapshot.getChildren()) {
                    String userId = userSnapshot.getKey();
                    if (userId != null) {
                        removeUserShopSyncMapping(userId, shopSyncUid);
                    }
                }

                // remove the shop sync from the shop sync to users map
                shopSyncToUsersMapReference.child(shopSyncUid).removeValue();

                Log.d(TAG, "removeShopSync: successfully removed all user mappings for shop sync");
            } else {
                Log.e(TAG, "removeShopSync: failed to get users associated with shop sync",
                      task.getException());
            }
        });
    }
}
