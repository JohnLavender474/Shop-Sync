package edu.uga.cs.shopsync.frontend.activities;

import static edu.uga.cs.shopsync.frontend.Constants.SHOP_SYNC_MAX_USER_COUNT;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import edu.uga.cs.shopsync.R;
import edu.uga.cs.shopsync.backend.exceptions.IllegalNullValueException;
import edu.uga.cs.shopsync.backend.models.ShopSyncModel;
import edu.uga.cs.shopsync.frontend.Constants;
import edu.uga.cs.shopsync.frontend.activities.CreateShopSyncActivity.InvitedUsersAdapter;

public class EditShopSyncActivity extends BaseActivity implements ChildEventListener {

    private static final String TAG = "InviteUsersToShopSyncActivity";
    private static final String USER_COUNT_STRING = "User Count: %d / %d";

    private EditText editTextShopSyncDescription;
    private TextView textViewUserCount;
    private List<String> invitedUserEmails;
    private InvitedUsersAdapter invitedUsersAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
                    ShopSyncModel shopSync = data.getValue(ShopSyncModel.class);
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

                    // set up the text view for the user count
                    textViewUserCount = findViewById(R.id.textViewUserCount);

                    // set up the button for inviting a user
                    Button buttonInviteUser = findViewById(R.id.buttonInviteUser);
                    buttonInviteUser.setOnClickListener(v -> onInviteUserButtonClick(shopSync));

                    // set up the invited users array adapter
                    invitedUserEmails = new ArrayList<>();
                    invitedUsersAdapter = new InvitedUsersAdapter(this, invitedUserEmails,
                                                                  textViewUserCount,
                                                                  () -> invitedUserEmails.size());
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to fetch shop sync metadata", e);
                    Toast.makeText(this, "Failed to open the Edit Shop Sync activity",
                                   Toast.LENGTH_SHORT).show();
                    finish();
                });
    }

    private void onInviteUserButtonClick(ShopSyncModel shopSync) {
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

    @Override
    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

    }

    @Override
    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

    }

    @Override
    public void onChildRemoved(@NonNull DataSnapshot snapshot) {

    }

    @Override
    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {

    }
}
