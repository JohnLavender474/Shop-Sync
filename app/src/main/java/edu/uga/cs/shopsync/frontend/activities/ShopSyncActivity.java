package edu.uga.cs.shopsync.frontend.activities;

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
        setContentView(R.layout.activity_shop_sync);

        Button backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> {
            Log.d(TAG, "Back button clicked");
            finish();
        });

        shopSyncNameTextView = findViewById(R.id.textViewShopSyncName);
        descriptionTextView = findViewById(R.id.textViewDescription);
        usernamesTable = findViewById(R.id.tableLayoutUsernames);

        Button shoppingItemsButton = findViewById(R.id.buttonShoppingItems);
        Button basketItemsButton = findViewById(R.id.buttonBasketItems);
        Button purchasedItemsButton = findViewById(R.id.buttonPurchasedItems);

        itemTypeButtons = Map.of(
                R.id.buttonShoppingItems, shoppingItemsButton,
                R.id.buttonBasketItems, basketItemsButton,
                R.id.buttonPurchasedItems, purchasedItemsButton
        );

        itemTypeButtons.values()
                .forEach(button -> button.setOnClickListener(v -> handleItemsTypeButtonClick(button)));

        String shopSyncId = getIntent().getStringExtra(Constants.SHOP_SYNC_UID_EXTRA);
        if (shopSyncId == null) {
            throw new IllegalStateException("ShopSync started without shop sync id");
        }

        populateMetaData(shopSyncId);
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
        Log.d(TAG, "onFragmentCallback: called with props " + props);
    }

    private void handleItemsTypeButtonClick(Button button) {
        Log.d(TAG, "Items type button clicked: " + button.getText());

        itemTypeButtons.values().forEach(otherButton -> {
            otherButton.setEnabled(false);
            otherButton.setBackgroundColor(Color.GRAY);
            otherButton.setTextColor(Color.BLACK);
        });

        button.setEnabled(true);
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

