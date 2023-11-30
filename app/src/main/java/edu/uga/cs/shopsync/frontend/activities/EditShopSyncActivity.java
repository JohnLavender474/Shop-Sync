package edu.uga.cs.shopsync.frontend.activities;

import static edu.uga.cs.shopsync.frontend.Constants.SHOP_SYNC_MAX_USER_COUNT;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import edu.uga.cs.shopsync.R;
import edu.uga.cs.shopsync.backend.exceptions.IllegalNullValueException;
import edu.uga.cs.shopsync.backend.models.ShopSyncModel;
import edu.uga.cs.shopsync.backend.models.UserProfileModel;
import edu.uga.cs.shopsync.frontend.Constants;
import edu.uga.cs.shopsync.frontend.activities.CreateShopSyncActivity.InvitedUsersAdapter;

/**
 * Activity for editing a shop sync.
 */
public class EditShopSyncActivity extends BaseActivity implements ChildEventListener {

    private static final String TAG = "EditShopSyncActivity";
    private static final String USER_COUNT_STRING = "User Count: %d / %d";

    private EditText editTextShopSyncDescription;
    private TextView textViewUserCount;
    private List<String> invitedUserEmails;
    private InvitedUsersAdapter invitedUsersAdapter;

    private ShopSyncModel shopSync;
    private DatabaseReference shopSyncReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate");
        super.onCreate(savedInstanceState);

        // check if user is logged in
        FirebaseUser user = checkIfUserIsLoggedInAndFetch(true);
        if (user == null) {
            Log.e(TAG, "User is not logged in");
            return;
        }

        // fetch the shop sync id from the intent and populate the metadata
        String shopSyncUid = getIntent().getStringExtra(Constants.SHOP_SYNC_UID);
        if (shopSyncUid == null) {
            Log.e(TAG, "ShopSync started without shop sync id");
            throw new IllegalNullValueException("ShopSync started without shop sync id");
        }

        // fetch the shop sync
        applicationGraph.shopSyncsService()
                .getShopSyncWithUid(shopSyncUid)
                .addOnSuccessListener(data -> {
                    Log.d(TAG, "Task to fetch shop sync finished with data snapshot = " + data);

                    shopSync = data.getValue(ShopSyncModel.class);
                    Log.d(TAG, "Shop sync = " + shopSync);
                    if (shopSync == null || shopSync.getUid() == null ||
                            shopSync.getUid().isBlank()) {
                        Log.e(TAG, "Shop sync with uid " + shopSyncUid + " does not exist");
                        Toast.makeText(this, "Shop sync does not exist", Toast.LENGTH_SHORT).show();
                        finish();
                        return;
                    }

                    setContentView(R.layout.activity_edit_shop_sync);

                    // set up action bar
                    ActionBar actionBar = getSupportActionBar();
                    if (actionBar != null) {
                        actionBar.setDisplayHomeAsUpEnabled(true);
                    }

                    // set up the back button
                    Button backButton = findViewById(R.id.backButton);
                    backButton.setOnClickListener(v -> {
                        Log.d(TAG, "Back button clicked");
                        finish();
                    });

                    // set up the edit text view for the description
                    editTextShopSyncDescription = findViewById(R.id.editTextShopSyncDescription);
                    String description = shopSync.getDescription();
                    Log.d(TAG, "Shop sync description = " + description);
                    editTextShopSyncDescription.setText(description);

                    // set up the text view for the user count
                    textViewUserCount = findViewById(R.id.textViewUserCount);
                    int userCount = shopSync.getShoppingBaskets().size();
                    Log.d(TAG, "User count = " + userCount);
                    textViewUserCount.setText(String.format(Locale.getDefault(), USER_COUNT_STRING,
                                                            userCount, SHOP_SYNC_MAX_USER_COUNT));

                    // set up the button for inviting a user
                    Button buttonInviteUser = findViewById(R.id.buttonInviteUser);
                    buttonInviteUser.setOnClickListener(v -> onInviteUserButtonClick());

                    // set up the button for updating the shop sync
                    Button buttonUpdateShopSync = findViewById(R.id.buttonUpdateShopSync);
                    buttonUpdateShopSync.setOnClickListener(v -> onUpdateShopSyncButtonClick());

                    // set up the invited users array adapter
                    invitedUserEmails = new ArrayList<>();
                    invitedUsersAdapter = new InvitedUsersAdapter(this, invitedUserEmails,
                                                                  textViewUserCount,
                                                                  () -> invitedUserEmails.size());
                    ListView listViewInvitedUsers = findViewById(R.id.listViewEditInvitedUsers);
                    listViewInvitedUsers.setAdapter(invitedUsersAdapter);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch shop sync metadata", e);
                    Toast.makeText(this, "Failed to open the Edit Shop Sync activity",
                                   Toast.LENGTH_SHORT).show();
                    finish();
                });

        // add this as a child event listener to the shop sync collection
        shopSyncReference = applicationGraph.shopSyncsService()
                .getShopSyncsDatabaseReference().child(shopSyncUid);
        shopSyncReference.addChildEventListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        // remove this as a child event listener from the shop sync collection
        shopSyncReference.removeEventListener(this);
    }

    private void onInviteUserButtonClick() {
        // check if the maximum number of users has been reached
        int userCount = shopSync.getShoppingBaskets().size() + invitedUserEmails.size();
        if (userCount > SHOP_SYNC_MAX_USER_COUNT) {
            Toast.makeText(this, "Maximum number of users reached", Toast.LENGTH_SHORT).show();
            return;
        }

        // add an empty email to the list of invited users
        invitedUserEmails.add("");
        invitedUsersAdapter.notifyDataSetChanged();
        textViewUserCount.setText(String.format(Locale.getDefault(), USER_COUNT_STRING,
                                                userCount, SHOP_SYNC_MAX_USER_COUNT));
    }

    private void onUpdateShopSyncButtonClick() {
        // check if the current user is logged in
        FirebaseUser user = checkIfUserIsLoggedInAndFetch(true);
        if (user == null) {
            Log.e(TAG, "User is not logged in");
            Toast.makeText(this, "User is not logged in", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // shop sync description is optional and can be blank
        String shopSyncDescription = editTextShopSyncDescription.getText().toString().trim();
        shopSync.setDescription(shopSyncDescription);

        // on success
        Runnable onSuccess = () -> {
            Log.d(TAG, "Successfully updated shop sync");
            Toast.makeText(this, "Successfully updated shop sync", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(this, ShopSyncActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(Constants.SHOP_SYNC_UID, shopSync.getUid());
            startActivity(intent);

            finish();
        };

        // on failure
        Runnable onFailure = () -> {
            Log.e(TAG, "Failed to update shop sync");
            Toast.makeText(this, "Failed to update shop sync", Toast.LENGTH_SHORT).show();
        };

        // update the shop sync
        applicationGraph.shopSyncsService().updateShopSync(shopSync);

        // add invited users
        invitedUserEmails.forEach(invitedUserEmail -> applicationGraph.usersService()
                .getUserProfilesWithEmail(invitedUserEmail).addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DataSnapshot dataSnapshot = task.getResult();
                        if (dataSnapshot == null) {
                            Log.e(TAG,
                                  "Failed to fetch user profile with email " + invitedUserEmail);
                            onFailure.run();
                            return;
                        }

                        // check if the user exists
                        if (!dataSnapshot.exists()) {
                            Log.e(TAG, "User with email " + invitedUserEmail + " does not exist");
                            onFailure.run();
                            return;
                        }

                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            UserProfileModel userProfile = child.getValue(UserProfileModel.class);

                            Log.d(TAG, "Fetched from email = " + invitedUserEmail + " user " +
                                    "profile = " + userProfile);

                            if (userProfile == null || userProfile.getUserUid() == null ||
                                    userProfile.getUserUid().isBlank()) {
                                Log.e(TAG, "Failed to fetch user profile with email " +
                                        invitedUserEmail);
                                onFailure.run();
                                continue;
                            }

                            Log.d(TAG, "Adding user with email " + invitedUserEmail +
                                    " to shop sync with uid " + shopSync.getUid());

                            String userProfileUid = userProfile.getUserUid();
                            if (userProfileUid == null) {
                                Log.e(TAG, "User profile uid is null");
                                onFailure.run();
                                continue;
                            }

                            if (shopSync.getShoppingBaskets().containsKey(userProfileUid)) {
                                Log.d(TAG, "User with email " + invitedUserEmail +
                                        " is already a member of the shop sync");

                                // TODO:
                                //  notify this user that the other user is already a member of the
                                //  shop sync

                                continue;
                            }

                            // add the user to the shop sync
                            applicationGraph.shopSyncsService().addShopSyncToUser(
                                    userProfileUid, shopSync.getUid());

                            onSuccess.run();
                        }
                    } else {
                        Log.e(TAG, "Failed to fetch user profile with email " + invitedUserEmail,
                              task.getException());
                        onFailure.run();
                    }
                })
        );
    }

    @Override
    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
        Log.d(TAG, "Shop sync child added with snapshot = " + snapshot);
        if (shopSync == null) {
            Log.e(TAG, "Shop sync is null");
            return;
        }

        String key = snapshot.getKey();
        if (key == null) {
            Log.e(TAG, "Shop sync child added with null key");
            return;
        }

        Object value = snapshot.getValue();
        if (value instanceof String s) {
            switch (key) {
                case "uid" -> shopSync.setUid(s);
                case "name" -> shopSync.setName(s);
                case "description" -> shopSync.setDescription(s);
            }
        }

        Log.d(TAG, "Updated shop sync on child added = " + shopSync);
    }

    @Override
    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
        Log.d(TAG, "Shop sync child changed with snapshot = " + snapshot);
        if (shopSync == null) {
            Log.e(TAG, "Shop sync is null");
            return;
        }

        String key = snapshot.getKey();
        if (key == null) {
            Log.e(TAG, "Shop sync child changed with null key");
            return;
        }

        Object value = snapshot.getValue();
        if (value instanceof String s) {
            switch (key) {
                case "uid" -> shopSync.setUid(s);
                case "name" -> shopSync.setName(s);
                case "description" -> shopSync.setDescription(s);
            }
        }

        Log.d(TAG, "Updated shop sync on child changed = " + shopSync);
    }

    @Override
    public void onChildRemoved(@NonNull DataSnapshot snapshot) {
        Log.d(TAG, "Shop sync child removed with snapshot = " + snapshot);
        if (shopSync == null) {
            Log.e(TAG, "Shop sync is null");
            return;
        }

        String key = snapshot.getKey();
        if (key == null) {
            Log.e(TAG, "Shop sync child removed with null key");
            return;
        }

        switch (key) {
            case "uid" -> shopSync.setUid("");
            case "name" -> shopSync.setName("");
            case "description" -> shopSync.setDescription("");
        }
    }

    @Override
    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
        Log.d(TAG, "Shop sync child moved with snapshot = " + snapshot);
    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {
        Log.e(TAG, "Shop sync child cancelled with error = " + error);
    }
}
