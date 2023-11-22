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
    public void addShopSync(@NonNull String name, @Nullable String description,
                            @NonNull List<String> userUids, @Nullable Runnable onSuccess,
                            @Nullable Runnable onFailure) {
        // Add shop sync to shop syncs collection
        String shopSyncUid = shopSyncsFirebaseReference
                .addShopSync(name, description, null, null, null);

        // If the shop sync uid is null, then the shop sync was not added
        if (shopSyncUid == null) {
            Log.e(TAG, "addShopSync: failed to add shop sync");
            if (onFailure != null) {
                onFailure.run();
            }
            return;
        }

        // Map the shop sync to the users
        userUids.forEach(userUid -> userShopSyncMapFirebaseReference
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
     * @param userUid              the user uid
     * @param shopSyncUidsConsumer the shop sync uids consumer
     * @param onError              the on error consumer
     */
    public void getShopSyncsForUser(@NonNull String userUid,
                                    @NonNull Consumer<List<String>> shopSyncUidsConsumer,
                                    @Nullable Consumer<ErrorHandle> onError) {
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
     * Returns the task that attempts to delete the shop sync with the given uid.
     *
     * @param shopSyncUid the uid of the shop sync to delete
     */
    public void deleteShopSync(String shopSyncUid) {
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
}

