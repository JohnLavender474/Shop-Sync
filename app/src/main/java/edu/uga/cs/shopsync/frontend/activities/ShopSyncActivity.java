package edu.uga.cs.shopsync.frontend.activities;

import android.os.Bundle;
import android.widget.RadioGroup;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import edu.uga.cs.shopsync.R;
import edu.uga.cs.shopsync.frontend.fragments.PurchasedItemsFragment;
import edu.uga.cs.shopsync.frontend.fragments.ShoppingItemsFragment;

public class ShopSyncActivity extends BaseActivity {

    private boolean showShoppingItems = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_sync);

        RadioGroup radioGroupItems = findViewById(R.id.radioGroupItems);
        radioGroupItems.setOnCheckedChangeListener((group, checkedId) -> {
            showShoppingItems = checkedId == R.id.radioButtonShoppingItems;
            updateFragments();
        });

        updateFragments();
    }

    private void updateFragments() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // remove existing fragments if any
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        if (showShoppingItems) {
            // show the shopping items fragment
            transaction.replace(R.id.fragmentContainer, new ShoppingItemsFragment());
        } else {
            // show the purchased items fragment
            transaction.replace(R.id.fragmentContainer, new PurchasedItemsFragment());
        }

        transaction.commit();
    }
}

