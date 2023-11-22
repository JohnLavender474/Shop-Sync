package edu.uga.cs.shopsync.frontend.activities;

import android.os.Bundle;
import android.widget.RadioGroup;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import edu.uga.cs.shopsync.R;
import edu.uga.cs.shopsync.frontend.fragments.BasketItemsFragment;
import edu.uga.cs.shopsync.frontend.fragments.PurchasedItemsFragment;
import edu.uga.cs.shopsync.frontend.fragments.ShoppingItemsFragment;

public class ShopSyncActivity extends BaseActivity {

    private enum ItemsListType {
        SHOPPING_ITEMS_LIST,
        BASKET_ITEMS_LIST,
        PURCHASED_ITEMS_LIST
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shop_sync);

        // set up radio group
        RadioGroup radioGroupItems = findViewById(R.id.radioGroupItems);
        radioGroupItems.setOnCheckedChangeListener((group, checkedId) -> {
            ItemsListType itemsListType;
            if (checkedId == R.id.radioButtonShoppingItems) {
                itemsListType = ItemsListType.SHOPPING_ITEMS_LIST;
            } else if (checkedId == R.id.radioButtonBasketItems) {
                itemsListType = ItemsListType.BASKET_ITEMS_LIST;
            } else if (checkedId == R.id.radioButtonPurchasedItems) {
                itemsListType = ItemsListType.PURCHASED_ITEMS_LIST;
            } else {
                throw new IllegalStateException("Unexpected value: " + checkedId);
            }

            updateFragments(itemsListType);
        });

        // set default fragment
        updateFragments(ItemsListType.SHOPPING_ITEMS_LIST);
    }

    private void updateFragments(ItemsListType itemsListType) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        // remove existing fragments if any
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        // set new fragment and commit
        switch (itemsListType) {
            case SHOPPING_ITEMS_LIST ->
                    transaction.replace(R.id.fragmentContainer, new ShoppingItemsFragment());
            case BASKET_ITEMS_LIST ->
                    transaction.replace(R.id.fragmentContainer, new BasketItemsFragment());
            case PURCHASED_ITEMS_LIST ->
                    transaction.replace(R.id.fragmentContainer, new PurchasedItemsFragment());
        }
        transaction.commit();
    }
}

