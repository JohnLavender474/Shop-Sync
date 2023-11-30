package edu.uga.cs.shopsync.backend.firebase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import edu.uga.cs.shopsync.backend.models.ShopSyncModel;
import edu.uga.cs.shopsync.backend.models.ShoppingItemModel;
import edu.uga.cs.shopsync.utils.DataWrapper;

@RunWith(MockitoJUnitRunner.class)
public class ShopSyncsFirebaseReferenceTest {

    private static final String TEST_NAME = "Test Shop Sync";
    private static final String TEST_DESCRIPTION = "Test Description";
    private static final String TEST_SHOP_SYNC_UID = "testShopSyncUid";
    private static final String TEST_ITEM_UID = "testItemUid";
    private static final String TEST_BASKET_UID = "testBasketUid";
    private static final String TEST_PURCHASED_ITEM_UID = "testPurchasedItemUid";

    private DatabaseReference mockShopSyncsCollection;
    private DatabaseReference mockShopSyncChildReference;

    private DatabaseReference mockShoppingItemsCollection;
    private DatabaseReference mockShoppingItemChildReference;

    private DatabaseReference mockShoppingBasketsCollection;

    private DatabaseReference mockPurchasedItemsCollection;

    private DatabaseReference mockNewEntryReference;
    private Task<DataSnapshot> mockDataTask;
    private Task<Void> mockVoidTask;
    private ShopSyncsFirebaseReference shopSyncsFirebaseReference;

    @Before
    @SuppressWarnings("unchecked")
    public void setUp() {
        // mocks
        mockShopSyncsCollection = mock(DatabaseReference.class);
        mockShopSyncChildReference = mock(DatabaseReference.class);
        when(mockShopSyncsCollection.child(TEST_SHOP_SYNC_UID)).thenReturn(mockShopSyncChildReference);

        mockShoppingItemsCollection = mock(DatabaseReference.class);
        when(mockShopSyncChildReference.child(ShopSyncsFirebaseReference.SHOPPING_ITEMS_NESTED_COLLECTION))
                .thenReturn(mockShoppingItemsCollection);
        mockShoppingItemChildReference = mock(DatabaseReference.class);
        when(mockShoppingItemsCollection.child(TEST_ITEM_UID)).thenReturn(mockShoppingItemChildReference);

        mockShoppingBasketsCollection = mock(DatabaseReference.class);
        when(mockShopSyncChildReference.child(ShopSyncsFirebaseReference.SHOPPING_BASKETS_NESTED_COLLECTION))
                .thenReturn(mockShoppingBasketsCollection);

        mockPurchasedItemsCollection = mock(DatabaseReference.class);
        when(mockShopSyncChildReference.child(ShopSyncsFirebaseReference.PURCHASED_ITEMS_NESTED_COLLECTION))
                .thenReturn(mockPurchasedItemsCollection);

        mockNewEntryReference = mock(DatabaseReference.class);
        mockDataTask = mock(Task.class);
        mockVoidTask = mock(Task.class);

        // spies
        shopSyncsFirebaseReference = spy(new ShopSyncsFirebaseReference(mockShopSyncsCollection));
    }

    @Test
    public void testGetShopSyncsCollection() {
        // Act
        DatabaseReference result = shopSyncsFirebaseReference.getShopSyncsCollection();

        // Assert
        assertEquals(mockShopSyncsCollection, result);
    }

    @Test
    public void testGetShoppingItemsCollection() {
        // Act
        DatabaseReference result =
                shopSyncsFirebaseReference.getShoppingItemsCollection(TEST_SHOP_SYNC_UID);

        // Assert
        verify(mockShopSyncsCollection).child(TEST_SHOP_SYNC_UID);
        assertEquals(mockShoppingItemsCollection, result);
    }

    @Test
    public void testGetShoppingBasketsCollection() {
        // Act
        DatabaseReference result =
                shopSyncsFirebaseReference.getShoppingBasketsCollection(TEST_SHOP_SYNC_UID);

        // Assert
        verify(mockShopSyncsCollection).child(TEST_SHOP_SYNC_UID);
        assertEquals(mockShoppingBasketsCollection, result);
    }

    @Test
    public void testGetPurchasedItemsCollection() {
        // Act
        DatabaseReference result =
                shopSyncsFirebaseReference.getPurchasedItemsCollection(TEST_SHOP_SYNC_UID);

        // Assert
        verify(mockShopSyncsCollection).child(TEST_SHOP_SYNC_UID);
        assertEquals(mockPurchasedItemsCollection, result);
    }

    @Test
    public void testAddShopSync() {
        // Arrange
        ShopSyncModel expectedShopSync = new ShopSyncModel(TEST_SHOP_SYNC_UID, TEST_NAME,
                                                           TEST_DESCRIPTION,
                                                           Collections.emptyMap(),
                                                           Collections.emptyMap(),
                                                           Collections.emptyMap());

        when(mockShopSyncsCollection.push()).thenReturn(mockNewEntryReference);
        when(mockNewEntryReference.getKey()).thenReturn(TEST_SHOP_SYNC_UID);

        // Act
        ShopSyncModel result = shopSyncsFirebaseReference.addShopSync(
                TEST_NAME, TEST_DESCRIPTION, null, null, null);

        // Assert
        assertNotNull(result);
        assertEquals(expectedShopSync, result);
        verify(mockShopSyncsCollection).push();
        verify(mockNewEntryReference).getKey();
        verify(mockShopSyncChildReference).setValue(result);
    }

    @Test
    public void testGetShopSyncWithUid() {
        // Arrange
        when(mockShopSyncsCollection.child(TEST_SHOP_SYNC_UID)).thenReturn(mockShopSyncChildReference);
        when(mockShopSyncChildReference.get()).thenReturn(mockDataTask);

        // Act
        Task<DataSnapshot> result =
                shopSyncsFirebaseReference.getShopSyncWithUid(TEST_SHOP_SYNC_UID);

        // Assert
        assertNotNull(result);
        verify(mockShopSyncsCollection).child(TEST_SHOP_SYNC_UID);
    }

    @Test
    public void testUpdateShopSync() {
        // Arrange
        ShopSyncModel updatedShopSync = new ShopSyncModel(
                TEST_SHOP_SYNC_UID, "Updated Name",
                "Updated Description", Collections.emptyMap(), Collections.emptyMap(),
                Collections.emptyMap());
        when(mockShopSyncsCollection.updateChildren(anyMap())).thenReturn(mockVoidTask);

        // Act
        Task<Void> result = shopSyncsFirebaseReference.updateShopSync(updatedShopSync);

        // Assert
        assertNotNull(result);
        verify(mockShopSyncsCollection).updateChildren(anyMap());
    }

    @Test
    public void testDeleteShopSync() {
        // Arrange
        when(mockShopSyncsCollection.child(TEST_SHOP_SYNC_UID)).thenReturn(mockShopSyncChildReference);
        when(mockShopSyncChildReference.removeValue()).thenReturn(mockVoidTask);

        // Act
        Task<Void> result = shopSyncsFirebaseReference.deleteShopSync(TEST_SHOP_SYNC_UID);

        // Assert
        assertNotNull(result);
        verify(mockShopSyncsCollection).child(TEST_SHOP_SYNC_UID);
        verify(mockShopSyncChildReference).removeValue();
    }

    @Test
    public void testAddShoppingItem() {
        // Arrange
        String testName = "Test Item";
        boolean testInBasket = false;
        when(mockShoppingItemsCollection.push()).thenReturn(mockNewEntryReference);
        when(mockNewEntryReference.getKey()).thenReturn(TEST_ITEM_UID);

        DataWrapper<ShoppingItemModel> actualShoppingItemWrapper = new DataWrapper<>(null);

        when(mockShoppingItemChildReference.setValue(any())).thenAnswer(invocation -> {
            actualShoppingItemWrapper.set(invocation.getArgument(0));
            return null;
        });

        // Act
        ShoppingItemModel result = shopSyncsFirebaseReference.addShoppingItem(
                TEST_SHOP_SYNC_UID, testName, testInBasket);

        // Assert
        assertNotNull(result);
        assertEquals(TEST_ITEM_UID, result.getShoppingItemUid());
        assertEquals(testName, result.getName());
        assertEquals(testInBasket, result.isInBasket());
        verify(mockShoppingItemsCollection).push();
        verify(mockNewEntryReference).getKey();
        verify(mockShoppingItemChildReference).setValue(result);
    }

    @Test
    public void testGetShoppingItemWithUid() {
        // Arrange
        DatabaseReference mockShoppingItemChildReference = mock(DatabaseReference.class);
        when(shopSyncsFirebaseReference.getShoppingItemsCollection(TEST_SHOP_SYNC_UID)
                     .child(TEST_ITEM_UID)).thenReturn(mockShoppingItemChildReference);
        when(mockShoppingItemChildReference.get()).thenReturn(mockDataTask);

        // Act
        Task<DataSnapshot> result =
                shopSyncsFirebaseReference.getShoppingItemWithUid(
                        TEST_SHOP_SYNC_UID, TEST_ITEM_UID);

        // Assert
        assertNotNull(result);
        verify(mockShoppingItemChildReference).get();
    }

    @Test
    public void testGetShoppingItemsWithShopSyncUid() {
        // Arrange
        DatabaseReference mockShoppingItemsReference = mock(DatabaseReference.class);
        when(shopSyncsFirebaseReference.getShoppingItemsCollection(TEST_SHOP_SYNC_UID))
                .thenReturn(mockShoppingItemsReference);
        when(mockShoppingItemsReference.get()).thenReturn(mockDataTask);

        // Act
        Task<DataSnapshot> result =
                shopSyncsFirebaseReference.getShoppingItemsWithShopSyncUid(TEST_SHOP_SYNC_UID);

        // Assert
        assertNotNull(result);
        verify(mockShoppingItemsReference).get();
    }
}

