package edu.uga.cs.shopsync.frontend.activities;

import static edu.uga.cs.shopsync.frontend.fragments.BasketItemsFragment.ACTION_FETCH_ITEM_NAME_BY_SHOPPING_ITEM_UID;
import static edu.uga.cs.shopsync.frontend.fragments.BasketItemsFragment.ACTION_INITIALIZE_BASKET_ITEMS;
import static edu.uga.cs.shopsync.frontend.fragments.BasketItemsFragment.ACTION_PURCHASE_BASKET_ITEM;
import static edu.uga.cs.shopsync.frontend.fragments.BasketItemsFragment.ACTION_REMOVE_BASKET_ITEM;
import static edu.uga.cs.shopsync.frontend.fragments.BasketItemsFragment.ACTION_UPDATE_BASKET_ITEM;
import static edu.uga.cs.shopsync.frontend.fragments.BasketItemsFragment.PROP_BASKET_ITEMS;
import static edu.uga.cs.shopsync.frontend.fragments.PurchasedItemsFragment.ACTION_DELETE_PURCHASE;
import static edu.uga.cs.shopsync.frontend.fragments.PurchasedItemsFragment.ACTION_UNDO_PURCHASE;
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
import com.google.firebase.database.DatabaseReference;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import edu.uga.cs.shopsync.R;
import edu.uga.cs.shopsync.backend.exceptions.IllegalNullValueException;
import edu.uga.cs.shopsync.backend.exceptions.TaskFailureException;
import edu.uga.cs.shopsync.backend.models.BasketItemModel;
import edu.uga.cs.shopsync.backend.models.PurchasedItemModel;
import edu.uga.cs.shopsync.backend.models.ShopSyncModel;
import edu.uga.cs.shopsync.backend.models.ShoppingBasketModel;
import edu.uga.cs.shopsync.backend.models.ShoppingItemModel;
import edu.uga.cs.shopsync.backend.models.UserProfileModel;
import edu.uga.cs.shopsync.frontend.Constants;
import edu.uga.cs.shopsync.frontend.fragments.BasketItemsFragment;
import edu.uga.cs.shopsync.frontend.fragments.BasketItemsFragment.BasketItemsAdapter;
import edu.uga.cs.shopsync.frontend.fragments.PurchasedItemsFragment;
import edu.uga.cs.shopsync.frontend.fragments.ShoppingItemsFragment;
import edu.uga.cs.shopsync.frontend.fragments.ShoppingItemsFragment.ShoppingItemsAdapter;
import edu.uga.cs.shopsync.frontend.utils.ChildEventListenerFragment;
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
    private static final int DEFAULT_QUANTITY = 1;
    private static final double DEFAULT_PRICE_PER_UNIT = 1.0;

    private enum ItemsListType {
        SHOPPING_ITEMS_LIST, BASKET_ITEMS_LIST, PURCHASED_ITEMS_LIST
    }

    private final class FragmentChildEventListener implements ChildEventListener {

        private final Class<?> fragmentClass;

        FragmentChildEventListener(Class<? extends ChildEventListenerFragment> fragmentClass) {
            this.fragmentClass = fragmentClass;
        }

        @Override
        public void onChildAdded(@NonNull DataSnapshot snapshot,
                                 @Nullable String previousChildName) {
            ChildEventListenerFragment fragment = getCurrentChildEventListenerFragment();
            if (fragmentClass.isAssignableFrom(fragment.getClass())) {
                fragment.onChildAdded(snapshot, previousChildName);
            }
        }

        @Override
        public void onChildChanged(@NonNull DataSnapshot snapshot,
                                   @Nullable String previousChildName) {
            ChildEventListenerFragment fragment = getCurrentChildEventListenerFragment();
            if (fragmentClass.isAssignableFrom(fragment.getClass())) {
                fragment.onChildChanged(snapshot, previousChildName);
            }
        }

        @Override
        public void onChildRemoved(@NonNull DataSnapshot snapshot) {
            ChildEventListenerFragment fragment = getCurrentChildEventListenerFragment();
            if (fragmentClass.isAssignableFrom(fragment.getClass())) {
                fragment.onChildRemoved(snapshot);
            }
        }

        @Override
        public void onChildMoved(@NonNull DataSnapshot snapshot,
                                 @Nullable String previousChildName) {
            ChildEventListenerFragment fragment = getCurrentChildEventListenerFragment();
            if (fragmentClass.isAssignableFrom(fragment.getClass())) {
                fragment.onChildMoved(snapshot, previousChildName);
            }
        }

        @Override
        public void onCancelled(@NonNull DatabaseError error) {
            ChildEventListenerFragment fragment = getCurrentChildEventListenerFragment();
            if (fragmentClass.isAssignableFrom(fragment.getClass())) {
                fragment.onCancelled(error);
            }
        }
    }

    private final FragmentChildEventListener shoppingItemsEventListener =
            new FragmentChildEventListener(ShoppingItemsFragment.class);
    private final FragmentChildEventListener basketItemsEventListener =
            new FragmentChildEventListener(BasketItemsFragment.class);
    private final FragmentChildEventListener purchasedItemsEventListener =
            new FragmentChildEventListener(PurchasedItemsFragment.class);

    private DatabaseReference shoppingItemsReference;
    private DatabaseReference shoppingBasketReference;
    private DatabaseReference purchasedItemsReference;

    private TextView shopSyncNameTextView;
    private TextView descriptionTextView;
    private TableLayout usernamesTable;
    private Map<Integer, Button> itemTypeButtons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate: called");
        super.onCreate(savedInstanceState);

        FirebaseUser user = checkIfUserIsLoggedInAndFetch(true);
        if (user == null) {
            Log.e(TAG, "onCreate: user is not signed in");
            finish();
            return;
        }

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
        itemTypeButtons = Map.of(R.id.buttonShoppingItems, shoppingItemsButton,
                                 R.id.buttonBasketItems, basketItemsButton,
                                 R.id.buttonPurchasedItems, purchasedItemsButton);
        itemTypeButtons.values()
                .forEach(button -> button.setOnClickListener(v -> handleItemsTypeChange(button)));
        // set the shopping items button as the default selected button
        handleItemsTypeChange(shoppingItemsButton);

        // fetch the shop sync id from the intent and populate the metadata
        String shopSyncUid = getIntent().getStringExtra(Constants.SHOP_SYNC_UID);
        if (shopSyncUid == null) {
            throw new IllegalStateException("ShopSync started without shop sync id");
        }

        // fetch the shop sync from the database
        applicationGraph.shopSyncsService().getShopSyncWithUid(shopSyncUid)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DataSnapshot dataSnapshot = task.getResult();
                        if (dataSnapshot == null) {
                            Log.e(TAG, "onCreate: DataSnapshot is null");
                            throw new IllegalNullValueException("DataSnapshot is null");
                        }

                        ShopSyncModel shopSync = dataSnapshot.getValue(ShopSyncModel.class);
                        if (shopSync == null) {
                            Log.e(TAG, "onCreate: no shop sync found with id: " + shopSyncUid);
                            throw new IllegalNullValueException("No shop sync found with id: " + shopSyncUid);
                        }

                        Log.d(TAG, "onCreate: shop sync: " + shopSync);

                        // populate the metadata
                        populateMetaData(shopSync);
                    } else {
                        Log.e(TAG, "onCreate: failed to fetch shop sync with id: " + shopSyncUid,
                              task.getException());
                        throw new TaskFailureException(task, "Failed to fetch shop sync with id: "
                                + shopSyncUid);
                    }
                });

        // add child event listener for shopping items
        shoppingItemsReference = applicationGraph.shopSyncsService().getShopSyncsFirebaseReference()
                .getShoppingItemsCollection(shopSyncUid);
        shoppingItemsReference.addChildEventListener(shoppingItemsEventListener);

        // add child event listener for basket items
        shoppingBasketReference = applicationGraph.shopSyncsService()
                .getShopSyncsFirebaseReference().getShoppingBasketsCollection(shopSyncUid)
                .child(user.getUid()).child(Constants.BASKET_ITEMS_DB_KEY);
        shoppingBasketReference.addChildEventListener(basketItemsEventListener);

        // add child event listener for purchased items
        purchasedItemsReference = applicationGraph.shopSyncsService()
                .getShopSyncsFirebaseReference().getPurchasedItemsCollection(shopSyncUid);
        purchasedItemsReference.addChildEventListener(purchasedItemsEventListener);
    }

    @Override
    protected void onStop() {
        super.onStop();

        // fetch the shop sync id from the intent and populate the metadata
        String shopSyncUid = getIntent().getStringExtra(Constants.SHOP_SYNC_UID);
        if (shopSyncUid == null) {
            throw new IllegalStateException("ShopSync started without shop sync id");
        }

        // remove the child event listener for shopping items
        if (shoppingItemsReference != null) {
            shoppingItemsReference.removeEventListener(shoppingItemsEventListener);
        }

        // remove the child event listener for basket items
        if (shoppingBasketReference != null) {
            shoppingBasketReference.removeEventListener(basketItemsEventListener);
        }

        // remove the child event listener for purchased items
        if (purchasedItemsReference != null) {
            purchasedItemsReference.removeEventListener(purchasedItemsEventListener);
        }
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

    private ChildEventListenerFragment getCurrentChildEventListenerFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentById(R.id.fragmentContainer);
        if (!(fragment instanceof ChildEventListenerFragment)) {
            throw new IllegalStateException("Current fragment is not a child event listener " +
                                                    "fragment");
        }
        return (ChildEventListenerFragment) fragment;
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

        Consumer<List<String>> userUidsConsumer =
                userUids -> userUids.forEach(userUid -> applicationGraph.usersService()
                        .getUserProfileWithUid(userUid)
                        .addOnSuccessListener(data -> handleUserProfileData(data, userUid)));

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
        /*
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            setContentView(R.layout.activity_shop_sync_landscape);
        } else {
            setContentView(R.layout.activity_shop_sync);
        }
         */
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
            case ACTION_DELETE_SHOPPING_ITEM -> {
                if (props == null) {
                    throw new IllegalNullValueException("Props cannot be null for " + action);
                }
                deleteShoppingItem(shopSyncUid, props);
            }
            case ACTION_MOVE_SHOPPING_ITEM_TO_BASKET -> {
                if (props == null) {
                    throw new IllegalNullValueException("Props cannot be null for " + action);
                }
                moveShoppingItemToBasket(shopSyncUid, props);
            }
            case ACTION_INITIALIZE_BASKET_ITEMS -> {
                if (props == null) {
                    throw new IllegalNullValueException("Props cannot be null for " + action);
                }
                initializeBasketItems(shopSyncUid, props);
            }
            case ACTION_FETCH_ITEM_NAME_BY_SHOPPING_ITEM_UID -> {
                if (props == null) {
                    throw new IllegalNullValueException("Props cannot be null for " + action);
                }
                fetchItemNameByShoppingItemUid(shopSyncUid, props);
            }
            case ACTION_UPDATE_BASKET_ITEM -> {
                if (props == null) {
                    throw new IllegalNullValueException("Props cannot be null for " + action);
                }
                updateBasketItem(shopSyncUid, props);
            }
            case ACTION_PURCHASE_BASKET_ITEM -> {
                if (props == null) {
                    throw new IllegalNullValueException("Props cannot be null for " + action);
                }
                purchaseBasketItem(shopSyncUid, props);
            }
            case ACTION_REMOVE_BASKET_ITEM -> {
                if (props == null) {
                    throw new IllegalNullValueException("Props cannot be null for " + action);
                }
                removeBasketItem(shopSyncUid, props);
            }
            case ACTION_UNDO_PURCHASE -> {
                if (props == null) {
                    throw new IllegalNullValueException("Props cannot be null for " + action);
                }
                undoPurchase(shopSyncUid, props);
            }
            case ACTION_DELETE_PURCHASE -> {
                if (props == null) {
                    throw new IllegalNullValueException("Props cannot be null for " + action);
                }
                deletePurchase(shopSyncUid, props);
            }
        }
    }

    private void addShoppingItem(@NonNull String shopSyncUid) {
        Log.d(TAG, "addShoppingItem: adding shopping item");

        ShoppingItemModel shoppingItem = applicationGraph.shopSyncsService()
                .addShoppingItem(shopSyncUid, "New " + "Shopping Item", false);

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

                        ShoppingItemsAdapter adapter =
                                (ShoppingItemsAdapter) props.get(Constants.ADAPTER);
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
                        Log.d(TAG,
                              "populateShoppingItems: shopping items list size: " + shoppingItems.size());

                        // populate the shopping items list and notify the adapter
                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            // fetch and add the shopping item
                            ShoppingItemModel shoppingItem =
                                    child.getValue(ShoppingItemModel.class);
                            if (shoppingItem == null) {
                                Log.e(TAG,
                                      "populateShoppingItems: no shopping item found with " + "id" +
                                              ": " + child.getKey());
                                throw new IllegalNullValueException("No shopping item found with "
                                                                            + "id: " + child.getKey());
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

    private void deleteShoppingItem(@NonNull String shopSyncUid, @NonNull Props props) {
        Log.d(TAG, "deleteShoppingItem: deleting shopping item");

        // fetch the shopping item from the props and delete it
        ShoppingItemModel shoppingItem = props.get(Constants.SHOPPING_ITEM,
                                                   ShoppingItemModel.class);
        if (shoppingItem == null) {
            throw new IllegalNullValueException("Shopping item is null");
        }

        applicationGraph.shopSyncsService()
                .deleteShoppingItem(shopSyncUid, shoppingItem.getShoppingItemUid())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "deleteShoppingItem: successfully deleted shopping item");
                    } else {
                        Log.e(TAG, "deleteShoppingItem: failed to delete shopping item",
                              task.getException());
                    }
                });
    }

    private void moveShoppingItemToBasket(@NonNull String shopSyncUid, @NonNull Props props) {
        Log.d(TAG, "moveShoppingItemToBasket: moving shopping item to basket");

        // shopping basket uid is the same as the user's uid
        FirebaseUser user = checkIfUserIsLoggedInAndFetch(true);
        if (user == null) {
            Log.e(TAG, "moveShoppingItemToBasket: user is not signed in");
            return;
        }

        // fetch the shopping item from the props and add it to the basket
        ShoppingItemModel shoppingItem = props.get(Constants.SHOPPING_ITEM,
                                                   ShoppingItemModel.class);
        if (shoppingItem == null) {
            Log.e(TAG, "moveShoppingItemToBasket: shopping item is null");
            throw new IllegalNullValueException("Shopping item is null");
        }

        // on-success consumer
        Consumer<BasketItemModel> onSuccess = basketItem -> {
            Log.d(TAG, "moveShoppingItemToBasket: successfully added shopping item to basket");

            Runnable onSuccessRunnable = props.get(Constants.ON_SUCCESS, Runnable.class);
            if (onSuccessRunnable != null) {
                onSuccessRunnable.run();
            }
        };

        // on-failure consumer
        Consumer<ErrorHandle> onFailure = error -> {
            Log.e(TAG, "moveShoppingItemToBasket: failed to add shopping item to basket due to " +
                    "error: " + error);

            Runnable onFailureRunnable = props.get(Constants.ON_FAILURE, Runnable.class);
            if (onFailureRunnable != null) {
                onFailureRunnable.run();
            }
        };

        applicationGraph.shopSyncsService()
                .addBasketItem(shopSyncUid, user.getUid(), shoppingItem.getShoppingItemUid(),
                               DEFAULT_QUANTITY, DEFAULT_PRICE_PER_UNIT, onSuccess, onFailure);
    }

    @SuppressLint("NotifyDataSetChanged")
    @SuppressWarnings("unchecked")
    private void initializeBasketItems(@NonNull String shopSyncUid, @NonNull Props props) {
        Log.d(TAG, "initializeBasketItems: initializing basket items");

        FirebaseUser user = checkIfUserIsLoggedInAndFetch(true);
        if (user == null) {
            finish();
            return;
        }

        List<BasketItemModel> basketItems = (List<BasketItemModel>) props.get(PROP_BASKET_ITEMS);
        if (basketItems == null) {
            Log.e(TAG, "initializeBasketItems: basket items list is null");
            throw new IllegalNullValueException("Basket items list is null");
        }

        BasketItemsAdapter adapter = props.get(Constants.ADAPTER, BasketItemsAdapter.class);
        if (adapter == null) {
            Log.e(TAG, "initializeBasketItems: adapter is null");
            throw new IllegalNullValueException("Adapter is null");
        }

        // fetch basket items list from the props and populate it
        applicationGraph.shopSyncsService().getShoppingBasketWithUid(shopSyncUid, user.getUid())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DataSnapshot dataSnapshot = task.getResult();
                        if (dataSnapshot == null) {
                            Log.e(TAG, "initializeBasketItems: DataSnapshot is null");
                            throw new IllegalNullValueException("DataSnapshot is null");
                        }

                        ShoppingBasketModel shoppingBasket =
                                dataSnapshot.getValue(ShoppingBasketModel.class);
                        if (shoppingBasket == null) {
                            Log.e(TAG, "initializeBasketItems: no shopping basket found with " +
                                    "uid: " + user.getUid());
                            throw new IllegalNullValueException("No shopping basket found with " + "uid: " + user.getUid());
                        }

                        Map<String, BasketItemModel> shoppingBasketItems =
                                shoppingBasket.getBasketItems();
                        if (shoppingBasketItems == null) {
                            throw new IllegalNullValueException("Shopping basket items is null");
                        }

                        shoppingBasketItems.values().forEach(basketItem -> {
                            // basket items extends ArraySetList so [contains] is constant time
                            if (!basketItems.contains(basketItem)) {
                                basketItems.add(basketItem);
                            }
                        });

                        adapter.notifyDataSetChanged();
                    } else {
                        Log.e(TAG, "initializeBasketItems: failed to fetch shopping basket",
                              task.getException());
                        throw new TaskFailureException(task, "Failed to fetch shopping basket");
                    }
                });
    }

    private void fetchItemNameByShoppingItemUid(@NonNull String shopSyncUid, @NonNull Props props) {
        Log.d(TAG, "fetchItemNameByShoppingItemUid: fetching item name");

        // fetch the item name
        String shoppingItemUid = props.get(Constants.SHOPPING_ITEM, String.class);
        if (shoppingItemUid == null) {
            throw new IllegalNullValueException("Shopping item uid cannot be null");
        }

        TextView itemTextView = props.get(Constants.TEXT_VIEW, TextView.class);
        if (itemTextView == null) {
            throw new IllegalNullValueException("Item text view cannot be null");
        }

        applicationGraph.shopSyncsService().getShoppingItemWithUid(shopSyncUid, shoppingItemUid)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DataSnapshot dataSnapshot = task.getResult();
                        if (dataSnapshot == null) {
                            Log.e(TAG, "fetchItemNameByShoppingItemUid: DataSnapshot is null");
                            throw new IllegalNullValueException("DataSnapshot cannot be null");
                        }

                        ShoppingItemModel shoppingItem =
                                dataSnapshot.getValue(ShoppingItemModel.class);
                        if (shoppingItem == null || shoppingItem.getShoppingItemUid() == null || shoppingItem.getShoppingItemUid()
                                .isBlank()) {
                            Log.e(TAG, "fetchItemNameByShoppingItemUid: no shopping item" + " " +
                                    "found for shop sync uid = " + shopSyncUid + " and shopping " + "item uid = " + shoppingItemUid);
                            throw new IllegalNullValueException("No shopping item found for shop " +
                                                                        "sync uid = " + shopSyncUid + " and shopping item uid = " + shoppingItemUid);
                        }

                        // set the name of the item in the text view
                        itemTextView.setText(shoppingItem.getName());
                        Log.d(TAG, "fetchItemNameByShoppingItemUid: item text view has been " +
                                "set with the item's name");
                    }
                });
    }

    private void updateBasketItem(@NonNull String shopSyncUid, @NonNull Props props) {
        Log.d(TAG, "updateBasketItem: updating basket item");

        // fetch the basket item from the props and update it
        BasketItemModel basketItem = props.get(Constants.BASKET_ITEM, BasketItemModel.class);
        if (basketItem == null) {
            throw new IllegalNullValueException("Basket item is null");
        }

        String shoppingItemUid = basketItem.getShoppingItemUid();
        if (shoppingItemUid == null) {
            throw new IllegalNullValueException("Shopping item uid is null");
        }
        String shoppingBasketUid = basketItem.getShoppingBasketUid();
        if (shoppingBasketUid == null) {
            throw new IllegalNullValueException("Shopping basket uid is null");
        }

        applicationGraph.shopSyncsService().getShoppingBasketWithUid(shopSyncUid, shoppingBasketUid)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DataSnapshot dataSnapshot = task.getResult();
                        if (dataSnapshot == null) {
                            Log.e(TAG, "updateBasketItem: DataSnapshot is null");
                            throw new IllegalNullValueException("DataSnapshot is null");
                        }

                        ShoppingBasketModel shoppingBasket =
                                dataSnapshot.getValue(ShoppingBasketModel.class);
                        if (shoppingBasket == null) {
                            Log.e(TAG,
                                  "updateBasketItem: no shopping basket found with uid: " + shoppingBasketUid);
                            throw new IllegalNullValueException("No shopping basket found with " + "uid: " + shoppingBasketUid);
                        }

                        Map<String, BasketItemModel> basketItems = shoppingBasket.getBasketItems();
                        if (basketItems == null) {
                            throw new IllegalNullValueException("Basket items is null");
                        }

                        BasketItemModel oldBasketItem = basketItems.get(shoppingItemUid);
                        if (oldBasketItem == null) {
                            Log.e(TAG,
                                  "updateBasketItem: no basket item found with uid: " + shoppingItemUid);
                            throw new IllegalNullValueException("No basket item found with uid: " + shoppingItemUid);
                        }

                        // update the basket item
                        oldBasketItem.setQuantity(basketItem.getQuantity());
                        oldBasketItem.setPricePerUnit(basketItem.getPricePerUnit());
                        applicationGraph.shopSyncsService()
                                .updateShoppingBasket(shopSyncUid, shoppingBasket);
                    } else {
                        Log.e(TAG,
                              "updateBasketItem: failed to fetch shopping basket with uid: " + shoppingBasketUid, task.getException());
                        throw new TaskFailureException(task,
                                                       "Failed to fetch shopping basket " + "with" +
                                                               " uid: " + shoppingBasketUid);
                    }
                });
    }

    private void purchaseBasketItem(@NonNull String shopSyncUid, @NonNull Props props) {
        Log.d(TAG, "purchaseBasketItem: purchasing basket item");

        FirebaseUser user = checkIfUserIsLoggedInAndFetch(true);
        if (user == null) {
            throw new IllegalNullValueException("User is null");
        }

        BasketItemModel basketItem = props.get(Constants.BASKET_ITEM, BasketItemModel.class);
        if (basketItem == null) {
            Log.e(TAG, "purchaseBasketItem: basket item is null");
            throw new IllegalNullValueException("Basket item is null");
        }

        applicationGraph.shopSyncsService()
                .addPurchasedItem(shopSyncUid, user.getUid(), basketItem, null, null);
    }

    private void removeBasketItem(@NonNull String shopSyncUid, @NonNull Props props) {
        Log.d(TAG, "removeBasketItem: removing basket item");

        BasketItemModel basketItem = props.get(Constants.BASKET_ITEM, BasketItemModel.class);
        if (basketItem == null) {
            Log.e(TAG, "removeBasketItem: basket item is null");
            throw new IllegalNullValueException("Basket item is null");
        }

        applicationGraph.shopSyncsService()
                .deleteBasketItem(shopSyncUid, basketItem.getShoppingBasketUid(),
                                  basketItem.getShoppingItemUid(), null, true);
    }

    private void undoPurchase(@NonNull String shopSyncUid, @NonNull Props props) {
        Log.d(TAG, "undoPurchase: undoing purchase");

        // fetch the purchased item from the props and undo the purchase
        PurchasedItemModel purchasedItem = props.get(Constants.PURCHASED_ITEM,
                                                     PurchasedItemModel.class);
        if (purchasedItem == null) {
            throw new IllegalNullValueException("Purchased item is null");
        }

        // fetch shopping item to be re-added to shop sync
        ShoppingItemModel shoppingItem = purchasedItem.getShoppingItem();
        if (shoppingItem == null) {
            throw new IllegalNullValueException("Shopping item is null");
        }

        // fetch basket item to be re-added to user's shopping basket
        BasketItemModel basketItem = purchasedItem.getBasketItem();
        if (basketItem == null) {
            throw new IllegalNullValueException("Basket item is null");
        }

        // delete the purchased item
        applicationGraph.shopSyncsService()
                .deletePurchasedItem(shopSyncUid, purchasedItem.getPurchasedItemUid())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "undoPurchase: successfully deleted purchased item");

                        // re-add the shopping item to the shop sync
                        ShoppingItemModel addedShoppingItem = applicationGraph.shopSyncsService()
                                .addShoppingItem(shopSyncUid, shoppingItem.getName(), true);
                        Log.d(TAG, "undoPurchase: shopping item added to db: " + shoppingItem);
                        Log.d(TAG,
                              "undoPurchase: shopping item returned from db: " + addedShoppingItem);

                        // re-add the basket item to the user's shopping basket
                        Consumer<BasketItemModel> onSuccess = _basketItem -> Log.d(TAG,
                                                                                   "undoPurchase:" +
                                                                                           " successfully added basket item to db: " + basketItem);
                        Consumer<ErrorHandle> onFailure = error -> Log.e(TAG, "undoPurchase: " +
                                "failed to add basket item to db due to error: " + error);
                        applicationGraph.shopSyncsService()
                                .addBasketItem(shopSyncUid, basketItem.getShoppingBasketUid(),
                                               basketItem.getShoppingItemUid(),
                                               basketItem.getQuantity(),
                                               basketItem.getPricePerUnit(), onSuccess, onFailure);
                    } else {
                        Log.e(TAG, "undoPurchase: failed to delete purchased item",
                              task.getException());
                    }
                });
    }

    private void deletePurchase(@NonNull String shopSyncUid, @NonNull Props props) {
        Log.d(TAG, "deletePurchase: deleting purchase");

        // fetch the purchased item from the props and delete it
        PurchasedItemModel purchasedItem = props.get(Constants.PURCHASED_ITEM,
                                                     PurchasedItemModel.class);
        if (purchasedItem == null) {
            throw new IllegalNullValueException("Purchased item is null");
        }

        applicationGraph.shopSyncsService()
                .deletePurchasedItem(shopSyncUid, purchasedItem.getPurchasedItemUid())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "deletePurchase: successfully deleted purchased item");
                    } else {
                        Log.e(TAG, "deletePurchase: failed to delete purchased item",
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

