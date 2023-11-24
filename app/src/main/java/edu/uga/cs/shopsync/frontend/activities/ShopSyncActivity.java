package edu.uga.cs.shopsync.frontend.activities;

import static edu.uga.cs.shopsync.frontend.fragments.ShoppingItemsFragment.*;
import static edu.uga.cs.shopsync.frontend.fragments.ShoppingItemsFragment.ACTION_INITIALIZE_SHOPPING_ITEMS;
import static edu.uga.cs.shopsync.frontend.fragments.ShoppingItemsFragment.PROP_SHOPPING_ITEMS;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.database.DataSnapshot;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import edu.uga.cs.shopsync.R;
import edu.uga.cs.shopsync.backend.exceptions.IllegalNullValueException;
import edu.uga.cs.shopsync.backend.exceptions.TaskFailureException;
import edu.uga.cs.shopsync.backend.models.ShopSyncModel;
import edu.uga.cs.shopsync.backend.models.ShoppingItemModel;
import edu.uga.cs.shopsync.backend.models.UserProfileModel;
import edu.uga.cs.shopsync.frontend.Constants;
import edu.uga.cs.shopsync.frontend.activities.contracts.FragmentCallbackReceiver;
import edu.uga.cs.shopsync.frontend.fragments.BasketItemsFragment;
import edu.uga.cs.shopsync.frontend.fragments.PurchasedItemsFragment;
import edu.uga.cs.shopsync.frontend.fragments.ShoppingItemsFragment;
import edu.uga.cs.shopsync.utils.Props;

/**
 * Activity for displaying a shop sync.
 */
public class ShopSyncActivity extends BaseActivity implements FragmentCallbackReceiver {

    private static final String TAG = "ShopSync";

    private enum ItemsListType {
        SHOPPING_ITEMS_LIST,
        BASKET_ITEMS_LIST,
        PURCHASED_ITEMS_LIST
    }

    private TextView shopSyncNameTextView;
    private TextView descriptionTextView;
    private TableLayout usernamesTable;
    private Map<Integer, Button> itemTypeButtons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: called");
        super.onCreate(savedInstanceState);
        checkIfUserIsLoggedInAndFetch(true);

        // TODO: implement landscape layout
        /*
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.activity_shop_sync_landscape);
        } else {
            setContentView(R.layout.activity_shop_sync);
        }
        */
        // set the content view
        setContentView(R.layout.activity_shop_sync);

        // set up the back button
        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            Log.d(TAG, "Back button clicked");
            finish();
        });

        // set up the views for displaying the shop sync metadata
        shopSyncNameTextView = findViewById(R.id.textViewShopSyncName);
        descriptionTextView = findViewById(R.id.textViewDescription);
        usernamesTable = findViewById(R.id.tableLayoutUsernames);

        // set up the buttons for switching between the different items lists
        Button shoppingItemsButton = findViewById(R.id.buttonShoppingItems);
        Button basketItemsButton = findViewById(R.id.buttonBasketItems);
        Button purchasedItemsButton = findViewById(R.id.buttonPurchasedItems);
        itemTypeButtons = Map.of(
                R.id.buttonShoppingItems, shoppingItemsButton,
                R.id.buttonBasketItems, basketItemsButton,
                R.id.buttonPurchasedItems, purchasedItemsButton
        );
        itemTypeButtons.values()
                .forEach(button -> button.setOnClickListener(v -> handleItemsTypeChange(button)));
        // set the shopping items button as the default selected button
        handleItemsTypeChange(shoppingItemsButton);

        // fetch the shop sync id from the intent and populate the metadata
        String shopSyncId = getIntent().getStringExtra(Constants.SHOP_SYNC_UID);
        if (shopSyncId == null) {
            throw new IllegalStateException("ShopSync started without shop sync id");
        }
        populateMetaData(shopSyncId);

        // update the fragments to display the shopping items list
        updateFragments(ItemsListType.SHOPPING_ITEMS_LIST);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration config) {
        super.onConfigurationChanged(config);
        // TODO: implement landscape layout
        /*
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.activity_shop_sync_landscape);
        } else {
            setContentView(R.layout.activity_shop_sync);
        }
         */
    }

    @Override
    public void onFragmentCallback(String action, Props props) {
        Log.d(TAG, "onFragmentCallback: called");

        String shopSyncUid = getIntent().getStringExtra(Constants.SHOP_SYNC_UID);
        if (shopSyncUid == null) {
            throw new IllegalNullValueException("ShopSync started without shop sync id");
        }

        Log.d(TAG, "onFragmentCallback: called with action " + action + " and props " + props);

        switch (action) {
            case ACTION_INITIALIZE_SHOPPING_ITEMS -> initializeShoppingItems(shopSyncUid, props);
        }
    }

    private void initializeShoppingItems(String shopSyncUid, Props props) {
        Log.d(TAG, "initializeShoppingItems: initializing shopping items");

        // fetch the shopping items list from the props and populate it
        applicationGraph.shopSyncsService().getShoppingItemsWithShopSyncUid(shopSyncUid)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DataSnapshot dataSnapshot = task.getResult();
                        if (dataSnapshot == null) {
                            throw new IllegalNullValueException("DataSnapshot is null");
                        }

                        populateShoppingItems(dataSnapshot, props);
                    } else {
                        Log.e(TAG, "onFragmentCallback: failed to fetch shopping items",
                              task.getException());
                        throw new TaskFailureException(task, "Failed to fetch shopping items");
                    }
                });
    }

    @SuppressWarnings("unchecked")
    @SuppressLint("NotifyDataSetChanged")
    private void populateShoppingItems(DataSnapshot data, Props props) {
        // fetch the shopping items list from the props
        List<ShoppingItemModel> shoppingItems =
                (List<ShoppingItemModel>) props.get(PROP_SHOPPING_ITEMS);
        if (shoppingItems == null) {
            Log.e(TAG, "populateShoppingItems: shopping items list is null");
            throw new IllegalNullValueException("Shopping items list is null");
        }
        Log.d(TAG, "populateShoppingItems: shopping items list size: " + shoppingItems.size());

        // fetch the RecyclerView from the props
        ShoppingItemsAdapter adapter =
                (ShoppingItemsAdapter) props.get(Constants.RECYCLER_VIEW_ADAPTER);
        if (adapter == null) {
            Log.e(TAG, "populateShoppingItems: Shopping items adapter is null");
            throw new IllegalNullValueException("RecyclerView is null");
        }
        Log.d(TAG, "populateShoppingItems: Adapter " + adapter);

        // populate the shopping items list and notify the adapter
        for (DataSnapshot child : data.getChildren()) {
            // fetch and add the shopping item
            ShoppingItemModel shoppingItem = child.getValue(ShoppingItemModel.class);
            if (shoppingItem == null) {
                Log.e(TAG,
                      "populateShoppingItems: no shopping item found with id: " + child.getKey());
                throw new IllegalNullValueException("No shopping item found with id: " + child.getKey());
            }
            shoppingItems.add(shoppingItem);

            // notify the adapter
            adapter.notifyDataSetChanged();
        }
    }

    private void handleItemsTypeChange(Button button) {
        Log.d(TAG, "Items type button clicked: " + button.getText());

        itemTypeButtons.values().forEach(otherButton -> {
            otherButton.setBackgroundColor(Color.GRAY);
            otherButton.setTextColor(Color.BLACK);
        });

        button.setBackgroundColor(Color.GREEN);
        button.setTextColor(Color.WHITE);

        int checkedId = button.getId();
        ItemsListType itemsListType;
        if (checkedId == R.id.buttonShoppingItems) {
            itemsListType = ItemsListType.SHOPPING_ITEMS_LIST;
        } else if (checkedId == R.id.buttonBasketItems) {
            itemsListType = ItemsListType.BASKET_ITEMS_LIST;
        } else if (checkedId == R.id.buttonPurchasedItems) {
            itemsListType = ItemsListType.PURCHASED_ITEMS_LIST;
        } else {
            throw new IllegalStateException("Unexpected checked id value: " + checkedId);
        }

        updateFragments(itemsListType);
    }

    private void populateMetaData(String shopSyncId) {
        applicationGraph.shopSyncsService().getShopSyncWithUid(shopSyncId)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DataSnapshot dataSnapshot = task.getResult();
                        if (dataSnapshot == null) {
                            Log.e(TAG, "populateMetaData: DataSnapshot is null");
                            throw new IllegalNullValueException("DataSnapshot is null");
                        }

                        ShopSyncModel shopSync = dataSnapshot.getValue(ShopSyncModel.class);
                        if (shopSync == null) {
                            Log.e(TAG,
                                  "populateMetaData: no shop sync found with id: " + shopSyncId);
                            throw new IllegalNullValueException("No shop sync found with id: " + shopSyncId);
                        }

                        shopSyncNameTextView.setText(shopSync.getName());
                        Log.d(TAG, "populateMetaData: shop sync name: " + shopSync.getName());

                        String description = shopSync.getDescription();
                        if (description == null || description.isBlank()) {
                            Log.d(TAG, "populateMetaData: no description found for shop sync: "
                                    + shopSyncId);
                            descriptionTextView.setVisibility(View.INVISIBLE);
                        } else {
                            String descriptionText = "Description: " + shopSync.getDescription();
                            descriptionTextView.setText(descriptionText);
                            descriptionTextView.setVisibility(View.VISIBLE);
                            Log.d(TAG,
                                  "populateMetaData: shop sync description: " + descriptionText);
                        }

                        Consumer<List<String>> userUidsConsumer = userUids ->
                                userUids.forEach(userUid -> applicationGraph.usersService()
                                        .getUserProfileWithUid(userUid)
                                        .addOnSuccessListener(data -> handleUserProfileData(data,
                                                                                            userUid)));

                        applicationGraph.shopSyncsService()
                                .getUsersForShopSync(shopSyncId, userUidsConsumer, null);
                    } else {
                        throw new TaskFailureException(task, "Failed to fetch shop sync with id: "
                                + shopSyncId);
                    }
                });
    }

    private void handleUserProfileData(DataSnapshot data, String userUid) {
        Log.d(TAG,
              "handleUserProfileData: handling user profile data for user with id: " + userUid);

        if (data == null) {
            Log.e(TAG, "handleUserProfileData: DataSnapshot is null for user with id: " + userUid);
            throw new IllegalNullValueException("DataSnapshot is null for user with id: " + userUid);
        }

        UserProfileModel userProfile = data.getValue(UserProfileModel.class);
        if (userProfile == null) {
            Log.e(TAG, "handleUserProfileData: no user profile found with id: " + userUid);
            throw new IllegalNullValueException("No user profile found with id: " + userUid);
        }
        Log.d(TAG, "handleUserProfileData: user profile: " + userProfile);

        TextView usernameTextView = new TextView(this);
        String message = "Member: " + userProfile.getUsername();
        usernameTextView.setText(message);
        TableRow tableRow = new TableRow(this);
        tableRow.addView(usernameTextView);
        usernamesTable.addView(tableRow);
    }

    private void updateFragments(ItemsListType itemsListType) {
        Log.d(TAG, "Updating fragments to display " + itemsListType + " items");

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

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

