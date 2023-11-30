package edu.uga.cs.shopsync.backend.firebase;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static edu.uga.cs.shopsync.backend.firebase.ShopSyncsFirebaseReference.NAME_FIELD;
import static edu.uga.cs.shopsync.backend.firebase.ShopSyncsFirebaseReference.PURCHASED_ITEMS_NESTED_COLLECTION;
import static edu.uga.cs.shopsync.backend.firebase.ShopSyncsFirebaseReference.SHOPPING_BASKETS_NESTED_COLLECTION;
import static edu.uga.cs.shopsync.backend.firebase.ShopSyncsFirebaseReference.SHOPPING_ITEMS_NESTED_COLLECTION;
import static edu.uga.cs.shopsync.backend.firebase.ShopSyncsFirebaseReference.USER_UID_FIELD;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import edu.uga.cs.shopsync.backend.models.BasketItemModel;
import edu.uga.cs.shopsync.backend.models.PurchasedItemModel;
import edu.uga.cs.shopsync.backend.models.ShopSyncModel;
import edu.uga.cs.shopsync.backend.models.ShoppingBasketModel;
import edu.uga.cs.shopsync.backend.models.ShoppingItemModel;
import edu.uga.cs.shopsync.utils.DataWrapper;
import edu.uga.cs.shopsync.utils.ErrorHandle;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class ShopSyncsFirebaseReferenceTest {

    private static final String TEST_NAME = "Test Shop Sync";
    private static final String TEST_DESCRIPTION = "Test Description";
    private static final String TEST_SHOP_SYNC_UID = "TEST_SHOP_SYNC_UID";
    private static final String TEST_USER_UID = "testUserUid";
    private static final String TEST_SHOPPING_ITEM_UID = "testItemUid";
    private static final String TEST_PURCHASED_ITEM_UID = "testPurchasedItemUid";

    private DatabaseReference mockShopSyncsCollection;
    private DatabaseReference mockShopSyncChildReference;

    private DatabaseReference mockShoppingItemsCollection;
    private DatabaseReference mockShoppingItemChildReference;

    private DatabaseReference mockShoppingBasketsCollection;
    private DatabaseReference mockShoppingBasketChildReference;

    private DatabaseReference mockPurchasedItemsCollection;
    private DatabaseReference mockPurchasedItemChildReference;

    private DatabaseReference mockNewEntryReference;
    private Task<DataSnapshot> mockDataTask;
    private Task<Void> mockVoidTask;
    private ShopSyncsFirebaseReference shopSyncsFirebaseReference;

    @Before

    public void setUp() {
        // set up mock shop syncs collection
        mockShopSyncsCollection = mock(DatabaseReference.class);
        mockShopSyncChildReference = mock(DatabaseReference.class);
        when(mockShopSyncsCollection.child(TEST_SHOP_SYNC_UID))
                .thenReturn(mockShopSyncChildReference);

        // set up mock shopping items collection and shopping item child reference
        mockShoppingItemsCollection = mock(DatabaseReference.class);
        when(mockShopSyncChildReference.child(
                SHOPPING_ITEMS_NESTED_COLLECTION))
                .thenReturn(mockShoppingItemsCollection);
        mockShoppingItemChildReference = mock(DatabaseReference.class);
        when(mockShoppingItemsCollection.child(TEST_SHOPPING_ITEM_UID))
                .thenReturn(mockShoppingItemChildReference);

        // set up mock shopping baskets collection and shopping basket child reference
        mockShoppingBasketsCollection = mock(DatabaseReference.class);
        when(mockShopSyncChildReference.child(
                SHOPPING_BASKETS_NESTED_COLLECTION))
                .thenReturn(mockShoppingBasketsCollection);
        mockShoppingBasketChildReference = mock(DatabaseReference.class);
        when(mockShoppingBasketsCollection.child(TEST_USER_UID))
                .thenReturn(mockShoppingBasketChildReference);

        // set up mock purchased items collection and purchased item child reference
        mockPurchasedItemsCollection = mock(DatabaseReference.class);
        when(mockShopSyncChildReference.child(
                PURCHASED_ITEMS_NESTED_COLLECTION))
                .thenReturn(mockPurchasedItemsCollection);
        mockPurchasedItemChildReference = mock(DatabaseReference.class);
        when(mockPurchasedItemsCollection.child(TEST_PURCHASED_ITEM_UID))
                .thenReturn(mockPurchasedItemChildReference);

        // other mocks
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
        when(mockNewEntryReference.getKey()).thenReturn(TEST_SHOPPING_ITEM_UID);

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
        assertEquals(TEST_SHOPPING_ITEM_UID, result.getShoppingItemUid());
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
                     .child(TEST_SHOPPING_ITEM_UID)).thenReturn(mockShoppingItemChildReference);
        when(mockShoppingItemChildReference.get()).thenReturn(mockDataTask);

        // Act
        Task<DataSnapshot> result =
                shopSyncsFirebaseReference.getShoppingItemWithUid(
                        TEST_SHOP_SYNC_UID, TEST_SHOPPING_ITEM_UID);

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

    @Test
    public void testGetShoppingItemsWithName() {
        // Arrange
        String testName = "Test Item";

        Query mockOrderByChildQuery = mock(Query.class);
        when(mockShoppingItemsCollection.orderByChild(NAME_FIELD))
                .thenReturn(mockOrderByChildQuery);

        Query mockEqualToQuery = mock(Query.class);
        when(mockOrderByChildQuery.equalTo(testName)).thenReturn(mockEqualToQuery);

        when(mockEqualToQuery.get()).thenReturn(mockDataTask);

        // Act
        Task<DataSnapshot> result = shopSyncsFirebaseReference.getShoppingItemsWithName(
                TEST_SHOP_SYNC_UID, testName);

        // Assert
        assertNotNull(result);
        verify(mockOrderByChildQuery).equalTo(testName);
        verify(mockEqualToQuery).get();
    }

    @Test
    public void testUpdateShoppingItem() {
        // Arrange
        ShoppingItemModel updatedShoppingItem = new ShoppingItemModel(
                TEST_SHOPPING_ITEM_UID, "Updated Item", true);

        when(mockShoppingItemsCollection.updateChildren(anyMap())).thenReturn(mockVoidTask);

        // Act
        Task<Void> result = shopSyncsFirebaseReference.updateShoppingItem(
                TEST_SHOP_SYNC_UID, updatedShoppingItem);

        // Assert
        assertNotNull(result);
        verify(mockShoppingItemsCollection).updateChildren(anyMap());
    }

    @Test
    public void testDeleteShoppingItem() {
        // Arrange
        when(mockShoppingItemChildReference.removeValue()).thenReturn(mockVoidTask);

        // Act
        Task<Void> result = shopSyncsFirebaseReference.deleteShoppingItem(
                TEST_SHOP_SYNC_UID, TEST_SHOPPING_ITEM_UID);

        // Assert
        assertNotNull(result);
        verify(mockShoppingItemsCollection).child(TEST_SHOPPING_ITEM_UID);
        verify(mockShoppingItemChildReference).removeValue();
    }

    @Test

    public void testCheckIfShoppingBasketExists() {
        // Arrange
        DataSnapshot mockDataSnapshot = mock(DataSnapshot.class);
        when(mockDataSnapshot.exists()).thenReturn(true);

        Task<DataSnapshot> shoppingBasketDataTask = mock(Task.class);
        when(shoppingBasketDataTask.getResult()).thenReturn(mockDataSnapshot);
        when(shoppingBasketDataTask.addOnCompleteListener(any())).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            ((OnCompleteListener<DataSnapshot>) args[0]).onComplete(shoppingBasketDataTask);
            return null;
        });
        when(mockShoppingBasketChildReference.get()).thenReturn(shoppingBasketDataTask);
        when(shoppingBasketDataTask.isSuccessful()).thenReturn(true);

        Consumer<Boolean> mockResultConsumer = mock(Consumer.class);

        // Act
        shopSyncsFirebaseReference.checkIfShoppingBasketExists(
                TEST_SHOP_SYNC_UID, TEST_USER_UID, mockResultConsumer);

        // Assert
        verify(mockShoppingBasketsCollection).child(TEST_USER_UID);
        verify(mockShoppingBasketChildReference).get();
        verify(mockResultConsumer).accept(true);
    }

    @Test
    public void testAddShoppingBasket() {
        // Arrange
        when(mockShoppingBasketChildReference.setValue(any())).thenReturn(mockVoidTask);

        // Act
        ShoppingBasketModel result = shopSyncsFirebaseReference.addShoppingBasket(
                TEST_SHOP_SYNC_UID, TEST_USER_UID);

        // Assert
        assertNotNull(result);
        verify(mockShoppingBasketsCollection).child(TEST_USER_UID);
        verify(mockShoppingBasketChildReference).setValue(result);
    }

    @Test
    public void testGetShoppingBasketWithUid() {
        // Arrange
        when(mockShoppingBasketChildReference.get()).thenReturn(mockDataTask);

        // Act
        Task<DataSnapshot> result = shopSyncsFirebaseReference.getShoppingBasketWithUid(
                TEST_SHOP_SYNC_UID, TEST_USER_UID);

        // Assert
        assertNotNull(result);
        verify(mockShoppingBasketsCollection).child(TEST_USER_UID);
        verify(mockShoppingBasketChildReference).get();
    }

    @Test
    public void testUpdateShoppingBasket() {
        // Arrange
        ShoppingBasketModel updatedShoppingBasket = new ShoppingBasketModel(TEST_USER_UID,
                                                                            new HashMap<>());
        when(mockShoppingBasketsCollection.updateChildren(anyMap())).thenReturn(mockVoidTask);

        // Act
        Task<Void> result = shopSyncsFirebaseReference.updateShoppingBasket(TEST_SHOP_SYNC_UID,
                                                                            updatedShoppingBasket);

        // Assert
        assertNotNull(result);
        Map<String, Object> expectedMap = Map.of("/testUserUid", updatedShoppingBasket.toMap());
        verify(mockShoppingBasketsCollection).updateChildren(expectedMap);
    }

    @Test

    public void testDeleteShoppingBasket() {
        // Arrange
        BasketItemModel basketItem = new BasketItemModel(
                TEST_USER_UID, TEST_SHOPPING_ITEM_UID, 1, 10.0);

        ShoppingBasketModel shoppingBasket = new ShoppingBasketModel(
                TEST_USER_UID, Map.of(TEST_SHOPPING_ITEM_UID, basketItem));

        Task<DataSnapshot> getShoppingBasketTask = mock(Task.class);
        doReturn(getShoppingBasketTask).when(shopSyncsFirebaseReference).getShoppingBasketWithUid(
                TEST_SHOP_SYNC_UID, TEST_USER_UID);
        DataSnapshot getShoppingBasketData = mock(DataSnapshot.class);
        when(getShoppingBasketData.getValue(ShoppingBasketModel.class)).thenReturn(shoppingBasket);
        when(getShoppingBasketTask.addOnSuccessListener(any())).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            ((OnSuccessListener<DataSnapshot>) args[0]).onSuccess(getShoppingBasketData);
            return null;
        });

        DataWrapper<Boolean> setInBasketWrapper = new DataWrapper<>(true);
        DatabaseReference inBasketRef = mock(DatabaseReference.class);
        when(inBasketRef.setValue(anyBoolean())).thenAnswer(invocation -> {
            setInBasketWrapper.set(invocation.getArgument(0));
            return null;
        });
        when(mockShoppingItemChildReference.child("inBasket")).thenReturn(inBasketRef);

        when(mockShoppingBasketChildReference.removeValue()).thenReturn(mockVoidTask);
        when(mockVoidTask.addOnCompleteListener(any())).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            ((OnCompleteListener<Void>) args[0]).onComplete(mockVoidTask);
            return null;
        });
        when(mockVoidTask.isSuccessful()).thenReturn(true);

        Runnable onSuccess = mock(Runnable.class);
        Consumer<ErrorHandle> onFailure = mock(Consumer.class);

        // Act
        shopSyncsFirebaseReference.deleteShoppingBasket(
                TEST_SHOP_SYNC_UID, TEST_USER_UID, onSuccess, onFailure);

        // Assert
        verify(mockShoppingBasketsCollection).child(TEST_USER_UID);
        verify(mockShoppingBasketChildReference).removeValue();
        assertEquals(false, setInBasketWrapper.get());
        verify(onSuccess).run();
        verify(onFailure, times(0)).accept(any(ErrorHandle.class));
    }

    @Test

    public void testAddBasketItem() {
        // Arrange
        ShoppingBasketModel shoppingBasket = new ShoppingBasketModel(
                TEST_USER_UID, new HashMap<>());

        Task<DataSnapshot> getShoppingBasketTask = mock(Task.class);
        doReturn(getShoppingBasketTask).when(shopSyncsFirebaseReference).getShoppingBasketWithUid(
                TEST_SHOP_SYNC_UID, TEST_USER_UID);
        DataSnapshot getShoppingBasketData = mock(DataSnapshot.class);
        when(getShoppingBasketTask.isSuccessful()).thenReturn(true);
        when(getShoppingBasketData.getValue(ShoppingBasketModel.class)).thenReturn(shoppingBasket);
        when(getShoppingBasketTask.getResult()).thenReturn(getShoppingBasketData);
        when(getShoppingBasketTask.addOnCompleteListener(any())).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            ((OnCompleteListener<DataSnapshot>) args[0]).onComplete(getShoppingBasketTask);
            return null;
        });

        DataWrapper<String> shopSyncUidWrapper = new DataWrapper<>(null);
        DataWrapper<ShoppingBasketModel> shoppingBasketWrapper = new DataWrapper<>(null);
        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            shopSyncUidWrapper.set((String) args[0]);
            shoppingBasketWrapper.set((ShoppingBasketModel) args[1]);
            return null;
        }).when(shopSyncsFirebaseReference).updateShoppingBasket(any(), any());

        DataWrapper<Boolean> setInBasketWrapper = new DataWrapper<>(true);
        DatabaseReference inBasketRef = mock(DatabaseReference.class);
        when(inBasketRef.setValue(anyBoolean())).thenAnswer(invocation -> {
            setInBasketWrapper.set(invocation.getArgument(0));
            return null;
        });
        when(mockShoppingItemChildReference.child("inBasket")).thenReturn(inBasketRef);

        Consumer<BasketItemModel> mockOnSuccess = mock(Consumer.class);

        long quantity = 2;
        double pricePerUnit = 10.0;

        // Act
        shopSyncsFirebaseReference.addBasketItem(
                TEST_SHOP_SYNC_UID, TEST_USER_UID, TEST_SHOPPING_ITEM_UID, quantity, pricePerUnit,
                mockOnSuccess, null);

        // Assert
        verify(mockOnSuccess).accept(any(BasketItemModel.class));

        ShoppingBasketModel updatedShoppingBasket = shoppingBasketWrapper.get();
        assertNotNull(updatedShoppingBasket);

        BasketItemModel basketItem =
                updatedShoppingBasket.getBasketItems().get(TEST_SHOPPING_ITEM_UID);
        assertNotNull(basketItem);
        assertEquals(quantity, basketItem.getQuantity());
        assertEquals(pricePerUnit, basketItem.getPricePerUnit(), 0.0);

        assertEquals(TEST_SHOP_SYNC_UID, shopSyncUidWrapper.get());
        assertEquals(true, setInBasketWrapper.get());
    }

    @Test

    public void testDeleteBasketItem() {
        // Arrange
        Map<String, BasketItemModel> basketItems = new HashMap<>();
        basketItems.put(TEST_SHOPPING_ITEM_UID, new BasketItemModel(
                TEST_USER_UID, TEST_SHOPPING_ITEM_UID, 1, 10.0));

        ShoppingBasketModel shoppingBasket = new ShoppingBasketModel(TEST_USER_UID, basketItems);

        DataSnapshot mockDataSnapshot = mock(DataSnapshot.class);
        when(mockDataSnapshot.getValue(ShoppingBasketModel.class)).thenReturn(shoppingBasket);

        Task<DataSnapshot> mockShoppingBasketDataTask = mock(Task.class);
        when(mockShoppingBasketDataTask.isSuccessful()).thenReturn(true);
        when(mockShoppingBasketDataTask.getResult()).thenReturn(mockDataSnapshot);
        when(mockShoppingBasketDataTask.addOnCompleteListener(any())).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            ((OnCompleteListener<DataSnapshot>) args[0]).onComplete(mockShoppingBasketDataTask);
            return null;
        });

        doReturn(mockShoppingBasketDataTask).when(shopSyncsFirebaseReference)
                .getShoppingBasketWithUid(TEST_SHOP_SYNC_UID, TEST_USER_UID);

        DataWrapper<String> shopSyncUidWrapper = new DataWrapper<>(null);
        DataWrapper<ShoppingBasketModel> shoppingBasketWrapper = new DataWrapper<>(null);
        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            shopSyncUidWrapper.set((String) args[0]);
            shoppingBasketWrapper.set((ShoppingBasketModel) args[1]);
            return null;
        }).when(shopSyncsFirebaseReference).updateShoppingBasket(any(), any());

        ShoppingItemModel shoppingItem = new ShoppingItemModel(
                TEST_SHOPPING_ITEM_UID, "Test Item", true);

        DataSnapshot shoppingItemDataSnapshot = mock(DataSnapshot.class);
        when(shoppingItemDataSnapshot.getValue(ShoppingItemModel.class)).thenReturn(shoppingItem);

        Task<DataSnapshot> shoppingItemDataTask = mock(Task.class);
        when(shoppingItemDataTask.isSuccessful()).thenReturn(true);
        when(shoppingItemDataTask.getResult()).thenReturn(shoppingItemDataSnapshot);
        when(shoppingItemDataTask.addOnCompleteListener(any())).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            ((OnCompleteListener<DataSnapshot>) args[0]).onComplete(shoppingItemDataTask);
            return null;
        });

        doReturn(shoppingItemDataTask).when(shopSyncsFirebaseReference).getShoppingItemWithUid(
                TEST_SHOP_SYNC_UID, TEST_SHOPPING_ITEM_UID);

        DataWrapper<ShoppingItemModel> shoppingItemDataWrapper = new DataWrapper<>(null);
        doAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            shoppingItemDataWrapper.set((ShoppingItemModel) args[1]);
            return null;
        }).when(shopSyncsFirebaseReference).updateShoppingItem(any(), any());

        // Act
        shopSyncsFirebaseReference.deleteBasketItem(
                TEST_SHOP_SYNC_UID, TEST_USER_UID, TEST_SHOPPING_ITEM_UID, null, true);

        // Assert
        ShoppingBasketModel updatedShoppingBasket = shoppingBasketWrapper.get();
        assertNotNull(updatedShoppingBasket);
        assertTrue(basketItems.isEmpty());

        ShoppingItemModel updatedShoppingItem = shoppingItemDataWrapper.get();
        assertNotNull(updatedShoppingItem);
        assertFalse(updatedShoppingItem.isInBasket());

        assertEquals(TEST_SHOP_SYNC_UID, shopSyncUidWrapper.get());
    }


    @Test

    public void testCheckIfPurchasedItemExistsForBasketItem() {
        // Arrange
        Query mockOrderByChildQuery = mock(Query.class);
        when(mockPurchasedItemsCollection.orderByChild("basketItem/uid"))
                .thenReturn(mockOrderByChildQuery);

        Query mockEqualToQuery = mock(Query.class);
        when(mockOrderByChildQuery.equalTo(TEST_PURCHASED_ITEM_UID)).thenReturn(mockEqualToQuery);

        when(mockEqualToQuery.get()).thenReturn(mockDataTask);
        when(mockDataTask.isSuccessful()).thenReturn(true);
        when(mockDataTask.getResult()).thenReturn(mock(DataSnapshot.class));
        when(mockDataTask.addOnCompleteListener(any())).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            ((OnCompleteListener<DataSnapshot>) args[0]).onComplete(mockDataTask);
            return null;
        });

        Consumer<Boolean> mockResultConsumer = mock(Consumer.class);

        // Act
        shopSyncsFirebaseReference.checkIfPurchasedItemExistsForBasketItem(
                TEST_SHOP_SYNC_UID, TEST_PURCHASED_ITEM_UID, mockResultConsumer);

        // Assert
        verify(mockPurchasedItemsCollection).orderByChild("basketItem/uid");
        verify(mockResultConsumer).accept(false);
    }

    @Test

    public void testCheckIfPurchasedItemExistsForShoppingItem() {
        // Arrange
        Query mockOrderByChildQuery = mock(Query.class);
        when(mockPurchasedItemsCollection.orderByChild("shoppingItem/uid"))
                .thenReturn(mockOrderByChildQuery);

        Query mockEqualToQuery = mock(Query.class);
        when(mockOrderByChildQuery.equalTo(TEST_SHOPPING_ITEM_UID)).thenReturn(mockEqualToQuery);

        when(mockEqualToQuery.get()).thenReturn(mockDataTask);
        when(mockDataTask.isSuccessful()).thenReturn(true);
        when(mockDataTask.getResult()).thenReturn(mock(DataSnapshot.class));
        when(mockDataTask.addOnCompleteListener(any())).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            ((OnCompleteListener<DataSnapshot>) args[0]).onComplete(mockDataTask);
            return null;
        });

        Consumer<Boolean> mockResultConsumer = mock(Consumer.class);

        // Act
        shopSyncsFirebaseReference.checkIfPurchasedItemExistsForShoppingItem(
                TEST_SHOP_SYNC_UID, TEST_SHOPPING_ITEM_UID, mockResultConsumer);

        // Assert
        verify(mockPurchasedItemsCollection).orderByChild("shoppingItem/uid");
        verify(mockResultConsumer).accept(false);
    }

    @Test
    public void testGetPurchasedItemWithUid() {
        // Arrange
        when(mockPurchasedItemChildReference.get()).thenReturn(mockDataTask);

        // Act
        Task<DataSnapshot> result =
                shopSyncsFirebaseReference.getPurchasedItemWithUid(TEST_SHOP_SYNC_UID,
                                                                   TEST_PURCHASED_ITEM_UID);

        // Assert
        assertNotNull(result);
        verify(mockPurchasedItemsCollection).child(TEST_PURCHASED_ITEM_UID);
        verify(mockPurchasedItemChildReference).get();
    }

    @Test
    public void testGetPurchasedItemsWithShopSyncUid() {
        // Arrange
        when(mockPurchasedItemsCollection.get()).thenReturn(mockDataTask);

        // Act
        Task<DataSnapshot> result =
                shopSyncsFirebaseReference.getPurchasedItemsWithShopSyncUid(TEST_SHOP_SYNC_UID);

        // Assert
        assertNotNull(result);
        verify(mockPurchasedItemsCollection).get();
    }

    @Test
    public void testGetPurchasedItemsWithUserUid() {
        // Arrange
        Query mockOrderByChildQuery = mock(Query.class);
        when(mockPurchasedItemsCollection.orderByChild(USER_UID_FIELD))
                .thenReturn(mockOrderByChildQuery);

        Query mockEqualToQuery = mock(Query.class);
        when(mockOrderByChildQuery.equalTo(TEST_USER_UID)).thenReturn(mockEqualToQuery);

        when(mockEqualToQuery.get()).thenReturn(mockDataTask);

        // Act
        Task<DataSnapshot> result = shopSyncsFirebaseReference.getPurchasedItemsWithUserUid(
                TEST_SHOP_SYNC_UID, TEST_USER_UID);

        // Assert
        assertNotNull(result);
        verify(mockPurchasedItemsCollection).orderByChild(USER_UID_FIELD);
        verify(mockOrderByChildQuery).equalTo(TEST_USER_UID);
        verify(mockEqualToQuery).get();
        assertEquals(mockDataTask, result);
    }

    @Test
    public void testUpdatePurchasedItem() {
        // Arrange
        PurchasedItemModel updatedPurchasedItem = new PurchasedItemModel(
                TEST_PURCHASED_ITEM_UID, null, null, null);
        when(mockPurchasedItemsCollection.updateChildren(anyMap())).thenReturn(mockVoidTask);

        // Act
        Task<Void> result = shopSyncsFirebaseReference.updatePurchasedItem(
                TEST_SHOP_SYNC_UID, updatedPurchasedItem);

        // Assert
        assertNotNull(result);
        Map<String, Object> expectedMap = Map.of("/" + TEST_PURCHASED_ITEM_UID,
                                                 updatedPurchasedItem.toMap());
        verify(mockPurchasedItemsCollection).updateChildren(expectedMap);
    }

    @Test
    public void testDeletePurchasedItem() {
        // Arrange
        when(mockPurchasedItemChildReference.removeValue()).thenReturn(mockVoidTask);

        // Act
        Task<Void> result = shopSyncsFirebaseReference.deletePurchasedItem(
                TEST_SHOP_SYNC_UID, TEST_PURCHASED_ITEM_UID);

        // Assert
        assertNotNull(result);
        verify(mockPurchasedItemsCollection).child(TEST_PURCHASED_ITEM_UID);
        verify(mockPurchasedItemChildReference).removeValue();
    }

    @Test
    public void testDeleteAllPurchasedItems() {
        // Arrange
        when(mockPurchasedItemsCollection.removeValue()).thenReturn(mockVoidTask);

        // Act
        Task<Void> result = shopSyncsFirebaseReference.deleteAllPurchasedItems(TEST_SHOP_SYNC_UID);

        // Assert
        assertNotNull(result);
        verify(mockPurchasedItemsCollection).removeValue();
    }
}

