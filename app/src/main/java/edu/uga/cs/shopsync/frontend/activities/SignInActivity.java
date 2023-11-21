package edu.uga.cs.shopsync.frontend.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import edu.uga.cs.shopsync.ApplicationGraph;
import edu.uga.cs.shopsync.ApplicationGraphSingleton;
import edu.uga.cs.shopsync.R;

public class SignInActivity extends BaseActivity {

    private static final String TAG = "SignInActivity";

    private EditText editTextSignInEmail;
    private EditText editTextSignInPassword;

    /**
     * Default constructor. Uses the singleton instance of the application graph for service
     * dependencies. This should be used in production.
     *
     * @see ApplicationGraphSingleton
     */
    public SignInActivity() {
        super();
    }

    /**
     * Package-private constructor for testing only.
     *
     * @param applicationGraph the application graph
     */
    SignInActivity(ApplicationGraph applicationGraph) {
        super(applicationGraph);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (applicationGraph.usersService().isCurrentUserSignedIn()) {
            Log.d(TAG, "onCreate: user already signed in");

            Intent intent = new Intent(this, MyAccountActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            return;
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        editTextSignInEmail = findViewById(R.id.editTextSignInEmail);
        editTextSignInPassword = findViewById(R.id.editTextSignInPassword);

        Button buttonSignIn = findViewById(R.id.buttonSignIn);
        buttonSignIn.setOnClickListener(v -> onSignInButtonClick());
    }

    private void onSignInButtonClick() {
        String email = editTextSignInEmail.getText().toString();
        String password = editTextSignInPassword.getText().toString();

        applicationGraph
                .usersService()
                .signInUser(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "signInUser: success");
                        Toast.makeText(getApplicationContext(), "Signed in user: " + email,
                                       Toast.LENGTH_SHORT).show();

                        Intent intent = new Intent(SignInActivity.this, MyAccountActivity.class);
                        startActivity(intent);
                    } else {
                        Log.d(TAG, "signInUser: failure");
                        Toast.makeText(getApplicationContext(), "Sign in failed!",
                                       Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
