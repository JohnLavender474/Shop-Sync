package edu.uga.cs.shopsync;

import android.util.Log;

import com.google.firebase.database.DataSnapshot;

import java.util.List;
import java.util.function.Consumer;

import edu.uga.cs.shopsync.backend.models.BasketItemModel;
import edu.uga.cs.shopsync.backend.models.ShopSyncModel;
import edu.uga.cs.shopsync.backend.models.ShoppingBasketModel;
import edu.uga.cs.shopsync.backend.models.ShoppingItemModel;
import edu.uga.cs.shopsync.backend.models.UserProfileModel;
import edu.uga.cs.shopsync.backend.services.ShopSyncsService;
import edu.uga.cs.shopsync.backend.services.UsersService;

public class TemporaryStuff {

    public static void testAddNewUser(ApplicationGraph applicationGraph) {
        UsersService usersService = applicationGraph.usersService();
        usersService.createUser("dawg@mail.com", "dawg", "password",
                                userProfile -> Log.d("TemporaryStuff", "user created"), null);
    }

    public static void testFindByEmail(ApplicationGraph applicationGraph) {
        UsersService usersService = applicationGraph.usersService();

        Consumer<UserProfileModel> onSuccess = userProfile -> usersService
                .getUserProfilesWithEmail(userProfile.getEmail())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DataSnapshot dataSnapshot = task.getResult();
                        if (dataSnapshot == null) {
                            Log.d("TemporaryStuff", "data snapshot is null");
                            return;
                        }

                        for (DataSnapshot child : dataSnapshot.getChildren()) {
                            Log.d("TemporaryStuff", "child: " + child);

                            UserProfileModel _userProfile = child.getValue(UserProfileModel.class);

                            Log.d("TemporaryStuff", "expected user profile: " + userProfile);
                            Log.d("TemporaryStuff", "actual user profile: " + _userProfile);

                            if (userProfile.equals(_userProfile)) {
                                Log.d("TemporaryStuff", "user profile matches");
                            } else {
                                Log.e("TemporaryStuff", "user profile does not match");
                            }

                            // there should only be one child hence the break statement
                            break;
                        }
                    } else {
                        Log.d("TemporaryStuff", "failed to get user profile");
                    }
                });

        usersService.createUser("dawg@mail.com", "dawg", "password",
                                onSuccess, null);
    }

    public static void testAddShoppingItem(ApplicationGraph applicationGraph) {
        Log.d("TemporaryStuff", "testAddShoppingItemToShoppingBasket");

        UsersService usersService = applicationGraph.usersService();

        // on create user
        Consumer<UserProfileModel> createUserOnSuccess = userProfile -> {
            ShopSyncsService shopSyncsService = applicationGraph.shopSyncsService();

            // on create shop sync
            Consumer<ShopSyncModel> createShopSyncOnSuccess = shopSync -> {
                // create shopping item
                shopSyncsService.addShoppingItem(shopSync.getUid(), "name", false);

                // create user's shopping basket
                shopSyncsService.addShoppingBasket(shopSync.getUid(), userProfile.getUserUid());
            };

            shopSyncsService.addShopSync("Shop Sync", "Description",
                                         List.of(userProfile.getUserUid()),
                                         createShopSyncOnSuccess, null);

        };

        // create user
        usersService.createUser("dawg@mail.com", "dawg", "password",
                                createUserOnSuccess, null);
    }

    public static void testAddShoppingItemToShoppingBasket(ApplicationGraph applicationGraph) {
        Log.d("TemporaryStuff", "testAddShoppingItemToShoppingBasket");

        UsersService usersService = applicationGraph.usersService();

        // on create user
        Consumer<UserProfileModel> createUserOnSuccess = userProfile -> {
            ShopSyncsService shopSyncsService = applicationGraph.shopSyncsService();

            // on create shop sync
            Consumer<ShopSyncModel> createShopSyncOnSuccess = shopSync -> {
                // create shopping item
                ShoppingItemModel shoppingItem = shopSyncsService.
                        addShoppingItem(shopSync.getUid(), "name", false);

                // create user's shopping basket
                ShoppingBasketModel shoppingBasket = shopSyncsService
                        .addShoppingBasket(shopSync.getUid(), userProfile.getUserUid());

                Consumer<BasketItemModel> addBasketItemOnSuccess = basketItem -> {
                    Log.d("TemporaryStuff", "addBasketItemOnSuccess");

                    // purchase the item
                    shopSyncsService.addPurchasedItem(shopSync.getUid(), shoppingBasket.getUid(),
                                                      basketItem, null, null);
                };

                // add basket item to shopping basket
                shopSyncsService.addBasketItem(shopSync.getUid(), shoppingBasket.getUid(),
                                               shoppingItem.getUid(), 1, 2,
                                               addBasketItemOnSuccess, null);
            };

            shopSyncsService.addShopSync("Shop Sync", "Description",
                                         List.of(userProfile.getUserUid()),
                                         createShopSyncOnSuccess, null);

        };

        // create user
        usersService.createUser("dawg@mail.com", "dawg", "password",
                                createUserOnSuccess, null);
    }

}
