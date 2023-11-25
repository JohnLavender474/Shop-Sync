package edu.uga.cs.shopsync.frontend.activities;

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
import edu.uga.cs.shopsync.backend.models.UserProfileModel;

/**
 * Activity for displaying the user's account information.
 */
public class MyAccountActivity extends BaseActivity {

    private static final String TAG = "MyAccountActivity";

    /**
     * Default constructor for MyAccountActivity. This constructor uses the singleton instance of
     * the application graph. This constructor should only be used by the Android framework.
     */
    public MyAccountActivity() {
        super();
    }

    /**
     * Constructor for MyAccountActivity. This constructor should be used for testing purposes
     *
     * @param applicationGraph The application graph to use for this activity.
     */
    MyAccountActivity(ApplicationGraph applicationGraph) {
        super(applicationGraph);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // check if the user is logged in and fetch the current user, otherwise redirect to the
        // main activity
        FirebaseUser currentUser = checkIfUserIsLoggedInAndFetch(true);

        setContentView(R.layout.activity_my_account);

        TextView emailTextView = findViewById(R.id.textViewEmail);
        TextView usernameTextView = findViewById(R.id.textViewUsername);

        // fetch the user profile and then set the text views to display the user profile info
        applicationGraph.usersService().getUserProfileWithUid(currentUser.getUid())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "onCreate: task for user profile is complete");

                        // fetch the user profile model
                        DataSnapshot dataSnapshot = task.getResult();
                        UserProfileModel userProfileModel =
                                dataSnapshot.getValue(UserProfileModel.class);

                        // cannot proceed if user profile model is null
                        if (userProfileModel == null) {
                            Log.e(TAG, "onCreate: user profile model is null, signing out");
                            Toast.makeText(getApplicationContext(), "Failed to retrieve user " +
                                    "profile. Signing out now.", Toast.LENGTH_SHORT).show();

                            // sign out the user on error
                            applicationGraph.usersService().signOut();

                            // redirect to the main activity on error
                            Intent intent = new Intent(this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);

                            return;
                        }

                        Log.d(TAG, "onCreate: retrieved user profile with email " +
                                userProfileModel.getEmail() + " and username " +
                                userProfileModel.getUsername() + " and id (" +
                                userProfileModel.getUserUid() + ")");

                        // set the text views to display the user profile info
                        emailTextView.setText(userProfileModel.getEmail());
                        usernameTextView.setText(userProfileModel.getUsername());
                    } else {
                        signOutOnErrorAndRedirectToMainActivity("Failed to retrieve user profile." +
                                                                        " " +
                                                                        "Signing out now.");
                    }
                });

        // go to my shop syncs activity on button click
        Button buttonGoToMyShopSyncs = findViewById(R.id.buttonGoToMyShopSyncs);
        buttonGoToMyShopSyncs.setOnClickListener(v -> {
            Log.d(TAG, "onCreate: go to my shop syncs button clicked, redirecting to my shop " +
                    "syncs activity");
            Intent intent = new Intent(this, MyShopSyncsActivity.class);
            startActivity(intent);
        });

        // go to change password activity on button click
        Button buttonChangePassword = findViewById(R.id.buttonChangePassword);
        buttonChangePassword.setOnClickListener(v -> {
            Log.d(TAG, "onCreate: change password button clicked, redirecting to change password " +
                    "activity");
            Intent intent = new Intent(this, ChangePasswordActivity.class);
            startActivity(intent);
        });

        // sign out on button click
        Button buttonSignOut = findViewById(R.id.buttonSignOut);
        buttonSignOut.setOnClickListener(v -> {
            Log.d(TAG, "onCreate: sign out button clicked, signing out and redirecting to the " +
                    "main activity");

            applicationGraph.usersService().signOut();

            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            Toast.makeText(getApplicationContext(), "Signed out successfully", Toast.LENGTH_SHORT)
                    .show();
        });
    }

}
