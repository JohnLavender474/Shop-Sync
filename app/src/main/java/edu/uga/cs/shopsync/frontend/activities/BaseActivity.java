package edu.uga.cs.shopsync.frontend.activities;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseUser;

import edu.uga.cs.shopsync.ApplicationGraph;
import edu.uga.cs.shopsync.ApplicationGraphSingleton;
import edu.uga.cs.shopsync.R;
import edu.uga.cs.shopsync.backend.services.UsersService;

/**
 * Base activity class for all activities in the app. This class provides a common interface for
 * all activities to access the application graph along with common methods.
 */
public class BaseActivity extends AppCompatActivity {

    private static final String TAG = "BaseActivity";

    protected final ApplicationGraph applicationGraph;

    /**
     * Default constructor for BaseActivity. This constructor uses the singleton instance of the
     * application graph. This constructor should only be used by the Android framework.
     *
     * @see ApplicationGraphSingleton
     */
    public BaseActivity() {
        applicationGraph = ApplicationGraphSingleton.getInstance();
    }

    /**
     * Constructor for BaseActivity. This constructor should be used for testing purposes only.
     *
     * @param applicationGraph The application graph to use for this activity.
     */
    BaseActivity(ApplicationGraph applicationGraph) {
        this.applicationGraph = applicationGraph;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.my_account) {
            Class<? extends BaseActivity> clazz;
            if (applicationGraph.usersService().isCurrentUserSignedIn()) {
                clazz = MyAccountActivity.class;
            } else {
                clazz = SignInActivity.class;
            }
            Intent intent = new Intent(this, clazz);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Calls {@link #checkIfUserIsLoggedInAndFetch(Class)} with {@link MainActivity} as the
     * activity class to redirect to if the user is not signed in, or null if [redirect] is false.
     *
     * @param redirect Whether or not to redirect to the main activity if the user is not signed in.
     * @noinspection SameParameterValue
     */
    protected FirebaseUser checkIfUserIsLoggedInAndFetch(boolean redirect) {
        return checkIfUserIsLoggedInAndFetch(redirect ? MainActivity.class : null);
    }

    /**
     * Checks if the user is signed in and fetches the current user. If the user is not signed in,
     * null is returned and the user is redirected to the main activity. It is possible this method
     * can throw an {@link IllegalStateException}, though in theory this should never happen. The
     * exception is thrown when {@link UsersService#isCurrentUserSignedIn()} returns true but
     * {@link UsersService#getCurrentFirebaseUser()} returns null.
     *
     * @param activityClass The activity class to redirect to if the user is not signed in. If this
     *                      is null, then no redirect is performed.
     * @return The current user if signed in, null otherwise.
     */
    protected FirebaseUser checkIfUserIsLoggedInAndFetch(Class<? extends Activity> activityClass) {
        if (!applicationGraph.usersService().isCurrentUserSignedIn()) {
            Log.d(TAG, "checkIfUserIsLoggedInAndFetch: user not signed in, redirecting to " +
                    "activity " + activityClass);

            Toast.makeText(this, "You must be signed in to view the requested activity.",
                           Toast.LENGTH_SHORT).show();

            if (activityClass != null) {
                Intent intent = new Intent(this, activityClass);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }

            return null;
        }

        FirebaseUser currentUser = applicationGraph.usersService().getCurrentFirebaseUser();
        if (currentUser == null) {
            throw new IllegalStateException("Current user cannot be null at this point");
        }
        Log.d(TAG, "checkIfUserIsLoggedInAndFetch: user signed in with email " +
                currentUser.getEmail() + " and id (" + currentUser.getUid() + ")");

        return currentUser;
    }

    /**
     * Signs out the current user and redirects to the main activity. This method should be called
     * when an error occurs that requires the user to be signed out.
     *
     * @param error The error message to display to the user.
     */
    protected void signOutOnErrorAndRedirectToMainActivity(String error) {
        Log.e(TAG, "signOutOnErrorAndRedirectToMainActivity: " + error);
        Toast.makeText(getApplicationContext(), error, Toast.LENGTH_SHORT).show();

        // sign out the user on error
        applicationGraph.usersService().signOut();

        // redirect to the main activity on error
        Intent intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

}
