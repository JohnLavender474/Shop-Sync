package edu.uga.cs.shopsync.backend.firebase;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.verify;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import edu.uga.cs.shopsync.backend.models.UserProfileModel;

public class UsersFirebaseReferenceTest {

    @Mock
    private FirebaseAuth mockFirebaseAuth;

    @Mock
    private DatabaseReference mockUsersCollection;

    private UsersFirebaseReference usersFirebaseReference;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        usersFirebaseReference = new UsersFirebaseReference(mockFirebaseAuth, mockUsersCollection);
    }

    @Test
    public void testCreateUser_Successful() {
        // Arrange
        String email = "test@example.com";
        String username = "testuser";
        String password = "password";

        // Assuming FirebaseAuth.createUserWithEmailAndPassword() and setValue() are successful
        // You may need to adjust this based on your actual implementation

        // Act
        usersFirebaseReference.createUser(email, username, password, userProfile -> {
            // This is the success case, do any assertions or verifications here
            // For example, you can verify that setValue() was called with the correct arguments
            verify(mockUsersCollection).child(anyString()).setValue(any(UserProfileModel.class));
        }, null);

        // Assert
        // You can add assertions or verifications here if needed
    }

    @Test
    public void testCreateUser_UserAlreadyExists() {
        // Arrange
        // Assuming FirebaseAuth.createUserWithEmailAndPassword() fails due to user already existing
        // You may need to adjust this based on your actual implementation

        // Act
        usersFirebaseReference.createUser("existing@example.com", "existinguser", "password",
                                          null, null);

        // Assert
        // You can add assertions or verifications here based on your expected behavior
    }

    // Add more tests for other methods as needed

}

