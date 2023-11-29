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
import com.google.firebase.database.ValueEventListener;

import java.util.Map;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.inject.Singleton;

import edu.uga.cs.shopsync.backend.exceptions.IllegalNullValueException;
import edu.uga.cs.shopsync.backend.exceptions.TaskFailureException;
import edu.uga.cs.shopsync.backend.exceptions.UserAlreadyExistsException;
import edu.uga.cs.shopsync.backend.models.UserProfileModel;
import edu.uga.cs.shopsync.utils.ErrorHandle;
import edu.uga.cs.shopsync.utils.ErrorType;

/**
 * Provides methods to modify the firebase auth instance and the user profile collection.
 */
@Singleton
public class UsersFirebaseReference {

    private static final String TAG = "UsersFirebaseReference";
    public static final String USER_PROFILES_COLLECTION = "user_profiles";
    public static final String USER_EMAIL_FIELD = "email";

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
    public void createUser(@NonNull String email, @NonNull String username,
                           @NonNull String password,
                           @Nullable Consumer<UserProfileModel> onSuccess,
                           @Nullable Consumer<ErrorHandle> onError)
            throws TaskFailureException, UserAlreadyExistsException, IllegalNullValueException {
        Log.d(TAG, "createUser: creating user with email " + email);

        getUserProfileWithEmail(email).addOnCompleteListener(_checkIfExistsTask -> {
            Log.d(TAG, "createUser: task to check if username exists is complete");

            if (_checkIfExistsTask.isSuccessful()) {
                Log.d(TAG, "createUser: task to check if username exists is complete");
                DataSnapshot dataSnapshot = _checkIfExistsTask.getResult();

                // the data snapshot object should not be null
                if (dataSnapshot == null) {
                    Log.e(TAG, "createUser: task to check if username exists returned null " +
                            "data snapshot");
                    if (onError != null) {
                        onError.accept(new ErrorHandle(ErrorType.ILLEGAL_NULL_VALUE,
                                                       "Task to check if username exists " +
                                                               "returned null data snapshot"));
                    }
                    return;
                }

                // if the data snapshot exists, a user with the provided email already exists and
                // the task should fail
                if (dataSnapshot.exists()) {
                    Log.e(TAG, "createUser: user already exists with the email: " + email);
                    if (onError != null) {
                        onError.accept(new ErrorHandle(ErrorType.ENTITY_ALREADY_EXISTS, Map.of(
                                "email", email), "User already exists with the email: "
                                                               + email));
                    }
                    return;
                }

                // if the data snapshot does not exist, a user with the provided email does not
                // exist and the task should proceed
                firebaseAuth
                        .createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(_createUserTask -> {
                            if (_createUserTask.isSuccessful()) {
                                Log.d(TAG, "createUser: successfully created user with username " +
                                        username);
                                FirebaseUser user = firebaseAuth.getCurrentUser();

                                // the user should not be null after successful creation
                                if (user == null) {
                                    Log.e(TAG, "createUser: user should not be null after " +
                                            "successful creation");
                                    throw new IllegalNullValueException("User should not be null " +
                                                                                "after successful" +
                                                                                " creation");
                                }

                                // add the user profile to the user_profiles collection
                                String userUid = user.getUid();
                                String userEmail = user.getEmail();
                                UserProfileModel userProfileModel = new UserProfileModel(
                                        userUid, userEmail, username);
                                usersCollection.child(userUid).setValue(userProfileModel);

                                Log.d(TAG, "createUser: created user with username " + username);

                                // run the on success runnable if it is not null
                                if (onSuccess != null) {
                                    onSuccess.accept(userProfileModel);
                                }
                            } else {
                                // if the task to create the user fails, throw an exception
                                Log.e(TAG,
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
                Log.e(TAG, "createUser: task to check if username exists failed - " +
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
        Log.d(TAG, "signInUser: signing in user with email " + email);
        return firebaseAuth.signInWithEmailAndPassword(email, password);
    }

    /**
     * Returns if there is a user currently signed in.
     *
     * @return if there is a user currently signed in.
     */
    public boolean isCurrentUserSignedIn() {
        Log.d(TAG, "isCurrentUserSignedIn: checking if current user is signed in");
        return getCurrentFirebaseUser() != null;
    }

    /**
     * Returns the task that fetches the user profile for the user with the given unique id.
     *
     * @param userUid the user's unique id.
     * @return the task that fetches the user profile.
     */
    public @NonNull Task<DataSnapshot> getUserProfileWithUid(String userUid) {
        Log.d(TAG, "getUserProfileWithUid: getting user profile with uid (" + userUid + ")");
        return usersCollection.child(userUid).get();
    }

    /**
     * Returns the task that fetches the user profile for the user with the given email.
     *
     * @param email the user's email.
     * @return the task that fetches the user profile.
     */
    public Task<DataSnapshot> getUserProfileWithEmail(String email) {
        Log.d(TAG, "getUserProfileWithEmail: getting user profile with email (" + email + ")");
        return usersCollection.orderByChild(USER_EMAIL_FIELD).equalTo(email).get();
    }

    /**
     * Updates the user profile with the given user profile model.
     *
     * @param userProfileModel the user profile model
     * @noinspection UnusedReturnValue
     */
    public @NonNull Task<Void> updateUserProfile(@NonNull UserProfileModel userProfileModel) {
        Log.d(TAG, "updateUserProfile: updating user profile with uid (" +
                userProfileModel.getUserUid() + ")");
        return usersCollection.child(userProfileModel.getUserUid()).setValue(userProfileModel);
    }

    /**
     * Returns the task that fetches the user profile of the current user. Returns null if the
     * current user is not signed in.
     *
     * @return the task that fetches the user profile of the current user.
     */
    public @Nullable Task<DataSnapshot> getCurrentUserProfile() {
        Log.d(TAG, "getCurrentUserProfile: getting current user profile");

        FirebaseUser user = getCurrentFirebaseUser();
        if (user == null) {
            Log.d(TAG, "getCurrentUserProfile: current user is not signed in");
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
        Log.d(TAG, "signOut: signing out current user");
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
        Log.d(TAG, "deleteUser: deleting user");

        FirebaseUser user = getCurrentFirebaseUser();
        if (user == null) {
            Log.e(TAG, "deleteUser: cannot delete user if user is not logged in");
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
        Log.d(TAG, "changeUserPassword: changing user password");

        FirebaseUser user = getCurrentFirebaseUser();
        if (user == null) {
            Log.e(TAG, "changeUserPassword: cannot change password if user is not logged in");
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
        Log.d(TAG, "deleteUser: deleting user with uid (" + user.getUid() + ")");

        user.delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // remove the user account and profile
                usersCollection.child(user.getUid()).removeValue();
                Log.d(TAG, "User account and profile deleted.");

                if (onSuccess != null) {
                    onSuccess.run();
                }
            } else if (onFailure != null) {
                Log.e(TAG, "deleteUser: failed to delete user with uid (" + user.getUid() + ")",
                      task.getException());
                onFailure.run();
            }
        });
    }

    private void reauthenticateAndRun(@NonNull FirebaseUser user, @NonNull String password,
                                      @Nullable Runnable onSuccess, @Nullable Runnable onFailure)
            throws IllegalNullValueException {
        Log.d(TAG, "reauthenticateAndRun: re-authenticating user with uid (" + user.getUid() + ")");

        String email = user.getEmail();
        if (email == null) {
            Log.e(TAG, "reauthenticateAndRun: user email cannot be null");
            throw new IllegalNullValueException("User email cannot be null");
        }

        AuthCredential credential = getCredential(email, password);
        user.reauthenticate(credential).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d(TAG, "reauthenticateAndRun: successfully re-authenticated user with uid (" +
                        user.getUid() + ")");
                if (onSuccess != null) {
                    onSuccess.run();
                }
            } else if (onFailure != null) {
                Log.e(TAG, "reauthenticateAndRun: failed to re-authenticate user with uid (" +
                        user.getUid() + ")", task.getException());
                onFailure.run();
            }
        });
    }

    private void reauthenticateAndUpdatePassword(@NonNull FirebaseUser user,
                                                 @NonNull String oldPassword,
                                                 @NonNull String newPassword,
                                                 @Nullable Runnable onUpdatePassword,
                                                 @Nullable Runnable onFailureToAuthenticate) {
        Log.d(TAG, "reauthenticateAndUpdatePassword: re-authenticating and updating password " +
                "for user with uid (" + user.getUid() + ")");
        reauthenticateAndRun(user, oldPassword, () -> updatePassword(user, newPassword,
                                                                     onUpdatePassword),
                             onFailureToAuthenticate);
    }

    private void updatePassword(FirebaseUser user, String newPassword,
                                @Nullable Runnable onUpdatePassword) {
        Log.d(TAG, "updatePassword: updating password for user with uid (" + user.getUid() + ")");
        Task<Void> updatePasswordTask = user.updatePassword(newPassword);
        if (onUpdatePassword != null) {
            updatePasswordTask.addOnCompleteListener(t -> onUpdatePassword.run());
        }
    }

}
