package edu.uga.cs.shopsync.frontend.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import edu.uga.cs.shopsync.ApplicationGraph;
import edu.uga.cs.shopsync.R;
import edu.uga.cs.shopsync.TemporaryStuff;

/**
 * The main activity for the application. This activity is the first activity that is displayed
 * when the user is not signed in and the application is launched. This activity provides the
 * options to register a new user, sign in as an existing user, or change the user's password.
 */
public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";

    /**
     * Default constructor for MainActivity. This constructor uses the singleton instance of the
     * application graph. This constructor should only be used by the Android framework.
     */
    public MainActivity() {
        super();
        // TODO: remove after testing
        TemporaryStuff.testAddNewUser(applicationGraph);
        // TemporaryStuff.testAddShoppingItemToShoppingBasket(applicationGraph);
    }

    /**
     * Constructor for MainActivity. This constructor should be used for testing purposes only.
     *
     * @param applicationGraph The application graph to use for this activity.
     */
    MainActivity(ApplicationGraph applicationGraph) {
        super(applicationGraph);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // if the user is already signed in, then redirect to the my account activity
        if (applicationGraph.usersService().isCurrentUserSignedIn()) {
            Log.d(TAG, "onCreate: user already signed in, redirecting to my account activity");

            Intent intent = new Intent(this, MyAccountActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            return;
        }

        // set up the register button
        Button registerButton = findViewById(R.id.register_button);
        registerButton.setOnClickListener(v -> {
            Log.d(TAG, "onCreate: register button clicked, redirecting to registration activity");
            Intent intent = new Intent(MainActivity.this, RegistrationActivity.class);
            startActivity(intent);
        });

        // set up the sign in button
        Button signInButton = findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(v -> {
            Log.d(TAG, "onCreate: sign in button clicked, redirecting to sign in activity");
            Intent intent = new Intent(MainActivity.this, SignInActivity.class);
            startActivity(intent);
        });

        // set up forgot password button
        Button forgotPassword = findViewById(R.id.forgot_password_button);
        forgotPassword.setOnClickListener(v -> {
            Toast.makeText(getApplicationContext(), "Forgot password functionality not set up yet",
                           Toast.LENGTH_SHORT).show();

            // TODO:
            /*
            Log.d(TAG, "onCreate: forgot password button clicked, redirecting to forgot password
            activity");
            Intent intent = new Intent(MainActivity.this, ForgotPasswordActivity.class);
            startActivity(intent);
             */
        });
    }
}
