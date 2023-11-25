package edu.uga.cs.shopsync.utils;

import edu.uga.cs.shopsync.utils.Props;

/**
 * Interface for activities that receive callbacks from fragments. The activity must implement this
 * interface in order to receive callbacks from fragments.
 */
public interface CallbackReceiver {

    /**
     * Callback method for fragments to call when they want to send data back to the activity.
     *
     * @param props The data to send back to the activity.
     */
    void onCallback(String action, Props props);

}
