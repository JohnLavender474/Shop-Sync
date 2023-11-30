package edu.uga.cs.shopsync.backend.firebase;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import org.junit.Before;
import org.junit.Test;

import java.util.List;

import edu.uga.cs.shopsync.utils.DataWrapper;

@SuppressWarnings("unchecked")
public class UserShopSyncMapFirebaseReferenceTest {

    private static final String TEST_USER_UID = "testUserUid";
    private static final String TEST_SHOP_SYNC_UID = "testShopSyncUid";

    private UserShopSyncMapFirebaseReference firebaseReference;
    private DatabaseReference mockUserToShopSyncsMapReference;
    private DatabaseReference mockShopSyncToUsersMapReference;

    @Before
    public void setUp() {
        // mocks
        mockUserToShopSyncsMapReference = mock(DatabaseReference.class);
        mockShopSyncToUsersMapReference = mock(DatabaseReference.class);

        // spies
        firebaseReference = spy(new UserShopSyncMapFirebaseReference(
                mockUserToShopSyncsMapReference, mockShopSyncToUsersMapReference));
    }

    @Test
    public void testAddShopSyncToUser() {
        // Arrange
        DatabaseReference mockUserRef = mock(DatabaseReference.class);
        when(mockUserToShopSyncsMapReference.child(TEST_USER_UID)).thenReturn(mockUserRef);

        DatabaseReference mockUserShopSyncRef = mock(DatabaseReference.class);
        when(mockUserRef.child(TEST_SHOP_SYNC_UID)).thenReturn(mockUserShopSyncRef);

        DataWrapper<Boolean> value1DataWrapper = new DataWrapper<>(null);
        when(mockUserShopSyncRef.setValue(anyBoolean())).thenAnswer(invocation -> {
            value1DataWrapper.set((Boolean) invocation.getArguments()[0]);
            return null;
        });

        DatabaseReference mockShopSyncRef = mock(DatabaseReference.class);
        when(mockShopSyncToUsersMapReference.child(TEST_SHOP_SYNC_UID)).thenReturn(mockShopSyncRef);

        DatabaseReference mockShopSyncUserRef = mock(DatabaseReference.class);
        when(mockShopSyncRef.child(TEST_USER_UID)).thenReturn(mockShopSyncUserRef);

        DataWrapper<Boolean> value2DataWrapper = new DataWrapper<>(null);
        when(mockShopSyncUserRef.setValue(anyBoolean())).thenAnswer(invocation -> {
            value2DataWrapper.set((Boolean) invocation.getArguments()[0]);
            return null;
        });

        // Act
        firebaseReference.addShopSyncToUser(TEST_USER_UID, TEST_SHOP_SYNC_UID);

        // Assert
        verify(mockUserShopSyncRef).setValue(true);
        verify(mockShopSyncUserRef).setValue(true);
        Boolean value1 = value1DataWrapper.get();
        assertNotNull(value1);
        assertTrue(value1);
        Boolean value2 = value2DataWrapper.get();
        assertNotNull(value2);
        assertTrue(value2);
    }

    @Test
    public void testGetShopSyncsAssociatedWithUser() {
        // Arrange
        Task<DataSnapshot> mockDataTask = mock(Task.class);

        DatabaseReference mockUserRef = mock(DatabaseReference.class);
        when(mockUserToShopSyncsMapReference.child(TEST_USER_UID)).thenReturn(mockUserRef);

        when(mockUserRef.get()).thenReturn(mockDataTask);

        // Act
        Task<DataSnapshot> resultTask =
                firebaseReference.getShopSyncsAssociatedWithUser(TEST_USER_UID);

        // Assert
        assertSame(mockDataTask, resultTask);
    }

    @Test
    public void testGetUsersAssociatedWithShopSync() {
        // Arrange
        Task<DataSnapshot> mockDataTask = mock(Task.class);

        DatabaseReference mockShopSyncRef = mock(DatabaseReference.class);
        when(mockShopSyncToUsersMapReference.child(TEST_SHOP_SYNC_UID)).thenReturn(mockShopSyncRef);

        when(mockShopSyncRef.get()).thenReturn(mockDataTask);

        // Act
        Task<DataSnapshot> resultTask =
                firebaseReference.getUsersAssociatedWithShopSync(TEST_SHOP_SYNC_UID);

        // Assert
        assertSame(mockDataTask, resultTask);
    }

    @Test
    public void testRemoveUserShopSyncMapping() {
        // Arrange
        DatabaseReference mockUserRef = mock(DatabaseReference.class);
        when(mockUserToShopSyncsMapReference.child(TEST_USER_UID)).thenReturn(mockUserRef);

        DatabaseReference mockUserShopSyncRef = mock(DatabaseReference.class);
        when(mockUserRef.child(TEST_SHOP_SYNC_UID)).thenReturn(mockUserShopSyncRef);

        when(mockUserShopSyncRef.removeValue()).thenReturn(null);

        DatabaseReference mockShopSyncRef = mock(DatabaseReference.class);
        when(mockShopSyncToUsersMapReference.child(TEST_SHOP_SYNC_UID)).thenReturn(mockShopSyncRef);

        DatabaseReference mockShopSyncUserRef = mock(DatabaseReference.class);
        when(mockShopSyncRef.child(TEST_USER_UID)).thenReturn(mockShopSyncUserRef);

        when(mockShopSyncUserRef.removeValue()).thenReturn(null);

        // Act
        firebaseReference.removeUserShopSyncMapping(TEST_USER_UID, TEST_SHOP_SYNC_UID);

        // Assert
        verify(mockUserShopSyncRef).removeValue();
        verify(mockShopSyncUserRef).removeValue();
    }

    @Test
    public void testRemoveUser() {
        // Arrange
        Task<DataSnapshot> mockDataTask = mock(Task.class);
        when(mockDataTask.isSuccessful()).thenReturn(true);
        when(mockDataTask.addOnCompleteListener(any())).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            ((OnCompleteListener<DataSnapshot>) args[0]).onComplete(mockDataTask);
            return null;
        });

        doReturn(mockDataTask).when(firebaseReference)
                .getShopSyncsAssociatedWithUser(TEST_USER_UID);

        DataSnapshot mockChildDataSnapshot = mock(DataSnapshot.class);
        when(mockChildDataSnapshot.getKey()).thenReturn(TEST_SHOP_SYNC_UID);
        List<DataSnapshot> children = List.of(mockChildDataSnapshot);

        DataSnapshot mockDataSnapshot = mock(DataSnapshot.class);
        when(mockDataSnapshot.getChildren()).thenReturn(children);

        when(mockDataTask.getResult()).thenReturn(mockDataSnapshot);

        DataWrapper<String> userUidWrapper = new DataWrapper<>(null);
        DataWrapper<String> shopSyncUidWrapper = new DataWrapper<>(null);
        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            userUidWrapper.set((String) args[0]);
            shopSyncUidWrapper.set((String) args[1]);
            return null;
        }).when(firebaseReference).removeUserShopSyncMapping(TEST_USER_UID, TEST_SHOP_SYNC_UID);

        DatabaseReference userShopSyncRef = mock(DatabaseReference.class);
        when(mockUserToShopSyncsMapReference.child(TEST_USER_UID)).thenReturn(userShopSyncRef);
        when(userShopSyncRef.removeValue()).thenReturn(null);

        // Act
        firebaseReference.removeUser(TEST_USER_UID);

        // Assert
        verify(firebaseReference).removeUserShopSyncMapping(TEST_USER_UID, TEST_SHOP_SYNC_UID);
        String userUid = userUidWrapper.get();
        assertNotNull(userUid);
        assertSame(TEST_USER_UID, userUid);
        String shopSyncUid = shopSyncUidWrapper.get();
        assertNotNull(shopSyncUid);
        assertSame(TEST_SHOP_SYNC_UID, shopSyncUid);
        verify(userShopSyncRef).removeValue();
    }

    @Test
    public void testRemoveShopSync() {
        // Arrange
        Task<DataSnapshot> mockDataTask = mock(Task.class);
        when(mockDataTask.isSuccessful()).thenReturn(true);
        when(mockDataTask.addOnCompleteListener(any())).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            ((OnCompleteListener<DataSnapshot>) args[0]).onComplete(mockDataTask);
            return null;
        });

        doReturn(mockDataTask).when(firebaseReference)
                .getUsersAssociatedWithShopSync(TEST_SHOP_SYNC_UID);

        DataSnapshot mockChildDataSnapshot = mock(DataSnapshot.class);
        when(mockChildDataSnapshot.getKey()).thenReturn(TEST_USER_UID);
        List<DataSnapshot> children = List.of(mockChildDataSnapshot);

        DataSnapshot mockDataSnapshot = mock(DataSnapshot.class);
        when(mockDataSnapshot.getChildren()).thenReturn(children);

        when(mockDataTask.getResult()).thenReturn(mockDataSnapshot);

        DataWrapper<String> userUidWrapper = new DataWrapper<>(null);
        DataWrapper<String> shopSyncUidWrapper = new DataWrapper<>(null);
        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            userUidWrapper.set((String) args[0]);
            shopSyncUidWrapper.set((String) args[1]);
            return null;
        }).when(firebaseReference).removeUserShopSyncMapping(TEST_USER_UID, TEST_SHOP_SYNC_UID);

        DatabaseReference shopSyncUsersRef = mock(DatabaseReference.class);
        when(mockShopSyncToUsersMapReference.child(TEST_SHOP_SYNC_UID)).thenReturn(shopSyncUsersRef);
        when(shopSyncUsersRef.removeValue()).thenReturn(null);

        // Act
        firebaseReference.removeShopSync(TEST_SHOP_SYNC_UID);

        // Assert
        verify(firebaseReference).removeUserShopSyncMapping(TEST_USER_UID, TEST_SHOP_SYNC_UID);
        String userUid = userUidWrapper.get();
        assertNotNull(userUid);
        assertSame(TEST_USER_UID, userUid);
        String shopSyncUid = shopSyncUidWrapper.get();
        assertNotNull(shopSyncUid);
        assertSame(TEST_SHOP_SYNC_UID, shopSyncUid);
        verify(shopSyncUsersRef).removeValue();
    }


}

