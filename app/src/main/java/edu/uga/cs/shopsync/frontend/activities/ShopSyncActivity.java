package edu.uga.cs.shopsync.frontend.activities;

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
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

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
import edu.uga.cs.shopsync.frontend.fragments.BasketItemsFragment;
import edu.uga.cs.shopsync.frontend.fragments.PurchasedItemsFragment;
import edu.uga.cs.shopsync.frontend.fragments.ShoppingItemsFragment;
import edu.uga.cs.shopsync.frontend.fragments.ShoppingItemsFragment.ShoppingItemsAdapter;
import edu.uga.cs.shopsync.utils.CallbackReceiver;
import edu.uga.cs.shopsync.utils.Props;

/**
 * Activity for displaying a shop sync.
 */
public class ShopSyncActivity extends BaseActivity implements CallbackReceiver {

    private static final String TAG = "ShopSync";
    private static final String SHOPPING_ITEMS_FRAGMENT = "ShoppingItemsFragment";
    private static final String BASKET_ITEMS_FRAGMENT = "BasketItemsFragment";
    private static final String PURCHASED_ITEMS_FRAGMENT = "PurchasedItemsFragment";

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

        // fetch the shop sync from the database
        applicationGraph.shopSyncsService().getShopSyncWithUid(shopSyncId)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DataSnapshot dataSnapshot = task.getResult();
                        if (dataSnapshot == null) {
                            Log.e(TAG, "onCreate: DataSnapshot is null");
                            throw new IllegalNullValueException("DataSnapshot is null");
                        }

                        ShopSyncModel shopSync = dataSnapshot.getValue(ShopSyncModel.class);
                        if (shopSync == null) {
                            Log.e(TAG, "onCreate: no shop sync found with id: " + shopSyncId);
                            throw new IllegalNullValueException("No shop sync found with id: " + shopSyncId);
                        }

                        Log.d(TAG, "onCreate: shop sync: " + shopSync);

                        // populate the metadata
                        populateMetaData(shopSync);
                    } else {
                        Log.e(TAG, "onCreate: failed to fetch shop sync with id: " + shopSyncId,
                              task.getException());
                        throw new TaskFailureException(task, "Failed to fetch shop sync with id: "
                                + shopSyncId);
                    }
                });

        // add child event listener for shopping items
        applicationGraph.shopSyncsService().getShopSyncsFirebaseReference()
                .getShoppingItemsCollection(shopSyncId)
                .addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(@NonNull DataSnapshot snapshot,
                                             @Nullable String previousChildName) {
                        if (getCurrentFragment() instanceof ShoppingItemsFragment fragment) {
                            fragment.onChildAdded(snapshot, previousChildName);
                        }
                    }

                    @Override
                    public void onChildChanged(@NonNull DataSnapshot snapshot,
                                               @Nullable String previousChildName) {
                        if (getCurrentFragment() instanceof ShoppingItemsFragment fragment) {
                            fragment.onChildChanged(snapshot, previousChildName);
                        }
                    }

                    @Override
                    public void onChildRemoved(@NonNull DataSnapshot snapshot) {
                        if (getCurrentFragment() instanceof ShoppingItemsFragment fragment) {
                            fragment.onChildRemoved(snapshot);
                        }
                    }

                    @Override
                    public void onChildMoved(@NonNull DataSnapshot snapshot,
                                             @Nullable String previousChildName) {
                        if (getCurrentFragment() instanceof ShoppingItemsFragment fragment) {
                            fragment.onChildMoved(snapshot, previousChildName);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        if (getCurrentFragment() instanceof ShoppingItemsFragment fragment) {
                            fragment.onCancelled(error);
                        }
                    }
                });

        // update the fragments to display the shopping items list
        setFragment(ItemsListType.SHOPPING_ITEMS_LIST);
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
    public void onCallback(String action, Props props) {
        Log.d(TAG, "onCallback: called");

        String shopSyncUid = getIntent().getStringExtra(Constants.SHOP_SYNC_UID);
        if (shopSyncUid == null) {
            throw new IllegalNullValueException("ShopSync started without shop sync id");
        }

        Log.d(TAG, "onCallback: called with action " + action + " and props " + props);

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
                        Log.e(TAG, "onCallback: failed to fetch shopping items",
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

        // fetch the adapter from the props
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

        setFragment(itemsListType);
    }

    private void populateMetaData(ShopSyncModel shopSync) {
        shopSyncNameTextView.setText(shopSync.getName());
        Log.d(TAG, "populateMetaData: shop sync name: " + shopSync.getName());

        String description = shopSync.getDescription();
        if (description == null || description.isBlank()) {
            Log.d(TAG, "populateMetaData: no description found for shop sync: " + shopSync);
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
                        .addOnSuccessListener(data -> handleUserProfileData(
                                data, userUid)));

        applicationGraph.shopSyncsService()
                .getUsersForShopSync(shopSync.getUid(), userUidsConsumer, null);
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

    private void setFragment(ItemsListType itemsListType) {
        Log.d(TAG, "Updating fragments to display " + itemsListType + " items");

        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);

        switch (itemsListType) {
            case SHOPPING_ITEMS_LIST ->
                    transaction.replace(R.id.fragmentContainer, new ShoppingItemsFragment(),
                                        SHOPPING_ITEMS_FRAGMENT);
            case BASKET_ITEMS_LIST ->
                    transaction.replace(R.id.fragmentContainer, new BasketItemsFragment(),
                                        BASKET_ITEMS_FRAGMENT);
            case PURCHASED_ITEMS_LIST ->
                    transaction.replace(R.id.fragmentContainer, new PurchasedItemsFragment(),
                                        PURCHASED_ITEMS_FRAGMENT);
        }
        transaction.commit();
    }

    private Fragment getCurrentFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        return fragmentManager.findFragmentById(R.id.fragmentContainer);
    }

}

