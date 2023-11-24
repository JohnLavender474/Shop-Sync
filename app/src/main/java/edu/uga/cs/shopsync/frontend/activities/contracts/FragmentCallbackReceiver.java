package edu.uga.cs.shopsync.frontend.activities.contracts;

import edu.uga.cs.shopsync.utils.Props;

/**
 * Interface for activities that receive callbacks from fragments. The activity must implement this
 * interface in order to receive callbacks from fragments.
 */
public interface FragmentCallbackReceiver {

    /**
     * Callback method for fragments to call when they want to send data back to the activity.
     *
     * @param props The data to send back to the activity.
     */
    void onFragmentCallback(String action, Props props);

}
