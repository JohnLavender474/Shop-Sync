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

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
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
    private static final String TEST_VALID_PASSWORD = "Password123!";

    private FirebaseAuth mockFirebaseAuth;
    private DatabaseReference mockUsersCollection;
    private FirebaseUser mockFirebaseUser;
    private UsersFirebaseReference usersFirebaseReference;

    @Before
    public void setUp() {
        // mocks
        mockFirebaseAuth = mock(FirebaseAuth.class);
        mockUsersCollection = mock(DatabaseReference.class);
        mockFirebaseUser = mock(FirebaseUser.class);

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
        setUpCreateUserTask(true, TEST_UID, TEST_EMAIL);

        DataWrapper<UserProfileModel> userProfileDataWrapper = new DataWrapper<>();
        setUpInsertUserIntoDatabase(userProfileDataWrapper);

        DataWrapper<Boolean> onSuccessCalled = new DataWrapper<>(false);
        DataWrapper<Boolean> onFailureCalled = new DataWrapper<>(false);

        // Act
        usersFirebaseReference.createUser(TEST_EMAIL, TEST_USERNAME, TEST_VALID_PASSWORD,
                                          userProfileModel -> onSuccessCalled.set(true),
                                          errorHandle -> onFailureCalled.set(true));

        // Assert
        UserProfileModel userProfile = userProfileDataWrapper.get();
        assertEquals(TEST_UID, userProfile.getUserUid());
        assertEquals(TEST_EMAIL, userProfile.getEmail());
        assertEquals(TEST_USERNAME, userProfile.getUsername());
        assertTrue(onSuccessCalled.get());
        assertFalse(onFailureCalled.get());
    }

    @Test
    public void testCreateUser_Failure_TaskToCheckIfUsernameExists() {
        // Arrange
        setUpTaskGetUserProfileWithEmail(false, false);
        setUpCreateUserTask(false, TEST_UID, TEST_EMAIL);

        DataWrapper<UserProfileModel> userProfileDataWrapper = new DataWrapper<>();
        setUpInsertUserIntoDatabase(userProfileDataWrapper);

        DataWrapper<Boolean> onSuccessCalled = new DataWrapper<>(false);
        DataWrapper<ErrorHandle> onFailureCalled = new DataWrapper<>();

        // Act
        usersFirebaseReference.createUser(TEST_EMAIL, TEST_USERNAME, TEST_VALID_PASSWORD,
                                          userProfileModel -> onSuccessCalled.set(true),
                                          onFailureCalled::set);

        // Assert
        assertFalse(onSuccessCalled.get());
        assertNotNull(onFailureCalled.get());
        ErrorHandle errorHandle = onFailureCalled.get();
        assertEquals(ErrorType.TASK_FAILED, errorHandle.errorType());
        assertEquals("Task to check if username exists failed", errorHandle.errorMessage());
        assertNull(userProfileDataWrapper.get());
    }

    @Test
    public void testCreateUser_Failure_UserAlreadyExists() {
        // Arrange
        setUpTaskGetUserProfileWithEmail(true, true);
        setUpCreateUserTask(false, TEST_UID, TEST_EMAIL);

        DataWrapper<UserProfileModel> userProfileDataWrapper = new DataWrapper<>();
        setUpInsertUserIntoDatabase(userProfileDataWrapper);

        DataWrapper<Boolean> onSuccessCalled = new DataWrapper<>(false);
        DataWrapper<ErrorHandle> onFailureCalled = new DataWrapper<>();

        // Act
        usersFirebaseReference.createUser(TEST_EMAIL, TEST_USERNAME, TEST_VALID_PASSWORD,
                                          userProfileModel -> onSuccessCalled.set(true),
                                          onFailureCalled::set);

        // Assert
        assertFalse(onSuccessCalled.get());
        assertNotNull(onFailureCalled.get());
        ErrorHandle errorHandle = onFailureCalled.get();
        assertEquals(ErrorType.ENTITY_ALREADY_EXISTS, errorHandle.errorType());
        assertEquals("User already exists with the email: " + TEST_EMAIL,
                     errorHandle.errorMessage());
        assertTrue(errorHandle.props().containsKey("email"));
        assertEquals(TEST_EMAIL, errorHandle.props().get("email"));
        assertNull(userProfileDataWrapper.get());
    }

    @SuppressWarnings("unchecked")
    private void setUpTaskGetUserProfileWithEmail(boolean existsValue, boolean successfulValue) {
        Task<DataSnapshot> mockDataTask = mock(Task.class);
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
    private void setUpCreateUserTask(boolean successfulValue, String uid, String email) {
        Task<AuthResult> mockAuthTask = mock(Task.class);
        when(mockAuthTask.addOnCompleteListener(any())).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            ((OnCompleteListener<AuthResult>) args[0]).onComplete(mockAuthTask);
            return null;
        });
        when(mockAuthTask.isSuccessful()).thenReturn(successfulValue);

        when(mockFirebaseAuth.createUserWithEmailAndPassword(any(), any())).thenReturn(mockAuthTask);
        when(mockFirebaseAuth.getCurrentUser()).thenReturn(mockFirebaseUser);
        when(mockFirebaseUser.getUid()).thenReturn(uid);
        when(mockFirebaseUser.getEmail()).thenReturn(email);
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
