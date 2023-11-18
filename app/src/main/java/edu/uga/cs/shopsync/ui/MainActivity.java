package edu.uga.cs.shopsync.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.HashMap;
import java.util.Map;

import edu.uga.cs.shopsync.R;
import edu.uga.cs.shopsync.models.UserProfileModel;

public class MainActivity extends AppCompatActivity {

    private EditText loginEmailEditText;
    private EditText loginPasswordEditText;
    public static final Map<String, UserProfileModel> users = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button registerButton = findViewById(R.id.register_button);
        Button signInButton = findViewById(R.id.sign_in_button);
        registerButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegistrationActivity.class);
            startActivity(intent);
        });
        signInButton.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, SignInActivity.class);
            startActivity(intent);
        });
    }

    private void onSignInButtonClick() {
        String email = loginEmailEditText.getText().toString().trim();
        String password = loginPasswordEditText.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(MainActivity.this, "Email or password cannot be empty",
                           Toast.LENGTH_LONG)
                    .show();
            return;
        }

        // TODO:
        /*
        User user = users.get(email);
        if (user != null && user.getPassword().equals(password)) {
            Toast.makeText(MainActivity.this, "Login successful", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "Invalid email or password", Toast.LENGTH_SHORT)
                    .show();
        }
         */
    }

}
