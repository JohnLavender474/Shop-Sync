package edu.uga.cs.shopsync.ui;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import edu.uga.cs.shopsync.R;

public class SignInActivity extends AppCompatActivity {

    private EditText editTextSignInEmail, editTextSignInPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        editTextSignInEmail = findViewById(R.id.editTextSignInEmail);
        editTextSignInPassword = findViewById(R.id.editTextSignInPassword);
        Button buttonSignIn = findViewById(R.id.buttonSignIn);

        buttonSignIn.setOnClickListener(v -> signInUser());
    }

    private void signInUser() {
        String email = editTextSignInEmail.getText().toString().trim();
        String password = editTextSignInPassword.getText().toString().trim();

        // Validate input and perform sign-in using your mock database (HashMap)
        // For example, check if the user exists in MainActivity.users HashMap
        // ...
    }
}