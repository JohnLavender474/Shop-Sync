package edu.uga.cs.shopsync.backend.services;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.database.ValueEventListener;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.function.Consumer;

import edu.uga.cs.shopsync.backend.firebase.UserShopSyncMapFirebaseReference;
import edu.uga.cs.shopsync.backend.firebase.UsersFirebaseReference;
import edu.uga.cs.shopsync.backend.models.UserProfileModel;
import edu.uga.cs.shopsync.utils.ErrorHandle;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class UsersServiceTest {

    @Mock
    private UsersFirebaseReference mockFirebaseReference;

    @Mock
    private UserShopSyncMapFirebaseReference mockUserShopSyncMapReference;

    @InjectMocks
    private UsersService usersService;

    @Test
    public void testAddValueEventListener() {
        // Arrange
        ValueEventListener mockListener = mock(ValueEventListener.class);

        // Act
        usersService.addValueEventListener(mockListener);

        // Assert
        verify(mockFirebaseReference, times(1)).addValueEventListener(mockListener);
    }

    @Test
    public void testCreateUser() {
        // Arrange
        String email = "test@example.com";
        String username = "testuser";
        String password = "testpassword";
        Consumer<UserProfileModel> mockSuccessConsumer = mock(Consumer.class);
        Consumer<ErrorHandle> mockErrorConsumer = mock(Consumer.class);

        // Act
        try {
            usersService.createUser(email, username, password, mockSuccessConsumer,
                                    mockErrorConsumer);
        } catch (Exception e) {
            fail("Exception not expected");
        }

        // Assert
        verify(mockFirebaseReference, times(1))
                .createUser(email, username, password, mockSuccessConsumer, mockErrorConsumer);
    }

    @Test
    public void testSignInUser() {
        // Arrange
        String email = "test@example.com";
        String password = "testpassword";

        Task<AuthResult> mockTask = mock(Task.class);
        when(mockFirebaseReference.signInUser(email, password)).thenReturn(mockTask);

        // Act
        Task<AuthResult> result = usersService.signInUser(email, password);

        // Assert
        assertNotNull(result);
        assertSame(mockTask, result);
    }

    @Test
    public void testSignOut() {
        // Act
        usersService.signOut();

        // Assert
        verify(mockFirebaseReference, times(1)).signOut();
    }

    @Test
    public void testChangeUserPassword() {
        // Arrange
        String oldPassword = "oldpassword";
        String newPassword = "newpassword";
        Runnable mockUpdatePasswordListener = mock(Runnable.class);
        Runnable mockFailureToAuthenticate = mock(Runnable.class);

        // Act
        usersService.changeUserPassword(oldPassword, newPassword,
                                        mockUpdatePasswordListener, mockFailureToAuthenticate);

        // Assert
        verify(mockFirebaseReference, times(1))
                .changeUserPassword(oldPassword, newPassword, mockUpdatePasswordListener,
                                    mockFailureToAuthenticate);
    }

    @Test
    public void testIsCurrentUserSignedIn() {
        // Arrange
        when(mockFirebaseReference.isCurrentUserSignedIn()).thenReturn(true);

        // Act
        boolean result = usersService.isCurrentUserSignedIn();

        // Assert
        assertTrue(result);
    }

    // Add more tests for the remaining methods...

    @Test
    public void testDeleteUser() {
        // Arrange
        String password = "testpassword";
        Runnable mockSuccessRunnable = mock(Runnable.class);
        Runnable mockFailureRunnable = mock(Runnable.class);
        when(mockFirebaseReference.deleteCurrentUser(
                password, mockSuccessRunnable, mockFailureRunnable)).thenReturn("userUid");

        // Act
        usersService.deleteCurrentUser(password, mockSuccessRunnable, mockFailureRunnable);

        // Assert
        verify(mockFirebaseReference, times(1))
                .deleteCurrentUser(password, mockSuccessRunnable, mockFailureRunnable);
        verify(mockUserShopSyncMapReference, times(1)).removeUser("userUid");
    }
}

