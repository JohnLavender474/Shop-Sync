package edu.uga.cs.shopsync.frontend.activities;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.auth.FirebaseAuth;

import edu.uga.cs.shopsync.R;

public class ForgotPasswordActivity extends BaseActivity {

    private EditText editTextForgotPasswordEmail;
    private TextView textViewForgotPasswordError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        editTextForgotPasswordEmail = findViewById(R.id.editTextForgotPasswordEmail);
        textViewForgotPasswordError = findViewById(R.id.textViewForgotPasswordError);

        Button buttonResetPassword = findViewById(R.id.buttonResetPassword);
        buttonResetPassword.setOnClickListener(v -> resetPassword());

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());
    }

    private void resetPassword() {
        String email = editTextForgotPasswordEmail.getText().toString().trim();

        // Validate email
        if (email.isEmpty()) {
            textViewForgotPasswordError.setText("Email is required");
            editTextForgotPasswordEmail.requestFocus();
            return;
        }

        FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(ForgotPasswordActivity.this, "Password reset link sent to email if email is associated with an account.", Toast.LENGTH_LONG).show();
                    } else {
                        textViewForgotPasswordError.setText("Failed to send reset email");
                    }
                });
    }
}