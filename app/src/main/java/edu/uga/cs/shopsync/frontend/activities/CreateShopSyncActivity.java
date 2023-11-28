package edu.uga.cs.shopsync.frontend.activities;

import static edu.uga.cs.shopsync.frontend.Constants.SHOP_SYNC_MAX_USER_COUNT;

import android.content.Context;
import android.os.Bundle;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

import edu.uga.cs.shopsync.R;
import edu.uga.cs.shopsync.backend.models.ShopSyncModel;
import edu.uga.cs.shopsync.backend.models.UserProfileModel;
import edu.uga.cs.shopsync.frontend.Constants;
import edu.uga.cs.shopsync.frontend.utils.TextWatcherAdapter;
import edu.uga.cs.shopsync.utils.ErrorHandle;

/**
 * Activity for creating a new Shop Sync.
 */
public class CreateShopSyncActivity extends BaseActivity {

    private static final String TAG = "CreateShopSyncActivity";
    private static final String USER_COUNT_STRING = "User Count: %d / %d";

    private EditText editTextShopSyncName;
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
            return;
        }

        setContentView(R.layout.activity_create_shop_sync);

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

        // set up edit text views for name and description
        editTextShopSyncName = findViewById(R.id.editTextShopSyncName);
        editTextShopSyncDescription = findViewById(R.id.editTextShopSyncDescription);

        // set up view for user count
        textViewUserCount = findViewById(R.id.textViewUserCount);
        textViewUserCount.setText(String.format(Locale.getDefault(), USER_COUNT_STRING, 1,
                                                SHOP_SYNC_MAX_USER_COUNT));

        // set up button for inviting a user
        Button buttonInviteUser = findViewById(R.id.buttonInviteUser);
        buttonInviteUser.setOnClickListener(this::onInviteUserButtonClick);

        // set up button for creating the shop sync
        Button buttonCreateShopSync = findViewById(R.id.buttonCreateShopSync);
        buttonCreateShopSync.setOnClickListener(this::onCreateShopSyncButtonClick);

        // set up the invited users array adapter
        invitedUserEmails = new ArrayList<>();
        invitedUsersAdapter = new InvitedUsersAdapter(this, invitedUserEmails);
        ListView listViewInvitedUsers = findViewById(R.id.listViewInvitedUsers);
        listViewInvitedUsers.setAdapter(invitedUsersAdapter);
    }

    private int getUserCount() {
        // +1 for the current user
        return invitedUserEmails.size() + 1;
    }

    private void onInviteUserButtonClick(View view) {
        // check if the maximum number of users has been reached
        if (getUserCount() >= SHOP_SYNC_MAX_USER_COUNT) {
            Toast.makeText(this, "Maximum number of users reached", Toast.LENGTH_SHORT).show();
            return;
        }

        // add an empty email to the list of invited users
        invitedUserEmails.add("");
        invitedUsersAdapter.notifyDataSetChanged();
        textViewUserCount.setText(String.format(Locale.getDefault(), USER_COUNT_STRING,
                                                getUserCount(), SHOP_SYNC_MAX_USER_COUNT));
    }

    private void onCreateShopSyncButtonClick(View view) {
        // check if the current user is logged in
        FirebaseUser user = checkIfUserIsLoggedInAndFetch(true);
        if (user == null) {
            Log.e(TAG, "onCreateShopSyncButtonClick: user is not logged in");
            return;
        }

        // check that the shop sync name is not blank
        String shopSyncName = editTextShopSyncName.getText().toString().trim();
        if (shopSyncName.isBlank()) {
            Toast.makeText(this, "Shop Sync Name cannot be blank", Toast.LENGTH_SHORT).show();
            return;
        }

        // shop sync description is optional and can be blank
        String shopSyncDescription = editTextShopSyncDescription.getText().toString().trim();

        // on success consumer
        Consumer<ShopSyncModel> onSuccess = shopSync -> {
            Log.d(TAG, "onCreateShopSyncButtonClick: shop sync created successfully");
            Toast.makeText(this, "Shop Sync created successfully", Toast.LENGTH_SHORT).show();
            finish();

            invitedUserEmails.add(user.getEmail());

            invitedUserEmails.forEach(invitedUserEmail -> applicationGraph.usersService()
                    .getUserProfilesWithEmail(invitedUserEmail).addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            DataSnapshot dataSnapshot = task.getResult();
                            if (dataSnapshot == null) {
                                Log.e(TAG, "onCreateShopSyncButtonClick: failed to get " + "user " +
                                        "profile with email " + invitedUserEmail);
                                createNotificationForFailedToInviteUser(shopSync, invitedUserEmail);
                                return;
                            }

                            for (DataSnapshot child : dataSnapshot.getChildren()) {
                                UserProfileModel userProfile =
                                        child.getValue(UserProfileModel.class);
                                if (userProfile == null || userProfile.getUserUid().isBlank()) {
                                    Log.e(TAG, "onCreateShopSyncButtonClick: failed to get " +
                                            "user profile with email " + invitedUserEmail);
                                    createNotificationForFailedToInviteUser(shopSync,
                                                                            invitedUserEmail);
                                    return;
                                }

                                Log.d(TAG, "Fetching user profile with email " + invitedUserEmail +
                                        " succeeded: " + userProfile);

                                // TODO:
                                // instead of adding the shop sync to the user, send a
                                // notification to the user to accept the invitation

                                applicationGraph.shopSyncsService().addShopSyncToUser(
                                        userProfile.getUserUid(), shopSync.getUid());

                                // there should only be one child hence the break statement
                                break;
                            }
                        } else {
                            Log.e(TAG, "onCreateShopSyncButtonClick: failed to get user " +
                                    "profile with email " + invitedUserEmail);
                            createNotificationForFailedToInviteUser(shopSync, invitedUserEmail);
                        }
                    }));
        };

        // on failure consumer
        Consumer<ErrorHandle> onFailure = errorHandle -> {
            Log.e(TAG,
                  "onCreateShopSyncButtonClick: failed to create shop sync due to error: " + errorHandle);
            Toast.makeText(this, "Failed to create Shop Sync", Toast.LENGTH_SHORT).show();
        };

        applicationGraph.shopSyncsService().addShopSync
                (shopSyncName, shopSyncDescription, List.of(user.getUid()), onSuccess, onFailure);
    }

    private void createNotificationForFailedToInviteUser(@NonNull ShopSyncModel shopSync,
                                                         @NonNull String invitedUserEmail) {
        String type = Constants.NotificationTypes.FAILED_TO_INVITE_USER;
        String title = "Failed to invite user";
        String body =
                "Failed to invite user with email " + invitedUserEmail + " to Shop Sync " + shopSync.getName() + " (" + shopSync.getUid() + ")";

        // TODO: create notification
        // dummy commit
    }

    public class InvitedUsersAdapter extends ArrayAdapter<String> {

        public InvitedUsersAdapter(Context context, List<String> invitedUsers) {
            super(context, 0, invitedUsers);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext())
                        .inflate(R.layout.row_invited_user, parent, false);
            }

            String email = getItem(position);
            EditText editTextEmail = convertView.findViewById(R.id.editTextUserEmail);
            editTextEmail.setText(email);
            // change the text of the email in the internal list on text change
            TextWatcher editTextEmailWatcher = new TextWatcherAdapter() {
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    Log.d(TAG, "onTextChanged: email: " + email + ", s: " + s);
                    invitedUserEmails.set(position, s.toString());
                }
            };
            // a dirty but necessary hack to prevent the text watcher from being added multiple
            // times
            editTextEmail.setOnFocusChangeListener((v, hasFocus) -> {
                if (hasFocus) {
                    editTextEmail.addTextChangedListener(editTextEmailWatcher);
                } else {
                    editTextEmail.removeTextChangedListener(editTextEmailWatcher);
                }
            });

            // set up delete button
            ImageButton buttonDelete = convertView.findViewById(R.id.buttonDelete);
            buttonDelete.setOnClickListener(v -> {
                Log.d(TAG, "Delete button clicked for email: " + email);
                invitedUserEmails.remove(email);
                textViewUserCount.setText(String.format(Locale.getDefault(), USER_COUNT_STRING,
                                                        getUserCount(), SHOP_SYNC_MAX_USER_COUNT));
                notifyDataSetChanged();
            });

            return convertView;
        }

        @Override
        public int getCount() {
            return Math.min(super.getCount(), SHOP_SYNC_MAX_USER_COUNT - 1);
        }
    }
}
