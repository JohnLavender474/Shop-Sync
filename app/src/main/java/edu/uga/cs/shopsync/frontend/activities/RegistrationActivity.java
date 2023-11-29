package edu.uga.cs.shopsync.frontend.activities;

import static edu.uga.cs.shopsync.utils.PasswordStrength.MIN_LENGTH;
import static edu.uga.cs.shopsync.utils.PasswordStrength.PasswordStrengthCalculationResult;
import static edu.uga.cs.shopsync.utils.PasswordStrength.PasswordStrengthCriteria;
import static edu.uga.cs.shopsync.utils.PasswordStrength.WEAK;
import static edu.uga.cs.shopsync.utils.PasswordStrength.calculate;
import static edu.uga.cs.shopsync.utils.PasswordStrength.createCriteriaMap;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.Map;
import java.util.function.Consumer;

import edu.uga.cs.shopsync.ApplicationGraph;
import edu.uga.cs.shopsync.R;
import edu.uga.cs.shopsync.backend.models.UserProfileModel;
import edu.uga.cs.shopsync.frontend.utils.TextWatcherAdapter;
import edu.uga.cs.shopsync.utils.ErrorHandle;
import edu.uga.cs.shopsync.utils.ErrorType;
import edu.uga.cs.shopsync.utils.PasswordStrength;

public class RegistrationActivity extends BaseActivity {

    private static final String TAG = "RegistrationActivity";

    public static final int USERNAME_MIN_LENGTH = 8;
    public static final int USERNAME_MAX_LENGTH = 25;

    private EditText editTextEmail;
    private EditText editTextUsername;
    private EditText editTextPassword;
    private EditText editTextConfirmPassword;

    private TextView passwordStrengthTextView;
    private TextView confirmValidEmailTextView;
    private TextView confirmValidUsernameTextView;
    private TextView confirmPasswordMatchesTextView;
    private TextView hasMinLengthTextView;
    private TextView hasSpecialCharTextView;
    private TextView hasUpperCaseTextView;
    private TextView hasLowerCaseTextView;
    private TextView hasAlphanumericTextView;
    private TextView hasDigitTextView;

    private PasswordStrength passwordStrength;
    private Map<PasswordStrengthCriteria, Boolean> passwordStrengthCriteria;

    /**
     * Default constructor. Used by Android when recreating activities due to a configuration
     * change. Must be public.
     */
    public RegistrationActivity() {
        super();
    }

    /**
     * Constructor used for testing.
     *
     * @param applicationGraph The application graph.
     */
    RegistrationActivity(ApplicationGraph applicationGraph) {
        super(applicationGraph);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Redirect if already logged in
        if (applicationGraph.usersService().isCurrentUserSignedIn()) {
            redirectToMyAccountActivity();
            return;
        }

        setContentView(R.layout.activity_registration);

        passwordStrength = WEAK;
        passwordStrengthCriteria = createCriteriaMap();

        // Initialize views
        initializeViews();

        // Set up listeners
        setUpListeners();

        // Set up register button
        Button buttonRegister = findViewById(R.id.buttonRegister);
        buttonRegister.setOnClickListener(v -> registerUser());

        // Back button
        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        // set up the ui
        updateUI();
    }

    private void initializeViews() {
        passwordStrengthTextView = findViewById(R.id.passwordStrengthTextView);
        confirmPasswordMatchesTextView = findViewById(R.id.confirmPasswordMatchesTextView);

        hasMinLengthTextView = findViewById(R.id.hasMinLengthTextView);
        hasSpecialCharTextView = findViewById(R.id.hasSpecialCharTextView);
        hasUpperCaseTextView = findViewById(R.id.hasUpperCaseTextView);
        hasLowerCaseTextView = findViewById(R.id.hasLowerCaseTextView);
        hasAlphanumericTextView = findViewById(R.id.hasAlphanumericTextView);
        hasDigitTextView = findViewById(R.id.hasDigitTextView);

        editTextEmail = findViewById(R.id.editTextEmail);
        confirmValidEmailTextView = findViewById(R.id.confirmValidEmailTextView);

        editTextUsername = findViewById(R.id.editTextUsername);
        confirmValidUsernameTextView = findViewById(R.id.confirmValidUsernameTextView);

        editTextPassword = findViewById(R.id.editTextPassword);

        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        confirmPasswordMatchesTextView = findViewById(R.id.confirmPasswordMatchesTextView);
    }

    private void setUpListeners() {
        editTextEmail.addTextChangedListener(createTextWatcher());
        editTextUsername.addTextChangedListener(createTextWatcher());
        editTextPassword.addTextChangedListener(createPasswordTextWatcher());
        editTextConfirmPassword.addTextChangedListener(createTextWatcher());
    }

    private TextWatcher createTextWatcher() {
        return new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                updateUI();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                updateUI();
            }
        };
    }

    private TextWatcher createPasswordTextWatcher() {
        return new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                PasswordStrengthCalculationResult result = calculate(charSequence.toString());
                passwordStrength = result.strength();
                passwordStrengthCriteria = result.criteriaMet();

                updateUI();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                updateUI();
            }
        };
    }

    private void registerUser() {
        String email = editTextEmail.getText().toString().replace("\\s+", "");
        String username = editTextUsername.getText().toString().replace("\\s+", "");
        String password = editTextPassword.getText().toString().replace("\\s+", "");
        String confirmPassword = editTextConfirmPassword.getText().toString().replace("\\s+", "");

        if (!isValidEmail(email)) {
            Log.e(TAG, "Email is not valid");
            showToast("Invalid email");
            return;
        }

        if (!isValidUsername(username)) {
            Log.e(TAG,
                  "Username must be between " + USERNAME_MIN_LENGTH + " and " + USERNAME_MAX_LENGTH + " characters long");
            showToast("Username must be between " + USERNAME_MIN_LENGTH + " and " + USERNAME_MAX_LENGTH + " characters long");
            return;
        }

        if (!isValidPassword(password)) {
            Log.e(TAG, "Password is not valid");
            showToast("Password is not valid");
            return;
        }

        if (!password.equals(confirmPassword)) {
            Log.e(TAG, "Passwords do not match");
            showToast("Passwords do not match");
            return;
        }

        Consumer<UserProfileModel> onSuccess = userProfile -> {
            Log.d(TAG, "User registered successfully");
            showToast("User registered successfully");
            redirectToMyAccountActivity();
        };

        Consumer<ErrorHandle> onFailure = errorHandle -> {
            Log.e(TAG, "User registration failed due to an internal error: " + errorHandle);
            handleRegistrationFailure(errorHandle, email);
        };

        applicationGraph.usersService().createUser(email, username, password, onSuccess, onFailure);
    }

    private boolean isValidEmail(String email) {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidUsername(String username) {
        return username.length() >= USERNAME_MIN_LENGTH && username.length() <= USERNAME_MAX_LENGTH;
    }

    private boolean isValidPassword(@NonNull String password) {
        return password.length() >= MIN_LENGTH && !passwordStrengthCriteria.containsValue(false);
    }

    private void updateUI() {
        // update password strength text view
        passwordStrengthTextView.setTextColor(passwordStrength.color);
        String message = "Password strength: " + passwordStrength.name() + ".";
        passwordStrengthTextView.setText(message);
        passwordStrengthTextView.setTextColor(passwordStrength.color);

        // check email validity
        String email = editTextEmail.getText().toString().replace("\\s+", "");
        checkValidityAndShowMessage(confirmValidEmailTextView, !isValidEmail(email), "Invalid " +
                "email");

        // check username validity
        String username = editTextUsername.getText().toString().replace("\\s+", "");
        checkValidityAndShowMessage(confirmValidUsernameTextView, !isValidUsername(username),
                                    "Username must be between " + USERNAME_MIN_LENGTH + " and " + USERNAME_MAX_LENGTH + " characters long");

        // check password confirmation
        String password = editTextPassword.getText().toString().replace("\\s+", "");
        String confirmPassword = editTextConfirmPassword.getText().toString().replace("\\s+", "");
        checkValidityAndShowMessage(confirmPasswordMatchesTextView,
                                    !password.equals(confirmPassword), "Passwords do not match");

        // Update password criteria text views
        updatePasswordCriteriaTextViews();
    }

    private void updatePasswordCriteriaTextViews() {
        passwordStrengthCriteria.forEach((criteria, value) -> {
            TextView textView = getPasswordCriteriaTextView(criteria);
            String message = getPasswordCriteriaMessage(criteria, value);

            if (textView != null) {
                textView.setTextColor(value ? Color.GREEN : Color.RED);
                textView.setText(message);
            }
        });
    }

    private TextView getPasswordCriteriaTextView(PasswordStrengthCriteria criteria) {
        return switch (criteria) {
            case HAS_DIGIT -> hasDigitTextView;
            case HAS_ALPHANUMERIC -> hasAlphanumericTextView;
            case HAS_LOWER_CASE -> hasLowerCaseTextView;
            case HAS_UPPER_CASE -> hasUpperCaseTextView;
            case HAS_SPECIAL_CHAR -> hasSpecialCharTextView;
            case MEET_MIN_LENGTH -> hasMinLengthTextView;
        };
    }

    private String getPasswordCriteriaMessage(PasswordStrengthCriteria criteria, boolean value) {
        return switch (criteria) {
            case HAS_DIGIT -> value ? "Password must contain at least one digit." :
                    "Password contains at " + "least one digit.";
            case HAS_ALPHANUMERIC ->
                    value ? "Password must contain at least one alphanumeric character." :
                            "Password contains at least one alphanumeric character.";
            case HAS_LOWER_CASE ->
                    value ? "Password must contain at least one lower case letter." : "Password " +
                            "contains at least one lower case letter.";
            case HAS_UPPER_CASE ->
                    value ? "Password must contain at least one upper case letter." : "Password " +
                            "contains at least one upper case letter.";
            case HAS_SPECIAL_CHAR ->
                    value ? "Password must contain at least one special character." : "Password " +
                            "contains at least one special character.";
            case MEET_MIN_LENGTH ->
                    value ? "Password must be between 8 and 25 characters long." : "Password is " +
                            "between 8 and 25 characters long.";
        };
    }

    private void checkValidityAndShowMessage(TextView textView, boolean condition, String message) {
        if (condition) {
            textView.setText(message);
            textView.setVisibility(View.VISIBLE);
        } else {
            textView.setVisibility(View.INVISIBLE);
        }
    }

    private void redirectToMyAccountActivity() {
        Log.d(TAG, "Redirecting to MyAccountActivity");
        Intent intent = new Intent(RegistrationActivity.this, MyAccountActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void handleRegistrationFailure(ErrorHandle errorHandle, String email) {
        Log.e(TAG, "User registration failed due to an internal error: " + errorHandle);

        String errorMessage = getRegistrationErrorMessage(errorHandle, email);
        showToast(errorMessage);
    }

    private String getRegistrationErrorMessage(ErrorHandle errorHandle, String email) {
        if (errorHandle.errorType() == ErrorType.ENTITY_ALREADY_EXISTS) {
            return "User registration failed because a user already exists with the email: " + email;
        } else {
            return "User registration failed due to an unexpected internal error. Please try " +
                    "again later.";
        }
    }

    private void showToast(String message) {
        Toast.makeText(RegistrationActivity.this, message, Toast.LENGTH_LONG).show();
    }
}


/*
public class RegistrationActivity extends BaseActivity {

    public static final int USERNAME_MIN_LENGTH = 8;
    public static final int USERNAME_MAX_LENGTH = 25;

    private static final String TAG = "RegistrationActivity";

    private PasswordStrength passwordStrength;
    private Map<PasswordStrengthCriteria, Boolean> passwordStrengthCriteria;

    private EditText editTextEmail;
    private EditText editTextUsername;
    private EditText editTextPassword;
    private EditText editTextConfirmPassword;

    private TextView passwordStrengthTextView;
    private TextView confirmPasswordMatchesTextView;
    private TextView hasMinLengthTextView;
    private TextView hasSpecialCharTextView;
    private TextView hasUpperCaseTextView;
    private TextView hasLowerCaseTextView;
    private TextView hasAlphanumericTextView;
    private TextView hasDigitTextView;

    public RegistrationActivity() {
        super();
    }

    RegistrationActivity(ApplicationGraph applicationGraph) {
        super(applicationGraph);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // redirect if already logged in
        if (applicationGraph.usersService().isCurrentUserSignedIn()) {
            Log.d(TAG, "User is already signed in, redirecting to MyAccountActivity");
            Intent intent = new Intent(RegistrationActivity.this, MyAccountActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            return;
        }

        // set the layout for the activity
        setContentView(R.layout.activity_registration);

        // initialize password strength and criteria fields
        passwordStrength = WEAK;
        passwordStrengthCriteria = createCriteriaMap();

        // initialize password criteria text views
        passwordStrengthTextView = findViewById(R.id.passwordStrengthTextView);
        confirmPasswordMatchesTextView = findViewById(R.id.confirmPasswordMatchesTextView);
        hasMinLengthTextView = findViewById(R.id.hasMinLengthTextView);
        hasSpecialCharTextView = findViewById(R.id.hasSpecialCharTextView);
        hasUpperCaseTextView = findViewById(R.id.hasUpperCaseTextView);
        hasLowerCaseTextView = findViewById(R.id.hasLowerCaseTextView);
        hasAlphanumericTextView = findViewById(R.id.hasAlphanumericTextView);
        hasDigitTextView = findViewById(R.id.hasDigitTextView);

        // edit email
        editTextEmail = findViewById(R.id.editTextEmail);
        editTextEmail.setText("");
        editTextEmail.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                updateUI();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                updateUI();
            }
        });

        // edit username
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextUsername.setText("");
        editTextUsername.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                updateUI();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                updateUI();
            }
        });

        // edit password
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextPassword.setText("");
        editTextPassword.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                PasswordStrengthCalculationResult result = calculate(charSequence.toString());

                passwordStrength = result.strength();
                passwordStrengthCriteria = result.criteriaMet();

                passwordStrengthTextView.setTextColor(passwordStrength.color);
                String message = "Password strength: " + passwordStrength.name() + ".";
                passwordStrengthTextView.setText(message);

                updateUI();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                updateUI();
            }
        });

        // edit confirm password
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        editTextConfirmPassword.setText("");
        editTextConfirmPassword.addTextChangedListener(new TextWatcherAdapter() {
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                updateUI();
            }

            @Override
            public void afterTextChanged(Editable editable) {
                updateUI();
            }
        });

        // set up register button
        Button buttonRegister = findViewById(R.id.buttonRegister);
        buttonRegister.setOnClickListener(v -> registerUser());

        // back button
        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            Log.d(TAG, "Back button clicked");
            finish();
        });
    }

    private void registerUser() {
        String email = editTextEmail.getText().toString().replace("\\s+", "");
        String username = editTextUsername.getText().toString().replace("\\s+", "");
        String password = editTextPassword.getText().toString().replace("\\s+", "");
        String confirmPassword = editTextConfirmPassword.getText().toString().replace("\\s+", "");

        // validate email
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(RegistrationActivity.this, "Email is not valid",
                           Toast.LENGTH_LONG).show();
            return;
        }

        // validate username
        if (username.length() < USERNAME_MIN_LENGTH || username.length() > USERNAME_MAX_LENGTH) {
            Toast.makeText(RegistrationActivity.this, "Username must be between " +
                    USERNAME_MIN_LENGTH + " and " + USERNAME_MAX_LENGTH +
                    " characters long", Toast.LENGTH_LONG).show();
            return;
        }

        // validate password
        if (!isValidPassword(password)) {
            Toast.makeText(RegistrationActivity.this, "Password is not valid",
                           Toast.LENGTH_LONG).show();
            return;
        }

        // validate password matches confirm password
        if (!password.equals(confirmPassword)) {
            Toast.makeText(RegistrationActivity.this, "Passwords do not match",
                           Toast.LENGTH_LONG).show();
            return;
        }

        // to run if registration is successful
        Consumer<UserProfileModel> onSuccess = userProfile -> {
            Toast.makeText(RegistrationActivity.this, "User registered successfully",
                           Toast.LENGTH_LONG).show();

            Intent intent = new Intent(RegistrationActivity.this, MyAccountActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        };

        // to run if registration fails
        Consumer<ErrorHandle> onFailure = errorHandle -> {
            Log.e(TAG, "User registration failed due to an internal error: " + errorHandle);

            String message;
            if (errorHandle.errorType() == ErrorType.ENTITY_ALREADY_EXISTS) {
                message = "User registration failed because a user already exists with the " +
                        "email: " + email;
            } else {
                message = "User registration failed due to an unexpected internal error. " +
                        "Please try again later.";
            }

            Toast.makeText(RegistrationActivity.this, message, Toast.LENGTH_LONG).show();
        };

        applicationGraph.usersService().createUser(email, username, password, onSuccess, onFailure);
    }

    private boolean isValidPassword(@NonNull String password) {
        return password.length() >= MIN_LENGTH && !passwordStrengthCriteria.containsValue(false);
    }

    private void updateUI() {
        // check if email is valid
        String email = editTextEmail.getText().toString();
        email = email.replace("\\s+", "");

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            String message = "Invalid email";
            confirmPasswordMatchesTextView.setText(message);
            confirmPasswordMatchesTextView.setVisibility(View.VISIBLE);
        } else {
            confirmPasswordMatchesTextView.setVisibility(View.INVISIBLE);
        }

        // check if username is valid
        String username = editTextUsername.getText().toString().replace("\\s+", "");
        username = username.replace("\\s+", "");
        if (username.length() < USERNAME_MIN_LENGTH || username.length() > USERNAME_MAX_LENGTH) {
            String message = "Username must be between " + USERNAME_MIN_LENGTH + " and " +
                    USERNAME_MAX_LENGTH + " characters long";
            confirmPasswordMatchesTextView.setText(message);
            confirmPasswordMatchesTextView.setVisibility(View.VISIBLE);
        } else {
            confirmPasswordMatchesTextView.setVisibility(View.INVISIBLE);
        }

        // check if password matches confirm password
        String password = editTextPassword.getText().toString();
        password = password.replace("\\s+", "");
        String confirmPassword = editTextConfirmPassword.getText().toString();
        confirmPassword = confirmPassword.replace("\\s+", "");

        if (!password.equals(confirmPassword)) {
            String message = "Does not match password";
            confirmPasswordMatchesTextView.setText(message);
            confirmPasswordMatchesTextView.setVisibility(View.VISIBLE);
        } else {
            confirmPasswordMatchesTextView.setVisibility(View.INVISIBLE);
        }

        // password criteria text views
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
}

 */
