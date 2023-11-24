package edu.uga.cs.shopsync;

import android.util.Log;

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
