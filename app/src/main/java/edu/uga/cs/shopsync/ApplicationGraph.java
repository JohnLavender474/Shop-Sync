package edu.uga.cs.shopsync;

import javax.inject.Singleton;

import dagger.Component;
import edu.uga.cs.shopsync.backend.services.ShopSyncsService;
import edu.uga.cs.shopsync.backend.services.UsersService;

/**
 * The application graph.
 */
@Component
@Singleton
public interface ApplicationGraph {

    /**
     * Returns the shop syncs service.
     *
     * @return the shop syncs service
     */
    ShopSyncsService shopSyncsService();

    /**
     * Returns the users service.
     *
     * @return the users service
     */
    UsersService usersService();

}
