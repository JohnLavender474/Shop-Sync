package edu.uga.cs.shopsync.ui;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import edu.uga.cs.shopsync.ApplicationGraph;
import edu.uga.cs.shopsync.ApplicationGraphSingleton;
import edu.uga.cs.shopsync.R;

public class BaseActivity extends AppCompatActivity {

    protected final ApplicationGraph applicationGraph;

    public BaseActivity() {
        applicationGraph = ApplicationGraphSingleton.getInstance();
    }

    BaseActivity(ApplicationGraph applicationGraph) {
        this.applicationGraph = applicationGraph;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.my_account) {
            Class<? extends BaseActivity> clazz;
            if (applicationGraph.usersService().isCurrentUserSignedIn()) {
                clazz = MyAccountActivity.class;
            } else {
                clazz = SignInActivity.class;
            }
            Intent intent = new Intent(this, clazz);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
