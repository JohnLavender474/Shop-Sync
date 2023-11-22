package edu.uga.cs.shopsync.backend.firebase;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.inject.Singleton;

import edu.uga.cs.shopsync.backend.models.UserProfileModel;
import edu.uga.cs.shopsync.backend.exceptions.IllegalNullValueException;
import edu.uga.cs.shopsync.backend.exceptions.TaskFailureException;
import edu.uga.cs.shopsync.backend.exceptions.UserAlreadyExistsException;
import edu.uga.cs.shopsync.utils.ErrorHandle;
import edu.uga.cs.shopsync.utils.ErrorType;

/**
 * Provides methods to modify the firebase auth instance and the user profile collection.
 */
@Singleton
public class UsersFirebaseReference {

    private static final String TAG = "UsersFirebaseReference";
    private static final String USER_PROFILES_COLLECTION = "user_profiles";
    private static final String USER_EMAIL_FIELD = "email";

    private final FirebaseAuth firebaseAuth;
    private final DatabaseReference usersCollection;

    /**
     * Constructs a new UsersFirebaseReference. Empty constructor required for injection. Uses the
     * default firebase auth instance and the user_profiles collection.
     */
    @Inject
    public UsersFirebaseReference() {
        firebaseAuth = FirebaseAuth.getInstance();
        usersCollection = FirebaseDatabase.getInstance().getReference(USER_PROFILES_COLLECTION);
        Log.d(TAG, "UsersFirebaseReference: created");
    }

    /**
     * Package-private constructor for testing only. Uses the given firebase auth instance and the
     * user_profiles collection.
     *
     * @param firebaseAuth    the firebase auth instance
     * @param usersCollection the user_profiles collection
     */
    UsersFirebaseReference(FirebaseAuth firebaseAuth, DatabaseReference usersCollection) {
        this.firebaseAuth = firebaseAuth;
        this.usersCollection = usersCollection;
    }

    /**
     * Adds a value event listener.
     *
     * @param valueEventListener the value event listener.
     */
    public void addValueEventListener(ValueEventListener valueEventListener) {
        usersCollection.addValueEventListener(valueEventListener);
    }

    /**
     * Attempts to create a new user and add a user profile model to the user_profiles collection.
     * If the username already exists, the task will fail. If the task succeeds, the user will be
     * signed in automatically.
     *
     * @param email     the user's email address
     * @param username  the user's username
     * @param password  the user's password
     * @param onSuccess the runnable for if the task succeeds
     */
    public void createUser(String email, String username, String password,
                           @Nullable Runnable onSuccess, @Nullable Consumer<ErrorHandle> onError)
            throws TaskFailureException, UserAlreadyExistsException, IllegalNullValueException {
        // query to check if a user profile already exists with the given email
        Query query = usersCollection.orderByChild(USER_EMAIL_FIELD).equalTo(username);
        Task<DataSnapshot> checkIfExistsTask = query.get();

        checkIfExistsTask.addOnCompleteListener(_checkIfExistsTask -> {
            if (_checkIfExistsTask.isSuccessful()) {
                DataSnapshot dataSnapshot = _checkIfExistsTask.getResult();

                // the data snapshot object should not be null
                if (dataSnapshot == null) {
                    Log.d(TAG, "createUser: task to check if username exists returned null " +
                            "data snapshot");
                    if (onError != null) {
                        onError.accept(new ErrorHandle(ErrorType.TASK_FAILED,
                                                       "Task to check if username exists " +
                                                               "returned null data snapshot"));
                    }
                    return;
                }

                // if the data snapshot exists, a user with the provided email already exists and
                // the task should fail
                if (dataSnapshot.exists()) {
                    Log.d(TAG, "createUser: user already exists with the email: " + email);
                    if (onError != null) {
                        onError.accept(new ErrorHandle(ErrorType.USER_ALREADY_EXISTS, Map.of(
                                "email", email), "User already exists with the email: "
                                                               + email));
                    }
                    return;
                }

                // if the data snapshot does not exist, a user with the provided email does not
                // exist and the task should proceed
                Task<AuthResult> createUserTask =
                        firebaseAuth.createUserWithEmailAndPassword(email, password);

                createUserTask.addOnCompleteListener(_createUserTask -> {
                    if (_createUserTask.isSuccessful()) {
                        FirebaseUser user = firebaseAuth.getCurrentUser();

                        // the user should not be null after successful creation
                        if (user == null) {
                            throw new IllegalNullValueException("User should not be null after " +
                                                                        "successful creation");
                        }

                        // add the user profile to the user_profiles collection
                        String userUid = user.getUid();
                        String userEmail = user.getEmail();
                        UserProfileModel userProfileModel = new UserProfileModel(userUid,
                                                                                 userEmail,
                                                                                 username);
                        usersCollection.child(userUid).setValue(userProfileModel);

                        Log.d(TAG, "createUser: created user with username " + username);

                        // run the on success runnable if it is not null
                        if (onSuccess != null) {
                            onSuccess.run();
                        }
                    } else {
                        // if the task to create the user fails, throw an exception
                        Log.d(TAG,
                              "createUser: failed to create user with username " + username +
                                      " - " + _createUserTask.getException());
                        if (onError != null) {
                            onError.accept(new ErrorHandle(ErrorType.TASK_FAILED,
                                                           "Failed to create user with " +
                                                                   "username " + username));
                        }
                    }
                });
            } else {
                Log.d(TAG, "createUser: task to check if username exists failed - " +
                        _checkIfExistsTask.getException());
                if (onError != null) {
                    onError.accept(new ErrorHandle(ErrorType.TASK_FAILED,
                                                   "Task to check if username exists failed"));
                }
            }
        });
    }

    /**
     * Attempts to sing in the user with the given email and password.
     *
     * @param email    the user's email
     * @param password the user's password
     * @return the task that attempts to sign in the user
     */
    public @NonNull Task<AuthResult> signInUser(String email, String password) {
        return firebaseAuth.signInWithEmailAndPassword(email, password);
    }

    /**
     * Returns if there is a user currently signed in.
     *
     * @return if there is a user currently signed in.
     */
    public boolean isCurrentUserSignedIn() {
        return getCurrentFirebaseUser() != null;
    }

    /**
     * Returns the task that fetches the user profile for the user with the given unique id.
     *
     * @param userUid the user's unique id.
     * @return the task that fetches the user profile.
     */
    public @NonNull Task<DataSnapshot> getUserProfileWithUid(String userUid) {
        return usersCollection.child(userUid).get();
    }

    /**
     * Updates the user profile with the given user profile model.
     *
     * @param userProfileModel the user profile model
     * @noinspection UnusedReturnValue
     */
    public @NonNull Task<Void> updateUserProfile(UserProfileModel userProfileModel) {
        return usersCollection.child(userProfileModel.getUserUid()).setValue(userProfileModel);
    }

    /**
     * Returns the task that fetches the user profile of the current user. Returns null if the
     * current user is not signed in.
     *
     * @return the task that fetches the user profile of the current user.
     */
    public @Nullable Task<DataSnapshot> getCurrentUserProfile() {
        FirebaseUser user = getCurrentFirebaseUser();
        if (user == null) {
            return null;
        }
        return getUserProfileWithUid(user.getUid());
    }

    /**
     * Gets the current firebase user. Returns null if the current user is not signed in.
     *
     * @return the current firebase user.
     */
    public @Nullable FirebaseUser getCurrentFirebaseUser() {
        return firebaseAuth.getCurrentUser();
    }

    /**
     * Signs out the current user.
     */
    public void signOut() {
        firebaseAuth.signOut();
    }

    /**
     * Attempts to delete the current user. Throws an exception if the current user is not signed.
     *
     * @param password  the user's password
     * @param onFailure the runnable for if the task fails
     * @return the uid of the deleted user
     */
    public String deleteUser(@NonNull String password, @Nullable Runnable onSuccess,
                           @Nullable Runnable onFailure) {
        FirebaseUser user = getCurrentFirebaseUser();
        if (user == null) {
            throw new IllegalStateException("Cannot delete user if user is not logged in");
        }

        reauthenticateAndRun(user, password, () -> deleteUser(user, onSuccess, onFailure), null);
        return user.getUid();
    }

    /**
     * Attempts to change the user's password.
     *
     * @param oldPassword              the user's old password
     * @param newPassword              the user's new password
     * @param onUpdatePasswordListener the listener for when the update password task completes
     * @param onFailureToAuthenticate  the listener for if the authentication task fails
     */
    public void changeUserPassword(@NonNull String oldPassword, @NonNull String newPassword,
                                   @Nullable Runnable onUpdatePasswordListener,
                                   @Nullable Runnable onFailureToAuthenticate) {
        FirebaseUser user = getCurrentFirebaseUser();
        if (user == null) {
            throw new IllegalStateException("Cannot change password if user is not logged in");
        }

        reauthenticateAndUpdatePassword(user, oldPassword, newPassword, onUpdatePasswordListener,
                                        onFailureToAuthenticate);
    }

    /**
     * Package-private method that can be overridden for testing purposes. Returns the credential
     * for the email and password. Uses {@link EmailAuthProvider#getCredential(String, String)}
     * by default.
     *
     * @param email    the email
     * @param password the password
     * @return the credential for the email and password
     */
    AuthCredential getCredential(String email, String password) {
        return EmailAuthProvider.getCredential(email, password);
    }

    private void deleteUser(@NonNull FirebaseUser user, @Nullable Runnable onSuccess,
                            @Nullable Runnable onFailure) {
        user.delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // remove the user account and profile
                usersCollection.child(user.getUid()).removeValue();
                Log.d(TAG, "User account and profile deleted.");

                if (onSuccess != null) {
                    onSuccess.run();
                }
            } else if (onFailure != null) {
                onFailure.run();
            }
        });
    }

    private void reauthenticateAndRun(@NonNull FirebaseUser user, @NonNull String password,
                                      @Nullable Runnable onSuccess, @Nullable Runnable onFailure)
            throws IllegalNullValueException {
        String email = user.getEmail();
        if (email == null) {
            throw new IllegalNullValueException("User email cannot be null");
        }

        AuthCredential credential = getCredential(email, password);
        user.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                if (onSuccess != null) {
                    onSuccess.run();
                }
            } else if (onFailure != null) {
                onFailure.run();
            }
        });
    }

    private void reauthenticateAndUpdatePassword(@NonNull FirebaseUser user,
                                                 @NonNull String oldPassword,
                                                 @NonNull String newPassword,
                                                 @Nullable Runnable onUpdatePassword,
                                                 @Nullable Runnable onFailureToAuthenticate) {
        reauthenticateAndRun(user, oldPassword, () -> updatePassword(user, newPassword,
                                                                     onUpdatePassword),
                             onFailureToAuthenticate);
    }

    private void updatePassword(FirebaseUser user, String newPassword,
                                @Nullable Runnable onUpdatePassword) {
        Task<Void> updatePasswordTask = user.updatePassword(newPassword);
        if (onUpdatePassword != null) {
            updatePasswordTask.addOnCompleteListener(t -> onUpdatePassword.run());
        }
    }

}
