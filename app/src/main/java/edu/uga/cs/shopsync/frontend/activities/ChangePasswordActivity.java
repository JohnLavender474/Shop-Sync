package edu.uga.cs.shopsync.frontend.activities;

import static edu.uga.cs.shopsync.utils.PasswordStrength.MEDIUM;
import static edu.uga.cs.shopsync.utils.PasswordStrength.MIN_LENGTH;
import static edu.uga.cs.shopsync.utils.PasswordStrength.PasswordStrengthCalculationResult;
import static edu.uga.cs.shopsync.utils.PasswordStrength.PasswordStrengthCriteria;
import static edu.uga.cs.shopsync.utils.PasswordStrength.STRONG;
import static edu.uga.cs.shopsync.utils.PasswordStrength.WEAK;
import static edu.uga.cs.shopsync.utils.PasswordStrength.calculate;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseUser;

import java.util.Map;

import edu.uga.cs.shopsync.ApplicationGraph;
import edu.uga.cs.shopsync.R;
import edu.uga.cs.shopsync.backend.exceptions.IllegalNullValueException;
import edu.uga.cs.shopsync.frontend.utils.TextWatcherAdapter;
import edu.uga.cs.shopsync.utils.PasswordStrength;

/**
 * Activity for changing the user's password.
 * <p>
 * This activity provides a user interface for changing the password of a logged-in user.
 * It requires the user to input their old password for re-authentication and then
 * enter the new password along with its confirmation.
 */
public class ChangePasswordActivity extends BaseActivity {

    private static final String TAG = "ChangePasswordActivity";

    private PasswordStrength passwordStrength;
    private Map<PasswordStrengthCriteria, Boolean> passwordStrengthCriteria;

    private Button changePasswordButton;

    private EditText oldPasswordEditText;
    private EditText newPasswordEditText;
    private EditText confirmNewPasswordEditText;

    private TextView passwordStrengthTextView;
    private TextView oldPasswordNotSameAsNewPasswordTextView;
    private TextView confirmPasswordMatchesTextView;
    private TextView hasMinLengthTextView;
    private TextView hasSpecialCharTextView;
    private TextView hasUpperCaseTextView;
    private TextView hasLowerCaseTextView;
    private TextView hasAlphanumericTextView;
    private TextView hasDigitTextView;

    /**
     * Default constructor for ChangePasswordActivity.
     */
    public ChangePasswordActivity() {
        super();
    }

    /**
     * Constructor for ChangePasswordActivity with ApplicationGraph. Used for testing purposes only.
     *
     * @param applicationGraph The application graph for dependency injection.
     */
    ChangePasswordActivity(ApplicationGraph applicationGraph) {
        super(applicationGraph);
    }

    /**
     * Called when the activity is starting. This method initializes the UI components and sets up
     * the event listeners for the buttons.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being
     *                           shut down, then this Bundle contains the data it most recently
     *                           supplied. Otherwise, it is null.
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: called");
        super.onCreate(savedInstanceState);

        // check if user is logged in
        FirebaseUser user = checkIfUserIsLoggedInAndFetch(true);

        // set the layout for the activity
        setContentView(R.layout.activity_change_password);

        // initialize password strength and criteria fields
        passwordStrength = WEAK;
        passwordStrengthCriteria = PasswordStrength.createCriteriaMap();

        // initialize password criteria text views
        passwordStrengthTextView = findViewById(R.id.passwordStrengthTextView);
        oldPasswordNotSameAsNewPasswordTextView =
                findViewById(R.id.oldPasswordNotSameAsNewPasswordTextView);
        confirmPasswordMatchesTextView = findViewById(R.id.confirmPasswordMatchesTextView);
        hasMinLengthTextView = findViewById(R.id.hasMinLengthTextView);
        hasSpecialCharTextView = findViewById(R.id.hasSpecialCharTextView);
        hasUpperCaseTextView = findViewById(R.id.hasUpperCaseTextView);
        hasLowerCaseTextView = findViewById(R.id.hasLowerCaseTextView);
        hasAlphanumericTextView = findViewById(R.id.hasAlphanumericTextView);
        hasDigitTextView = findViewById(R.id.hasDigitTextView);

        // initialize old password edit text
        oldPasswordEditText = findViewById(R.id.oldPassword);
        oldPasswordEditText.setText("");
        oldPasswordEditText.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                updateUI();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                updateUI();
            }
        });

        // initialize new password edit text
        newPasswordEditText = findViewById(R.id.newPassword);
        newPasswordEditText.setText("");
        newPasswordEditText.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                PasswordStrengthCalculationResult result = calculate(s.toString());

                passwordStrength = result.strength();
                passwordStrengthCriteria = result.criteriaMet();

                passwordStrengthTextView.setTextColor(passwordStrength.color);
                String message = "Password strength: " + passwordStrength.name() + ".";
                passwordStrengthTextView.setText(message);
                int passwordStrengthColor;
                if (passwordStrength == STRONG) {
                    passwordStrengthColor = Color.GREEN;
                } else if (passwordStrength == MEDIUM) {
                    passwordStrengthColor = Color.YELLOW;
                } else {
                    passwordStrengthColor = Color.RED;
                }
                passwordStrengthTextView.setTextColor(passwordStrengthColor);

                updateUI();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                updateUI();
            }
        });

        // initialize confirm new password edit text
        confirmNewPasswordEditText = findViewById(R.id.confirmNewPassword);
        confirmNewPasswordEditText.setText("");
        confirmNewPasswordEditText.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateUI();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                updateUI();
            }
        });

        // back button
        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            // TODO: go back to parent activity which might be MyAccountActivity or MainActivity

            Log.d(TAG, "onCreate: back button clicked");
            Intent intent = new Intent(ChangePasswordActivity.this, MyAccountActivity.class);
            startActivity(intent);
            finish();
        });

        // change password button
        changePasswordButton = findViewById(R.id.changePasswordButton);
        changePasswordButton.setOnClickListener(v -> {
            Log.d(TAG, "changePasswordButton: change password button clicked");

            String oldPassword = oldPasswordEditText.getText().toString();
            String newPassword = newPasswordEditText.getText().toString();
            String confirmNewPassword = confirmNewPasswordEditText.getText().toString();

            if (!newPassword.equals(confirmNewPassword)) {
                Toast.makeText(ChangePasswordActivity.this, "Confirm new password field " +
                                       "does not match the new password field.",
                               Toast.LENGTH_SHORT).show();
                Log.e(TAG, "changePasswordButton: new passwords do not match");
                return;
            }

            if (!isValidPassword(newPassword)) {
                Toast.makeText(ChangePasswordActivity.this, "Password does not meet " +
                        "complexity requirements.", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "changePasswordButton: password does not meet complexity requirements");
                return;
            }

            reAuthenticateUser(user, oldPassword, newPassword);
        });

        // reset everything
        updateUI();
    }

    private void updateUI() {
        // fetch old password and new password fields
        String oldPassword = oldPasswordEditText.getText().toString();
        String newPassword = newPasswordEditText.getText().toString();

        // check if old password is the same as the new password
        if (oldPassword.equals(newPassword)) {
            String message = "Old password cannot be the same as the new password.";
            oldPasswordNotSameAsNewPasswordTextView.setText(message);
            oldPasswordNotSameAsNewPasswordTextView.setVisibility(View.VISIBLE);
        } else {
            oldPasswordNotSameAsNewPasswordTextView.setVisibility(View.INVISIBLE);
        }

        // fetch confirm new password field
        String confirmNewPassword = confirmNewPasswordEditText.getText().toString();

        // check if confirm new password matches new password
        if (!newPassword.equals(confirmNewPassword)) {
            String message = "Does not match new password.";
            confirmPasswordMatchesTextView.setText(message);
            confirmPasswordMatchesTextView.setVisibility(View.VISIBLE);
        } else {
            confirmPasswordMatchesTextView.setVisibility(View.INVISIBLE);
        }

        // update password criteria text views
        passwordStrengthCriteria.forEach((criteria, value) -> {
            TextView textView;
            String message;

            switch (criteria) {
                case HAS_DIGIT -> {
                    textView = hasDigitTextView;
                    if (value) {
                        message = "Password must contain at least one digit.";
                    } else {
                        message = "Password contains at least one digit.";
                    }
                }
                case HAS_ALPHANUMERIC -> {
                    textView = hasAlphanumericTextView;
                    if (value) {
                        message = "Password must contain at least one alphanumeric character.";
                    } else {
                        message = "Password contains at least one alphanumeric character.";
                    }
                }
                case HAS_LOWER_CASE -> {
                    textView = hasLowerCaseTextView;
                    if (value) {
                        message = "Password must contain at least one lower case letter.";
                    } else {
                        message = "Password contains at least one lower case letter.";
                    }
                }
                case HAS_UPPER_CASE -> {
                    textView = hasUpperCaseTextView;
                    if (value) {
                        message = "Password must contain at least one upper case letter.";
                    } else {
                        message = "Password contains at least one upper case letter.";
                    }
                }
                case HAS_SPECIAL_CHAR -> {
                    textView = hasSpecialCharTextView;
                    if (value) {
                        message = "Password must contain at least one special character.";
                    } else {
                        message = "Password contains at least one special character.";
                    }
                }
                case MEET_MIN_LENGTH -> {
                    textView = hasMinLengthTextView;
                    if (value) {
                        message = "Password must be between 8 and 25 characters long.";
                    } else {
                        message = "Password is between 8 and 25 characters long.";
                    }
                }
                default -> throw new IllegalStateException("Unexpected value: " + criteria);
            }

            if (textView == null) {
                throw new IllegalNullValueException("TextView cannot be null at this point");
            }

            textView.setTextColor(value ? Color.GREEN : Color.RED);
            textView.setText(message);
        });
    }

    private boolean isValidPassword(@NonNull String password) {
        return password.length() >= MIN_LENGTH && !passwordStrengthCriteria.containsValue(false);
    }

    private void reAuthenticateUser(@NonNull FirebaseUser user, @NonNull String oldPassword,
                                    @NonNull String newPassword) {
        String email = user.getEmail();
        if (email == null) {
            Log.e(TAG, "reAuthenticateUser: user email is null");
            throw new IllegalNullValueException("reAuthenticateUser: user email is null");
        }

        AuthCredential credential = EmailAuthProvider.getCredential(email, oldPassword);

        user.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "reAuthenticateUser: re-authentication successful");
                updatePassword(user, newPassword);
            } else {
                Log.e(TAG, "reAuthenticateUser: re-authentication failed", task.getException());
                Toast.makeText(ChangePasswordActivity.this, "Re-authentication failed.",
                               Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updatePassword(FirebaseUser user, String newPassword) {
        user.updatePassword(newPassword).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "updatePassword: password updated successfully");

                Toast.makeText(ChangePasswordActivity.this, "Password updated successfully.",
                               Toast.LENGTH_SHORT).show();

                Intent intent = new Intent(ChangePasswordActivity.this,
                                           MyAccountActivity.class);
                startActivity(intent);
                finish();
            } else {
                Log.e(TAG, "updatePassword: password update failed", task.getException());
                Toast.makeText(ChangePasswordActivity.this, "Password update failed.",
                               Toast.LENGTH_SHORT).show();
            }
        });
    }
}
