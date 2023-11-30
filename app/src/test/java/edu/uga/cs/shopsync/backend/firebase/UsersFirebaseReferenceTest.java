package edu.uga.cs.shopsync.backend.firebase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static edu.uga.cs.shopsync.backend.firebase.UsersFirebaseReference.USER_EMAIL_FIELD;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import edu.uga.cs.shopsync.backend.models.UserProfileModel;
import edu.uga.cs.shopsync.utils.DataWrapper;
import edu.uga.cs.shopsync.utils.ErrorHandle;
import edu.uga.cs.shopsync.utils.ErrorType;

@RunWith(MockitoJUnitRunner.class)
public class UsersFirebaseReferenceTest {

    private static final String TEST_UID = "testUid";
    private static final String TEST_EMAIL = "test@email.com";
    private static final String TEST_USERNAME = "testUser";
    private static final String TEST_PASSWORD = "Password123!";
    private static final String TEST_NEW_PASSWORD = "NewPassword123!";

    private FirebaseAuth mockFirebaseAuth;
    private DatabaseReference mockUsersCollection;
    private FirebaseUser mockFirebaseUser;
    private Task<AuthResult> mockAuthTask;
    private Task<DataSnapshot> mockDataTask;
    private Task<Void> mockVoidTask;
    private AuthCredential mockAuthCredential;
    private UsersFirebaseReference usersFirebaseReference;


    @Before
    @SuppressWarnings("unchecked")
    public void setUp() {
        // mocks
        mockFirebaseAuth = mock(FirebaseAuth.class);
        mockUsersCollection = mock(DatabaseReference.class);
        mockFirebaseUser = mock(FirebaseUser.class);
        mockAuthTask = mock(Task.class);
        mockDataTask = mock(Task.class);
        mockVoidTask = mock(Task.class);
        mockAuthCredential = mock(AuthCredential.class);

        // spies
        usersFirebaseReference = spy(new UsersFirebaseReference(mockFirebaseAuth,
                                                                mockUsersCollection));
    }

    @Test
    public void testAddValueEventListener() {
        ValueEventListener mockValueEventListener = mock(ValueEventListener.class);
        usersFirebaseReference.addValueEventListener(mockValueEventListener);

        verify(mockUsersCollection).addValueEventListener(eq(mockValueEventListener));
    }

    @Test
    public void testCreateUser_Success() {
        // Arrange
        setUpTaskGetUserProfileWithEmail(false, true);
        setUpCreateUserTask(true);

        DataWrapper<UserProfileModel> userProfileDataWrapper = new DataWrapper<>();
        setUpInsertUserIntoDatabase(userProfileDataWrapper);

        DataWrapper<Boolean> onSuccessCalled = new DataWrapper<>(false);
        DataWrapper<Boolean> onFailureCalled = new DataWrapper<>(false);

        // Act
        usersFirebaseReference.createUser(TEST_EMAIL, TEST_USERNAME, TEST_PASSWORD,
                                          userProfileModel -> onSuccessCalled.set(true),
                                          errorHandle -> onFailureCalled.set(true));

        // Assert
        UserProfileModel userProfile = userProfileDataWrapper.get();
        assertNotNull(userProfile);
        String userUid = userProfile.getUserUid();
        assertNotNull(userUid);
        assertEquals(TEST_UID, userUid);
        assertEquals(TEST_EMAIL, userProfile.getEmail());
        assertEquals(TEST_USERNAME, userProfile.getUsername());
        Boolean onSuccessResult = onSuccessCalled.get();
        assertNotNull(onSuccessResult);
        assertTrue(onSuccessResult);
        Boolean onFailureResult = onFailureCalled.get();
        assertNotNull(onFailureResult);
        assertFalse(onFailureResult);
    }

    @Test
    public void testCreateUser_Failure_TaskToCheckIfUsernameExists() {
        // Arrange
        setUpTaskGetUserProfileWithEmail(false, false);
        setUpCreateUserTask(false);

        DataWrapper<UserProfileModel> userProfileDataWrapper = new DataWrapper<>();
        setUpInsertUserIntoDatabase(userProfileDataWrapper);

        DataWrapper<Boolean> onSuccessCalled = new DataWrapper<>(false);
        DataWrapper<ErrorHandle> onFailureCalled = new DataWrapper<>();

        // Act
        usersFirebaseReference.createUser(TEST_EMAIL, TEST_USERNAME, TEST_PASSWORD,
                                          userProfileModel -> onSuccessCalled.set(true),
                                          onFailureCalled::set);

        // Assert
        Boolean onSuccessResult = onSuccessCalled.get();
        assertNotNull(onSuccessResult);
        assertFalse(onSuccessResult);
        assertNotNull(onFailureCalled.get());
        ErrorHandle errorHandle = onFailureCalled.get();
        assertNotNull(errorHandle);
        ErrorType errorType = errorHandle.errorType();
        assertNotNull(errorType);
        assertEquals(ErrorType.TASK_FAILED, errorHandle.errorType());
        assertEquals("Task to check if username exists failed", errorHandle.errorMessage());
        assertNull(userProfileDataWrapper.get());
    }

    @Test
    public void testCreateUser_Failure_UserAlreadyExists() {
        // Arrange
        setUpTaskGetUserProfileWithEmail(true, true);
        setUpCreateUserTask(false);

        DataWrapper<UserProfileModel> userProfileDataWrapper = new DataWrapper<>();
        setUpInsertUserIntoDatabase(userProfileDataWrapper);

        DataWrapper<Boolean> onSuccessCalled = new DataWrapper<>(false);
        DataWrapper<ErrorHandle> onFailureCalled = new DataWrapper<>();

        // Act
        usersFirebaseReference.createUser(TEST_EMAIL, TEST_USERNAME, TEST_PASSWORD,
                                          userProfileModel -> onSuccessCalled.set(true),
                                          onFailureCalled::set);

        // Assert
        Boolean onSuccessResult = onSuccessCalled.get();
        assertNotNull(onSuccessResult);
        assertFalse(onSuccessResult);
        assertNotNull(onFailureCalled.get());
        ErrorHandle errorHandle = onFailureCalled.get();
        assertNotNull(errorHandle);
        ErrorType errorType = errorHandle.errorType();
        assertNotNull(errorType);
        assertEquals(ErrorType.ENTITY_ALREADY_EXISTS, errorType);
        assertEquals("User already exists with the email: " + TEST_EMAIL,
                     errorHandle.errorMessage());
        assertTrue(errorHandle.props().containsKey("email"));
        assertEquals(TEST_EMAIL, errorHandle.props().get("email"));
        assertNull(userProfileDataWrapper.get());
    }

    @Test
    public void testSignInUser_Success() {
        // Arrange
        when(mockFirebaseAuth.signInWithEmailAndPassword(TEST_EMAIL, TEST_PASSWORD))
                .thenReturn(mockAuthTask);
        when(mockAuthTask.isSuccessful()).thenReturn(true);

        // Act
        Task<AuthResult> result = usersFirebaseReference.signInUser(TEST_EMAIL,
                                                                    TEST_PASSWORD);

        // Assert
        assertNotNull(result);
        assertTrue(result.isSuccessful());
    }

    @Test
    public void testSignInUser_Failure() {
        // Arrange
        when(mockFirebaseAuth.signInWithEmailAndPassword(TEST_EMAIL, TEST_PASSWORD))
                .thenReturn(mockAuthTask);
        when(mockAuthTask.isSuccessful()).thenReturn(false);

        // Act
        Task<AuthResult> result = usersFirebaseReference.signInUser(TEST_EMAIL, TEST_PASSWORD);

        // Assert
        assertNotNull(result);
        assertFalse(result.isSuccessful());
    }

    @Test
    public void testIsCurrentUserSignedIn_UserSignedIn() {
        // Arrange
        when(mockFirebaseAuth.getCurrentUser()).thenReturn(mockFirebaseUser);

        // Act
        boolean result = usersFirebaseReference.isCurrentUserSignedIn();

        // Assert
        assertTrue(result);
    }

    @Test
    public void testIsCurrentUserSignedIn_UserNotSignedIn() {
        // Arrange
        when(mockFirebaseAuth.getCurrentUser()).thenReturn(null);

        // Act
        boolean result = usersFirebaseReference.isCurrentUserSignedIn();

        // Assert
        assertFalse(result);
    }

    @Test
    public void testGetUserProfileWithUid() {
        // Arrange
        String userUid = "testUid";
        DatabaseReference mockChildReference = mock(DatabaseReference.class);
        when(mockUsersCollection.child(userUid)).thenReturn(mockChildReference);
        when(mockChildReference.get()).thenReturn(mockDataTask);

        // Act
        Task<DataSnapshot> result = usersFirebaseReference.getUserProfileWithUid(userUid);

        // Assert
        assertNotNull(result);
        verify(mockUsersCollection).child(userUid);
    }

    @Test
    public void testGetUserProfileWithEmail() {
        // Arrange
        DatabaseReference mockOrderedQuery = mock(DatabaseReference.class);
        Query mockQuery = mock(Query.class);

        // Make sure to set up the mockQuery properly
        when(mockUsersCollection.orderByChild(USER_EMAIL_FIELD)).thenReturn(mockQuery);
        when(mockQuery.equalTo(TEST_EMAIL)).thenReturn(mockOrderedQuery);
        when(mockOrderedQuery.get()).thenReturn(mockDataTask);

        // Act
        Task<DataSnapshot> result = usersFirebaseReference.getUserProfileWithEmail(TEST_EMAIL);

        // Assert
        assertNotNull(result);
        verify(mockUsersCollection).orderByChild(USER_EMAIL_FIELD);
        verify(mockQuery).equalTo(TEST_EMAIL);
    }


    @Test
    public void testUpdateUserProfile() {
        // Arrange
        UserProfileModel userProfileModel = new UserProfileModel();
        userProfileModel.setUserUid(TEST_UID);

        DatabaseReference mockChildReference = mock(DatabaseReference.class);
        when(mockUsersCollection.child(TEST_UID)).thenReturn(mockChildReference);
        when(mockChildReference.setValue(userProfileModel)).thenReturn(mockVoidTask);

        // Act
        Task<Void> result = usersFirebaseReference.updateUserProfile(userProfileModel);

        // Assert
        assertNotNull(result);
        verify(mockUsersCollection).child(TEST_UID);
    }

    @Test
    public void testGetCurrentUserProfile_UserSignedIn() {
        // Arrange
        when(mockFirebaseAuth.getCurrentUser()).thenReturn(mockFirebaseUser);
        when(mockFirebaseUser.getUid()).thenReturn(TEST_UID);
        doReturn(mockDataTask).when(usersFirebaseReference).getUserProfileWithUid(TEST_UID);

        // Act
        Task<DataSnapshot> result = usersFirebaseReference.getCurrentUserProfile();

        // Assert
        assertNotNull(result);
        verify(usersFirebaseReference).getUserProfileWithUid(TEST_UID);
    }

    @Test
    public void testGetCurrentUserProfile_UserNotSignedIn() {
        // Arrange
        when(mockFirebaseAuth.getCurrentUser()).thenReturn(null);

        // Act
        Task<DataSnapshot> result = usersFirebaseReference.getCurrentUserProfile();

        // Assert
        assertNull(result);
    }

    @Test
    public void testSignOut() {
        // Act
        usersFirebaseReference.signOut();

        // Assert
        verify(mockFirebaseAuth).signOut();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testDeleteUser_Success() {
        // Arrange
        doReturn(mockAuthCredential).when(usersFirebaseReference).getCredential(TEST_EMAIL,
                                                                                TEST_PASSWORD);
        when(mockFirebaseUser.reauthenticate(mockAuthCredential)).thenReturn(mockVoidTask);

        when(mockVoidTask.addOnCompleteListener(any())).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            ((OnCompleteListener<Void>) args[0]).onComplete(mockVoidTask);
            return null;
        });
        when(mockVoidTask.isSuccessful()).thenReturn(true);

        when(mockFirebaseAuth.getCurrentUser()).thenReturn(mockFirebaseUser);
        when(mockFirebaseUser.getEmail()).thenReturn(TEST_EMAIL);
        when(mockFirebaseUser.getUid()).thenReturn(TEST_UID);
        when(mockFirebaseUser.delete()).thenReturn(mockVoidTask);

        DatabaseReference userRef = mock(DatabaseReference.class);
        when(mockUsersCollection.child(TEST_UID)).thenReturn(userRef);
        when(userRef.removeValue()).thenReturn(mockVoidTask);

        DataWrapper<Boolean> successful = new DataWrapper<>(false);
        DataWrapper<Boolean> failure = new DataWrapper<>(false);

        Runnable onSuccess = () -> successful.set(true);
        Runnable onFailure = () -> failure.set(true);

        // Act
        String result = usersFirebaseReference.deleteUser(TEST_PASSWORD, onSuccess, onFailure);

        // Assert
        assertEquals(TEST_UID, result);
        verify(userRef).removeValue();
        Boolean successfulResult = successful.get();
        assertNotNull(successfulResult);
        assertTrue(successfulResult);
        Boolean failureResult = failure.get();
        assertNotNull(failureResult);
        assertFalse(failureResult);
    }

    @Test(expected = IllegalStateException.class)
    public void testDeleteUser_UserNotSignedIn() {
        // Arrange
        when(mockFirebaseAuth.getCurrentUser()).thenReturn(null);

        // Assert
        usersFirebaseReference.deleteUser(TEST_PASSWORD, null, null);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testChangeUserPassword_Success() {
        // Arrange
        DataWrapper<String> newPasswordWrapper = new DataWrapper<>(null);

        when(mockVoidTask.isSuccessful()).thenReturn(true);
        when(mockVoidTask.addOnCompleteListener(any())).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            ((OnCompleteListener<Void>) args[0]).onComplete(mockVoidTask);
            return null;
        });

        when(mockFirebaseAuth.getCurrentUser()).thenReturn(mockFirebaseUser);

        when(mockFirebaseUser.getEmail()).thenReturn(TEST_EMAIL);
        when(mockFirebaseUser.getUid()).thenReturn(TEST_UID);
        when(mockFirebaseUser.updatePassword(TEST_NEW_PASSWORD)).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            newPasswordWrapper.set((String) args[0]);
            return mockVoidTask;
        });

        doReturn(mockAuthCredential).when(usersFirebaseReference)
                .getCredential(TEST_EMAIL, TEST_PASSWORD);
        when(mockFirebaseUser.reauthenticate(mockAuthCredential)).thenReturn(mockVoidTask);

        DataWrapper<Boolean> successful = new DataWrapper<>(false);
        DataWrapper<Boolean> failure = new DataWrapper<>(false);
        Runnable onSuccess = () -> successful.set(true);
        Runnable onFailure = () -> failure.set(true);

        // Act
        usersFirebaseReference.changeUserPassword(TEST_PASSWORD, TEST_NEW_PASSWORD,
                                                  onSuccess, onFailure);

        // Assert
        verify(mockFirebaseUser).updatePassword(TEST_NEW_PASSWORD);
        String newPassword = newPasswordWrapper.get();
        assertNotNull(newPassword);
        assertEquals(newPassword, TEST_NEW_PASSWORD);
        Boolean successfulResult = successful.get();
        assertNotNull(successfulResult);
        assertTrue(successfulResult);
        Boolean failureResult = failure.get();
        assertNotNull(failureResult);
        assertFalse(failureResult);
    }

    @Test(expected = IllegalStateException.class)
    public void testChangeUserPassword_UserNotSignedIn() {
        // Arrange
        when(mockFirebaseAuth.getCurrentUser()).thenReturn(null);

        // Act
        usersFirebaseReference.changeUserPassword(TEST_PASSWORD, TEST_NEW_PASSWORD, null, null);
    }


    @SuppressWarnings("unchecked")
    private void setUpTaskGetUserProfileWithEmail(boolean existsValue, boolean successfulValue) {
        doReturn(mockDataTask).when(usersFirebaseReference).getUserProfileWithEmail(any());
        when(mockDataTask.addOnCompleteListener(any())).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            ((OnCompleteListener<DataSnapshot>) args[0]).onComplete(mockDataTask);
            return null;
        });
        DataSnapshot mockDataSnapshot = mock(DataSnapshot.class);
        when(mockDataSnapshot.exists()).thenReturn(existsValue);
        when(mockDataTask.getResult()).thenReturn(mockDataSnapshot);
        when(mockDataTask.isSuccessful()).thenReturn(successfulValue);
    }

    @SuppressWarnings("unchecked")
    private void setUpCreateUserTask(boolean successfulValue) {
        when(mockAuthTask.addOnCompleteListener(any())).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            ((OnCompleteListener<AuthResult>) args[0]).onComplete(mockAuthTask);
            return null;
        });
        when(mockAuthTask.isSuccessful()).thenReturn(successfulValue);

        when(mockFirebaseAuth.createUserWithEmailAndPassword(any(), any())).thenReturn(mockAuthTask);
        when(mockFirebaseAuth.getCurrentUser()).thenReturn(mockFirebaseUser);
        when(mockFirebaseUser.getUid()).thenReturn(UsersFirebaseReferenceTest.TEST_UID);
        when(mockFirebaseUser.getEmail()).thenReturn(UsersFirebaseReferenceTest.TEST_EMAIL);
    }

    private void setUpInsertUserIntoDatabase(DataWrapper<UserProfileModel> userProfileDataWrapper) {
        DatabaseReference childUserCollection = mock(DatabaseReference.class);
        when(mockUsersCollection.child(any())).thenReturn(childUserCollection);
        when(childUserCollection.setValue(any())).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            userProfileDataWrapper.set((UserProfileModel) args[0]);
            return null;
        });
    }

}
