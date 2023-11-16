package edu.uga.cs.shopsync;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract;
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * The main activity for the ShopSync app. This activity is the entry point for the app.
 * The user is presented with three buttons: sign in, register, and forgot password. The
 * sign in button starts the sign in process. The register button starts the registration
 * process. The forgot password button starts the forgot password process.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "ShopSync";

    // Launcher for the AuthUI sign in activity. In this activity, the user attempts to login.
    // If the user able to login, then they are taken to the home page activity. If the user
    // is not able to login, then they are presented with an error message and returned to
    // the main activity.
    private final ActivityResultLauncher<Intent> signInLauncher =
            registerForActivityResult(new FirebaseAuthUIActivityResultContract(),
                                      this::onSignInResult);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.d(TAG, "MainActivity.onCreate()");

        Button signInButton = findViewById(R.id.sign_in_button);
        Button registerButton = findViewById(R.id.register_button);

        signInButton.setOnClickListener(this::onSignInButtonClick);
        registerButton.setOnClickListener(this::onRegisterButtonClick);
    }

    /**
     * This method is called when the user clicks the register button. The register button
     * starts the registration process.
     *
     * @param v the view that was clicked
     */
    private void onRegisterButtonClick(View v) {
        // TODO: start registration activity
        Intent intent = new Intent(this, null);
        startActivity(intent);
    }

    /**
     * This method is called when the user clicks the sign in button. The sign in button
     * starts the sign in process.
     *
     * @param v the view that was clicked
     */
    private void onSignInButtonClick(View v) {
        List<AuthUI.IdpConfig> providers =
                Collections.singletonList(new AuthUI.IdpConfig.EmailBuilder().build());

        Log.d(TAG, "MainActivity.SignInButtonClickListener: Signing in started");

        Intent signInIntent = AuthUI.getInstance().createSignInIntentBuilder()
                .setAvailableProviders(providers).setTheme(R.style.LoginTheme).build();

        signInLauncher.launch(signInIntent);
    }

    /**
     * This method is called once the Firebase sign-in activity (launched above) returns
     * (completes). Then, the current (logged-in) Firebase user can be obtained.
     *
     * @param result the result of the sign in activity
     */
    private void onSignInResult(FirebaseAuthUIAuthenticationResult result) {
        if (result.getResultCode() == RESULT_OK) {
            Log.d(TAG,
                  "MainActivity.onSignInResult: Sign in succeeded with result: " +
                          Objects.requireNonNull(result.getIdpResponse()).getEmail());

            // TODO: Start the home page activity
            Intent intent = new Intent(this, null);
            startActivity(intent);
        } else {
            // Sign in failed
            Log.d(TAG, "MainActivity.onSignInResult: Sign in failed");

            Toast.makeText(this, "Sign in failed", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "MainActivity.onStart()");
        super.onStart();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "MainActivity.onResume()");
        super.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "MainActivity.onPause()");
        super.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "MainActivity.onStop()");
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "MainActivity.onDestroy()");
        super.onDestroy();
    }

    @Override
    protected void onRestart() {
        Log.d(TAG, "MainActivity.onRestart()");
        super.onRestart();
    }
}