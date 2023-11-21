package edu.uga.cs.shopsync.frontend.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import edu.uga.cs.shopsync.ApplicationGraph;
import edu.uga.cs.shopsync.R;

public class MainActivity extends BaseActivity {

    private static final String TAG = "MainActivity";

    public MainActivity() {
        super();
    }

    MainActivity(ApplicationGraph applicationGraph) {
        super(applicationGraph);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (applicationGraph.usersService().isCurrentUserSignedIn()) {
            Log.d(TAG, "onCreate: user already signed in, redirecting to my account activity");

            Intent intent = new Intent(this, MyAccountActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);

            return;
        }

        Button registerButton = findViewById(R.id.register_button);
        registerButton.setOnClickListener(v -> {
            Log.d(TAG, "onCreate: register button clicked, redirecting to registration activity");

            Intent intent = new Intent(MainActivity.this, RegistrationActivity.class);
            startActivity(intent);
        });

        Button signInButton = findViewById(R.id.sign_in_button);
        signInButton.setOnClickListener(v -> {
            Log.d(TAG, "onCreate: sign in button clicked, redirecting to sign in activity");

            Intent intent = new Intent(MainActivity.this, SignInActivity.class);
            startActivity(intent);
        });
    }
}
