package edu.uga.cs.shopsync.frontend.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.ActionBar;

import edu.uga.cs.shopsync.R;

/**
 * Activity for creating a new shop sync.
 */
public class CreateShopSyncActivity extends BaseActivity {

    private static final String TAG = "CreateShopSyncActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // set the content view
        setContentView(R.layout.activity_create_shop_sync);

        // set the action bar
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // set the title
        setTitle("Create ShopSync");

        // set the button click listeners
        Button createShopSyncButton = findViewById(R.id.buttonCreateShopSync);
        createShopSyncButton.setOnClickListener(this::onCreateShopSyncButtonClick);
    }

    /**
     * Handles the create shop sync button click event.
     *
     * @param view The view that was clicked.
     */
    private void onCreateShopSyncButtonClick(View view) {

    }

}
