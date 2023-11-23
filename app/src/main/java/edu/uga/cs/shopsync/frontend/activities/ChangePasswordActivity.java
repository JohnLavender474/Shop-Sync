package edu.uga.cs.shopsync.frontend.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import edu.uga.cs.shopsync.ApplicationGraph;
import edu.uga.cs.shopsync.R;

/**
 * Activity for changing the user's password.
 *
 * This activity provides a user interface for changing the password of a logged-in user.
 * It requires the user to input their old password for re-authentication and then
 * enter the new password along with its confirmation.
 *
 */
public class ChangePasswordActivity extends BaseActivity {

    private static final String TAG = "ChangePasswordActivity";
    private EditText oldPasswordEditText, newPasswordEditText, confirmNewPasswordEditText;
    private Button changePasswordButton;
    private FirebaseUser user;

    /**
     * Default constructor for ChangePasswordActivity.
     */
    public ChangePasswordActivity() {
        super();
    }

    /**
     * Constructor for ChangePasswordActivity with ApplicationGraph.
     *
     * @param applicationGraph The application graph for dependency injection.
     */
    ChangePasswordActivity(ApplicationGraph applicationGraph) {
        super(applicationGraph);
    }

    /**
     * Called when the activity is starting.
     *
     * This method initializes the UI components and sets up the event listeners for the buttons.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being
     *                           shut down then this Bundle contains the data it most recently
     *                           supplied. Otherwise, it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        oldPasswordEditText = findViewById(R.id.oldPassword);
        newPasswordEditText = findViewById(R.id.newPassword);
        confirmNewPasswordEditText = findViewById(R.id.confirmNewPassword);
        changePasswordButton = findViewById(R.id.changePasswordButton);

        user = FirebaseAuth.getInstance().getCurrentUser();

        changePasswordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String oldPassword = oldPasswordEditText.getText().toString();
                String newPassword = newPasswordEditText.getText().toString();
                String confirmNewPassword = confirmNewPasswordEditText.getText().toString();

                if (!newPassword.equals(confirmNewPassword)) {
                    Toast.makeText(ChangePasswordActivity.this, "New passwords do not match.", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!isValidPassword(newPassword)) {
                    Toast.makeText(ChangePasswordActivity.this, "Password does not meet complexity requirements.", Toast.LENGTH_SHORT).show();
                    return;
                }

                reAuthenticateUser(oldPassword);
            }
        });
    }

    /**
     * Checks if the given password meets the complexity requirements.
     *
     * @param password The password to be validated.
     * @return true if the password meets the requirements, false otherwise.
     */
    private boolean isValidPassword(String password) {
        return password.length() > 6; // could change this for more checks
    }

    /**
     * Updates the user's password to the new password.
     *
     * This method updates the user's password and navigates to MyAccountActivity on success.
     *
     * @param newPassword The new password to be set for the user.
     */
    private void updatePassword(String newPassword) {
        user.updatePassword(newPassword).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(ChangePasswordActivity.this, "Password updated successfully.", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(ChangePasswordActivity.this, MyAccountActivity.class);
                    startActivity(intent);

                    finish();
                } else {
                    Toast.makeText(ChangePasswordActivity.this, "Password update failed.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    /**
     * Re-authenticates the user with their old password.
     *
     * This method is used for security purposes to confirm the identity of the user
     * before allowing a password change.
     *
     * @param oldPassword The current password of the user for re-authentication.
     */
    private void reAuthenticateUser(String oldPassword) {
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), oldPassword);

        user.reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    String newPassword = newPasswordEditText.getText().toString();
                    updatePassword(newPassword);
                } else {
                    Toast.makeText(ChangePasswordActivity.this, "Re-authentication failed.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
