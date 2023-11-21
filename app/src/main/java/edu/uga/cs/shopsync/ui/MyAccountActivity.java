package edu.uga.cs.shopsync.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;

import edu.uga.cs.shopsync.ApplicationGraph;
import edu.uga.cs.shopsync.R;
import edu.uga.cs.shopsync.models.UserProfileModel;

public class MyAccountActivity extends BaseActivity {

    private static final String TAG = "MyAccountActivity";

    public MyAccountActivity() {
        super();
    }

    MyAccountActivity(ApplicationGraph applicationGraph) {
        super(applicationGraph);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account);

        if (!applicationGraph.usersService().isCurrentUserSignedIn()) {
            Log.d(TAG, "onCreate: user not signed in, redirecting to main activity");

            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            return;
        }

        FirebaseUser currentUser = applicationGraph.usersService().getCurrentFirebaseUser();
        if (currentUser == null) {
            throw new IllegalStateException("Current user cannot be null at this point");
        }
        Log.d(TAG, "onCreate: user signed in with email " + currentUser.getEmail() + " and id (" +
                currentUser.getUid() + ")");

        TextView emailTextView = findViewById(R.id.textViewEmail);
        TextView usernameTextView = findViewById(R.id.textViewUsername);

        applicationGraph.usersService().getUserProfileWithUid(currentUser.getUid())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "onCreate: task for user profile is complete");

                        DataSnapshot dataSnapshot = task.getResult();
                        UserProfileModel userProfileModel =
                                dataSnapshot.getValue(UserProfileModel.class);

                        if (userProfileModel == null) {
                            throw new IllegalStateException("User profile model cannot be null " +
                                                                    "at this point");
                        }

                        Log.d(TAG, "onCreate: retrieved user profile with email " +
                                userProfileModel.getEmail() + " and username " +
                                userProfileModel.getUsername() + " and id (" +
                                userProfileModel.getUserUid() + ")");

                        emailTextView.setText(userProfileModel.getEmail());
                        usernameTextView.setText(userProfileModel.getUsername());
                    } else {
                        Log.d(TAG, "onCreate: failed to retrieve user profile");

                        Toast.makeText(getApplicationContext(), "Failed to retrieve user " +
                                "profile", Toast.LENGTH_SHORT).show();
                    }
                });

        Button buttonGoToMyShopSyncs = findViewById(R.id.buttonGoToMyShopSyncs);
        buttonGoToMyShopSyncs.setOnClickListener(v -> {
            Log.d(TAG, "onCreate: go to my shop syncs button clicked, redirecting to my shop " +
                    "syncs activity");

            Intent intent = new Intent(this, MyShopSyncsActivity.class);
            startActivity(intent);
        });

        Button buttonChangePassword = findViewById(R.id.buttonChangePassword);
        buttonChangePassword.setOnClickListener(v -> {
            Log.d(TAG, "onCreate: change password button clicked, redirecting to change password " +
                    "activity");

            Intent intent = new Intent(this, ChangePasswordActivity.class);
            startActivity(intent);
        });

        Button buttonSignOut = findViewById(R.id.buttonSignOut);
        buttonSignOut.setOnClickListener(v -> {
            Log.d(TAG, "onCreate: sign out button clicked, signing out and redirecting to the " +
                    "main activity");

            applicationGraph.usersService().signOut();

            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

            startActivity(intent);
        });
    }

}
