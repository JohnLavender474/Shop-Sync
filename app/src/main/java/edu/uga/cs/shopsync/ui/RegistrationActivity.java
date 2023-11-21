package edu.uga.cs.shopsync.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.util.function.Consumer;

import edu.uga.cs.shopsync.ApplicationGraph;
import edu.uga.cs.shopsync.R;
import edu.uga.cs.shopsync.utils.ErrorHandle;
import edu.uga.cs.shopsync.utils.ErrorType;

public class RegistrationActivity extends BaseActivity {

    private static final String TAG = "RegistrationActivity";

    private EditText editTextEmail;
    private EditText editTextUsername;
    private EditText editTextPassword;
    private EditText editTextConfirmPassword;

    public RegistrationActivity() {
        super();
    }

    RegistrationActivity(ApplicationGraph applicationGraph) {
        super(applicationGraph);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);

        Button buttonRegister = findViewById(R.id.buttonRegister);
        buttonRegister.setOnClickListener(v -> registerUser());
    }

    private void registerUser() {
        String email = editTextEmail.getText().toString().trim();
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        if (!password.equals(confirmPassword)) {
            Toast.makeText(RegistrationActivity.this, "Passwords do not match",
                           Toast.LENGTH_LONG).show();
            return;
        }

        // to run if registration is successful
        Runnable onSuccess = () -> {
            Toast.makeText(RegistrationActivity.this, "User registered successfully",
                           Toast.LENGTH_LONG).show();

            Intent intent = new Intent(RegistrationActivity.this,
                                       MyAccountActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        };

        // to run if registration fails
        Consumer<ErrorHandle> onFailure = errorHandle -> {
            Log.e(TAG,
                  "User registration failed due to an internal error: " + errorHandle);

            String message;
            if (errorHandle.errorType() == ErrorType.USER_ALREADY_EXISTS) {
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
}
