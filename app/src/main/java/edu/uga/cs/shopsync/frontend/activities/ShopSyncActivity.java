package edu.uga.cs.shopsync.frontend.activities;

import static edu.uga.cs.shopsync.frontend.fragments.ShoppingItemsFragment.ACTION_ADD_SHOPPING_ITEM;
import static edu.uga.cs.shopsync.frontend.fragments.ShoppingItemsFragment.ACTION_DELETE_SHOPPING_ITEM;
import static edu.uga.cs.shopsync.frontend.fragments.ShoppingItemsFragment.ACTION_INITIALIZE_SHOPPING_ITEMS;
import static edu.uga.cs.shopsync.frontend.fragments.ShoppingItemsFragment.ACTION_MOVE_SHOPPING_ITEM_TO_BASKET;
import static edu.uga.cs.shopsync.frontend.fragments.ShoppingItemsFragment.ACTION_UPDATE_SHOPPING_ITEM;
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

import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import edu.uga.cs.shopsync.R;
import edu.uga.cs.shopsync.backend.exceptions.IllegalNullValueException;
import edu.uga.cs.shopsync.backend.exceptions.TaskFailureException;
import edu.uga.cs.shopsync.backend.models.BasketItemModel;
import edu.uga.cs.shopsync.backend.models.ShopSyncModel;
import edu.uga.cs.shopsync.backend.models.ShoppingItemModel;
import edu.uga.cs.shopsync.backend.models.UserProfileModel;
import edu.uga.cs.shopsync.frontend.Constants;
import edu.uga.cs.shopsync.frontend.fragments.BasketItemsFragment;
import edu.uga.cs.shopsync.frontend.fragments.PurchasedItemsFragment;
import edu.uga.cs.shopsync.frontend.fragments.ShoppingItemsFragment;
import edu.uga.cs.shopsync.frontend.fragments.ShoppingItemsFragment.ShoppingItemsAdapter;
import edu.uga.cs.shopsync.utils.CallbackReceiver;
import edu.uga.cs.shopsync.utils.ErrorHandle;
import edu.uga.cs.shopsync.utils.Props;

/**
 * Activity for displaying a shop sync.
 */
public class ShopSyncActivity extends BaseActivity implements CallbackReceiver {

    private static final String TAG = "ShopSyncActivity";
    private static final String SHOPPING_ITEMS_FRAGMENT = "ShoppingItemsFragment";
    private static final String BASKET_ITEMS_FRAGMENT = "BasketItemsFragment";
    private static final String PURCHASED_ITEMS_FRAGMENT = "PurchasedItemsFragment";

    private enum ItemsListType {
        SHOPPING_ITEMS_LIST,
        BASKET_ITEMS_LIST,
        PURCHASED_ITEMS_LIST
    }

    private final ChildEventListener shoppingItemsEventListener = new ChildEventListener() {
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
    };

    private TextView shopSyncNameTextView;
    private TextView descriptionTextView;
    private TableLayout usernamesTable;
    private Map<Integer, Button> itemTypeButtons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: called");
        super.onCreate(savedInstanceState);
        if (checkIfUserIsLoggedInAndFetch(true) == null) {
            return;
        }

        // TODO: implement landscape layout
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.activity_shop_sync_landscape);
        } else {
            setContentView(R.layout.activity_shop_sync);
        }

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
                .addChildEventListener(shoppingItemsEventListener);
    }

    @Override
    protected void onStop() {
        super.onStop();

        // fetch the shop sync id from the intent and populate the metadata
        String shopSyncId = getIntent().getStringExtra(Constants.SHOP_SYNC_UID);
        if (shopSyncId == null) {
            throw new IllegalStateException("ShopSync started without shop sync id");
        }

        // remove the child event listener for shopping items
        applicationGraph.shopSyncsService().getShopSyncsFirebaseReference()
                .getShoppingItemsCollection(shopSyncId)
                .removeEventListener(shoppingItemsEventListener);
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
            Log.d(TAG, "populateMetaData: shop sync description: " + descriptionText);
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

    @Override
    public void onConfigurationChanged(@NonNull Configuration config) {
        super.onConfigurationChanged(config);
        // TODO: implement landscape layout

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.activity_shop_sync_landscape);
        } else {
            setContentView(R.layout.activity_shop_sync);
        }

    }

    @Override
    public void onCallback(@NonNull String action, @Nullable Props props) {
        Log.d(TAG, "onCallback: called");

        String shopSyncUid = getIntent().getStringExtra(Constants.SHOP_SYNC_UID);
        if (shopSyncUid == null) {
            throw new IllegalNullValueException("ShopSync started without shop sync id");
        }

        Log.d(TAG, "onCallback: called with action " + action + " and props " + props);

        switch (action) {
            case ACTION_ADD_SHOPPING_ITEM -> addShoppingItem(shopSyncUid);
            case ACTION_UPDATE_SHOPPING_ITEM -> {
                if (props == null) {
                    throw new IllegalNullValueException("Props cannot be null for " + action);
                }
                updateShoppingItem(shopSyncUid, props);
            }
            case ACTION_INITIALIZE_SHOPPING_ITEMS -> {
                if (props == null) {
                    throw new IllegalNullValueException("Props cannot be null for " + action);
                }
                initializeShoppingItems(shopSyncUid, props);
            }
            case ACTION_MOVE_SHOPPING_ITEM_TO_BASKET -> {
                if (props == null) {
                    throw new IllegalNullValueException("Props cannot be null for " + action);
                }
                moveShoppingItemToBasket(shopSyncUid, props);
            }
            case ACTION_DELETE_SHOPPING_ITEM -> {
                if (props == null) {
                    throw new IllegalNullValueException("Props cannot be null for " + action);
                }
                deleteShoppingItem(shopSyncUid, props);
            }
        }
    }

    private void addShoppingItem(@NonNull String shopSyncUid) {
        Log.d(TAG, "addShoppingItem: adding shopping item");

        ShoppingItemModel shoppingItem =
                applicationGraph.shopSyncsService().addShoppingItem(shopSyncUid, "New " +
                        "Shopping Item", false);

        Log.d(TAG, "addShoppingItem: added shopping item: " + shoppingItem);
    }

    private void updateShoppingItem(@NonNull String shopSyncUid, @NonNull Props props) {
        Log.d(TAG, "updateShoppingItem: updating shopping item");

        ShoppingItemModel shoppingItem = props.get(Constants.SHOPPING_ITEM,
                                                   ShoppingItemModel.class);
        if (shoppingItem == null) {
            throw new IllegalNullValueException("Shopping item is null");
        }

        applicationGraph.shopSyncsService().updateShoppingItem(shopSyncUid, shoppingItem);
    }

    @SuppressWarnings("unchecked")
    @SuppressLint("NotifyDataSetChanged")
    private void initializeShoppingItems(@NonNull String shopSyncUid, @NonNull Props props) {
        Log.d(TAG, "initializeShoppingItems: initializing shopping items");

        // fetch the shopping items list from the props and populate it
        applicationGraph.shopSyncsService().getShoppingItemsWithShopSyncUid(shopSyncUid)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DataSnapshot dataSnapshot = task.getResult();
                        if (dataSnapshot == null) {
                            throw new IllegalNullValueException("DataSnapshot is null");
                        }

                        ShoppingItemsAdapter adapter = (ShoppingItemsAdapter) props.get(
                                Constants.ADAPTER);
                        if (adapter == null) {
                            Log.e(TAG, "populateShoppingItems: adapter is null");
                            throw new IllegalNullValueException("Adapter is null");
                        }

                        // fetch the shopping items list from the props
                        List<ShoppingItemModel> shoppingItems =
                                (List<ShoppingItemModel>) props.get(PROP_SHOPPING_ITEMS);
                        if (shoppingItems == null) {
                            Log.e(TAG, "populateShoppingItems: shopping items list is null");
                            throw new IllegalNullValueException("Shopping items list is null");
                        }
                        Log.d(TAG, "populateShoppingItems: shopping items list size: " +
                                shoppingItems.size());

                        // populate the shopping items list and notify the adapter
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            // fetch and add the shopping item
                            ShoppingItemModel shoppingItem =
                                    child.getValue(ShoppingItemModel.class);
                            if (shoppingItem == null) {
                                Log.e(TAG, "populateShoppingItems: no shopping item found with " +
                                        "id: " + child.getKey());
                                throw new IllegalNullValueException("No shopping item found with " +
                                                                            "id: " + child.getKey());
                            }
                            Log.d(TAG, "populateShoppingItems: shopping item: " + shoppingItem);
                            if (!shoppingItems.contains(shoppingItem)) {
                                shoppingItems.add(shoppingItem);
                            }
                        }

                        adapter.notifyDataSetChanged();
                    } else {
                        Log.e(TAG, "onCallback: failed to fetch shopping items",
                              task.getException());
                        throw new TaskFailureException(task, "Failed to fetch shopping items");
                    }
                });
    }

    private void moveShoppingItemToBasket(@NonNull String shopSyncUid, @NonNull Props props) {
        Log.d(TAG, "moveShoppingItemToBasket: moving shopping item to basket");

        // shopping basket uid is the same as the user's uid
        FirebaseUser user = checkIfUserIsLoggedInAndFetch(true);
        if (user == null) {
            return;
        }

        // fetch the shopping item from the props and add it to the basket
        ShoppingItemModel shoppingItem = props.get(Constants.SHOPPING_ITEM,
                                                   ShoppingItemModel.class);
        if (shoppingItem == null) {
            throw new IllegalNullValueException("Shopping item is null");
        }

        // on-success consumer
        Consumer<BasketItemModel> onSuccess = basketItem -> {
            Log.d(TAG, "moveShoppingItemToBasket: successfully added shopping item to basket");
        };

        // on-failure consumer
        Consumer<ErrorHandle> onFailure = error -> Log.e(TAG, "moveShoppingItemToBasket: " +
                "failed to add shopping item to basket due to error: " + error);

        applicationGraph.shopSyncsService().addBasketItem(
                shopSyncUid, user.getUid(), shoppingItem.getUid(), 1, 0.0,
                onSuccess, onFailure);
    }

    private void deleteShoppingItem(String shopSyncUid, Props props) {
        Log.d(TAG, "deleteShoppingItem: deleting shopping item");

        // fetch the shopping item from the props and delete it
        ShoppingItemModel shoppingItem = props.get(Constants.SHOPPING_ITEM,
                                                   ShoppingItemModel.class);
        if (shoppingItem == null) {
            throw new IllegalNullValueException("Shopping item is null");
        }

        applicationGraph.shopSyncsService()
                .deleteShoppingItem(shopSyncUid, shoppingItem.getUid())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "deleteShoppingItem: successfully deleted shopping item");
                    } else {
                        Log.e(TAG, "deleteShoppingItem: failed to delete shopping item",
                              task.getException());
                    }
                });
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

}

