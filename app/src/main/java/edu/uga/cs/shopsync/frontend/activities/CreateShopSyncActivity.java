package edu.uga.cs.shopsync.frontend.activities;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

import edu.uga.cs.shopsync.R;

public class CreateShopSyncActivity extends BaseActivity {

    private static final String TAG = "CreateShopSyncActivity";
    public static final int MAX_USERS_TO_INVITE = 7;

    private EditText editTextShopSyncName;
    private EditText editTextShopSyncDescription;
    private Button buttonInviteUser;
    private ListView listViewInvitedUsers;
    private Button buttonCreateShopSync;

    private List<String> invitedUsers;
    private InvitedUsersAdapter invitedUsersAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_shop_sync);

        editTextShopSyncName = findViewById(R.id.editTextShopSyncName);
        editTextShopSyncDescription = findViewById(R.id.editTextShopSyncDescription);
        buttonInviteUser = findViewById(R.id.buttonInviteUser);
        listViewInvitedUsers = findViewById(R.id.listViewInvitedUsers);
        buttonCreateShopSync = findViewById(R.id.buttonCreateShopSync);

        invitedUsers = new ArrayList<>();
        invitedUsersAdapter = new InvitedUsersAdapter(this, invitedUsers);
        listViewInvitedUsers.setAdapter(invitedUsersAdapter);

        buttonInviteUser.setOnClickListener(this::onInviteUserButtonClick);
        buttonCreateShopSync.setOnClickListener(this::onCreateShopSyncButtonClick);
    }

    private void onInviteUserButtonClick(View view) {
        // Add logic to handle inviting a user
        // Check for the maximum number of invited users and show a message if the limit is reached
        // Validate the user's email and show the result in the list view
        // Remove duplicate emails
    }

    private void onCreateShopSyncButtonClick(View view) {
        // Add logic to handle creating a new Shop Sync
        String shopSyncName = editTextShopSyncName.getText().toString().trim();
        String shopSyncDescription = editTextShopSyncDescription.getText().toString().trim();

        if (shopSyncName.isEmpty()) {
            Toast.makeText(this, "Shop Sync Name cannot be blank", Toast.LENGTH_SHORT).show();
            return;
        }

        // Add logic to create the Shop Sync with the entered data
        // Display a Toast message based on the success or failure of the operation
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
            EditText editTextEmail = convertView.findViewById(R.id.editTextEmail);
            editTextEmail.setText(email);

            ImageButton buttonDelete = convertView.findViewById(R.id.buttonDelete);

            return convertView;
        }

        @Override
        public int getCount() {
            return Math.min(super.getCount(), MAX_USERS_TO_INVITE);
        }
    }
}
