package edu.uga.cs.shopsync.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import edu.uga.cs.shopsync.ApplicationGraph;
import edu.uga.cs.shopsync.ApplicationGraphSingleton;
import edu.uga.cs.shopsync.R;
import edu.uga.cs.shopsync.services.UsersService;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // TODO: test application graph
        ApplicationGraph applicationGraph = ApplicationGraphSingleton.getInstance();
        UsersService usersService1 = applicationGraph.usersService();
        UsersService usersService2 = applicationGraph.usersService();
        Log.d(TAG,
              "Created two instances of UsersService: " + usersService1 + ", " + usersService2);

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
}
