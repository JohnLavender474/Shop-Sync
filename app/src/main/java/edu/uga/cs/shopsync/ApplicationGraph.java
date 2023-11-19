package edu.uga.cs.shopsync;

import javax.inject.Singleton;

import dagger.Component;
import edu.uga.cs.shopsync.firebase.PurchasedItemsFirebaseReference;
import edu.uga.cs.shopsync.firebase.ShopSyncsFirebaseReference;
import edu.uga.cs.shopsync.firebase.ShoppingItemsFirebaseReference;
import edu.uga.cs.shopsync.firebase.UsersFirebaseReference;
import edu.uga.cs.shopsync.services.UsersService;

@Component
@Singleton
public interface ApplicationGraph {

    PurchasedItemsFirebaseReference purchasedItemsFirebaseReference();

    ShoppingItemsFirebaseReference shoppingItemsFirebaseReference();

    ShopSyncsFirebaseReference shopSyncsFirebaseReference();

    UsersFirebaseReference usersFirebaseReference();

    UsersService usersService();

}
