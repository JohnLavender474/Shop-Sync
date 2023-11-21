package edu.uga.cs.shopsync;

import javax.inject.Singleton;

import dagger.Component;
import edu.uga.cs.shopsync.backend.services.UsersService;

@Component
@Singleton
public interface ApplicationGraph {

    // TODO: remove if not required
    /*
    PurchasedItemsFirebaseReference purchasedItemsFirebaseReference();

    ShoppingItemsFirebaseReference shoppingItemsFirebaseReference();

    ShopSyncsFirebaseReference shopSyncsFirebaseReference();

    UsersFirebaseReference usersFirebaseReference();
    */

    UsersService usersService();

}
