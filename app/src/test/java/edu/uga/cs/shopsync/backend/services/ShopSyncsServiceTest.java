package edu.uga.cs.shopsync.backend.services;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.function.Consumer;

import edu.uga.cs.shopsync.backend.firebase.ShopSyncsFirebaseReference;
import edu.uga.cs.shopsync.backend.firebase.UserShopSyncMapFirebaseReference;
import edu.uga.cs.shopsync.backend.models.BasketItemModel;
import edu.uga.cs.shopsync.backend.models.PurchasedItemModel;
import edu.uga.cs.shopsync.backend.models.ShopSyncModel;
import edu.uga.cs.shopsync.backend.models.ShoppingBasketModel;
import edu.uga.cs.shopsync.backend.models.ShoppingItemModel;
import edu.uga.cs.shopsync.utils.ErrorHandle;

@RunWith(MockitoJUnitRunner.class)
@SuppressWarnings("unchecked")
public class ShopSyncsServiceTest {

    private UsersService mockUsersService;
    private ShopSyncsFirebaseReference shopSyncsFirebaseReference;
    private UserShopSyncMapFirebaseReference userShopSyncMapFirebaseReference;
    private ShopSyncsService shopSyncsService;

    @Before
    public void setUp() {
        // mocks
        mockUsersService = mock(UsersService.class);
        shopSyncsFirebaseReference = mock(ShopSyncsFirebaseReference.class);
        userShopSyncMapFirebaseReference = mock(UserShopSyncMapFirebaseReference.class);

        // spies
        shopSyncsService = spy(new ShopSyncsService(mockUsersService, shopSyncsFirebaseReference,
                                                    userShopSyncMapFirebaseReference));
    }

    @Test
    public void testAddShopSync_Success() {
        // Arrange
        String name = "Test ShopSync";
        String description = "Test Description";
        List<String> userUids = List.of("user1", "user2");

        ShopSyncModel mockShopSync = new ShopSyncModel();
        when(shopSyncsFirebaseReference.addShopSync(any(), any(), any(), any(), any())).thenReturn(mockShopSync);

        // Act
        shopSyncsService.addShopSync(name, description, userUids, null, null);

        // Assert
        verify(userShopSyncMapFirebaseReference, times(2)).addShopSyncToUser(any(), any());
        verify(shopSyncsFirebaseReference).addShopSync(name, description, null, null, null);
    }

    @Test
    public void testAddShopSync_Failure() {
        // Arrange
        String name = "Test ShopSync";
        String description = "Test Description";
        List<String> userUids = List.of("user1", "user2");

        when(shopSyncsFirebaseReference.addShopSync(any(), any(), any(), any(), any())).thenReturn(null);

        Consumer<ErrorHandle> onFailure = mock(Consumer.class);

        // Act
        shopSyncsService.addShopSync(name, description, userUids, null, onFailure);

        // Assert
        verify(onFailure).accept(any(ErrorHandle.class));
        verify(userShopSyncMapFirebaseReference, never()).addShopSyncToUser(any(), any());
    }

    @Test
    public void testAddShopSyncToUser() {
        // Arrange
        String userUid = "user1";
        String shopSyncUid = "shopSync1";

        // Act
        shopSyncsService.addShopSyncToUser(userUid, shopSyncUid);

        // Assert
        verify(userShopSyncMapFirebaseReference).addShopSyncToUser(userUid, shopSyncUid);
        verify(shopSyncsService).addShoppingBasket(shopSyncUid, userUid);
    }

    @Test
    public void testGetShopSyncWithUid() {
        // Arrange
        String shopSyncUid = "shopSync1";
        Task<DataSnapshot> mockDataTask = mock(Task.class);
        when(shopSyncsFirebaseReference.getShopSyncWithUid(shopSyncUid)).thenReturn(mockDataTask);

        // Act
        Task<DataSnapshot> result = shopSyncsService.getShopSyncWithUid(shopSyncUid);

        // Assert
        assertNotNull(result);
        assertSame(mockDataTask, result);
    }

    @Test
    public void testGetShopSyncsForUser_Success() {
        // Arrange
        String userUid = "user1";
        Consumer<List<String>> shopSyncUidsConsumer = mock(Consumer.class);

        DataSnapshot mockDataSnapshot = mock(DataSnapshot.class);
        DataSnapshot mockChildDataSnapshot = mock(DataSnapshot.class);
        when(mockChildDataSnapshot.getKey()).thenReturn("shop1");
        when(mockDataSnapshot.getChildren()).thenReturn(List.of(mockChildDataSnapshot));

        Task<DataSnapshot> mockDataTask = mock(Task.class);
        when(mockDataTask.isSuccessful()).thenReturn(true);
        when(mockDataTask.getResult()).thenReturn(mockDataSnapshot);
        when(mockDataTask.addOnCompleteListener(any())).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            ((OnCompleteListener<DataSnapshot>) args[0]).onComplete(mockDataTask);
            return mockDataTask;
        });

        when(userShopSyncMapFirebaseReference.getShopSyncsAssociatedWithUser(userUid)).thenReturn(mockDataTask);

        // Act
        shopSyncsService.getShopSyncsForUser(userUid, shopSyncUidsConsumer, null);

        // Assert
        verify(shopSyncUidsConsumer).accept(List.of("shop1"));
    }

    @Test
    public void testGetShopSyncsForUser_Failure() {
        // Arrange
        String userUid = "user1";
        Consumer<List<String>> shopSyncUidsConsumer = mock(Consumer.class);
        Consumer<ErrorHandle> onError = mock(Consumer.class);

        Task<DataSnapshot> mockDataTask = mock(Task.class);
        when(mockDataTask.isSuccessful()).thenReturn(false);
        when(mockDataTask.addOnCompleteListener(any())).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            ((OnCompleteListener<DataSnapshot>) args[0]).onComplete(mockDataTask);
            return mockDataTask;
        });

        when(userShopSyncMapFirebaseReference.getShopSyncsAssociatedWithUser(userUid)).thenReturn(mockDataTask);

        // Act
        shopSyncsService.getShopSyncsForUser(userUid, shopSyncUidsConsumer, onError);

        // Assert
        verify(shopSyncUidsConsumer, never()).accept(any());
        verify(onError).accept(any(ErrorHandle.class));
    }

    @Test
    public void testGetUsersForShopSync_Success() {
        // Arrange
        String shopSyncUid = "shop1";
        Consumer<List<String>> userUidsConsumer = mock(Consumer.class);

        DataSnapshot mockDataSnapshot = mock(DataSnapshot.class);
        DataSnapshot mockChildDataSnapshot = mock(DataSnapshot.class);
        when(mockChildDataSnapshot.getKey()).thenReturn("user1");
        when(mockDataSnapshot.getChildren()).thenReturn(List.of(mockChildDataSnapshot));

        Task<DataSnapshot> mockDataTask = mock(Task.class);
        when(mockDataTask.isSuccessful()).thenReturn(true);
        when(mockDataTask.getResult()).thenReturn(mockDataSnapshot);
        when(mockDataTask.addOnCompleteListener(any())).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            ((OnCompleteListener<DataSnapshot>) args[0]).onComplete(mockDataTask);
            return mockDataTask;
        });

        when(userShopSyncMapFirebaseReference.getUsersAssociatedWithShopSync(shopSyncUid)).thenReturn(mockDataTask);

        // Act
        shopSyncsService.getUsersForShopSync(shopSyncUid, userUidsConsumer, null);

        // Assert
        verify(userUidsConsumer).accept(List.of("user1"));
    }

    @Test
    public void testGetUsersForShopSync_Failure() {
        // Arrange
        String shopSyncUid = "shop1";
        Consumer<List<String>> userUidsConsumer = mock(Consumer.class);
        Consumer<ErrorHandle> onError = mock(Consumer.class);

        Task<DataSnapshot> mockDataTask = mock(Task.class);
        when(mockDataTask.isSuccessful()).thenReturn(false);
        when(mockDataTask.addOnCompleteListener(any())).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            ((OnCompleteListener<DataSnapshot>) args[0]).onComplete(mockDataTask);
            return mockDataTask;
        });

        when(userShopSyncMapFirebaseReference.getUsersAssociatedWithShopSync(shopSyncUid)).thenReturn(mockDataTask);

        // Act
        shopSyncsService.getUsersForShopSync(shopSyncUid, userUidsConsumer, onError);

        // Assert
        verify(userUidsConsumer, never()).accept(any());
        verify(onError).accept(any(ErrorHandle.class));
    }

    @Test
    public void testUpdateShopSync() {
        // Arrange
        ShopSyncModel updatedShopSync = new ShopSyncModel("uid", "shop1", "Updated Shop", null,
                                                          null, null);

        Task<Void> mockVoidTask = mock(Task.class);
        when(shopSyncsFirebaseReference.updateShopSync(updatedShopSync)).thenReturn(mockVoidTask);

        // Act
        Task<Void> result = shopSyncsService.updateShopSync(updatedShopSync);

        // Assert
        assertNotNull(result);
        assertSame(mockVoidTask, result);
    }

    @Test
    public void testDeleteShopSync_Success() {
        // Arrange
        String shopSyncUid = "shop1";

        Task<Void> mockVoidTask = mock(Task.class);
        when(mockVoidTask.isSuccessful()).thenReturn(true);
        when(mockVoidTask.addOnCompleteListener(any())).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            ((OnCompleteListener<Void>) args[0]).onComplete(mockVoidTask);
            return mockVoidTask;
        });
        when(shopSyncsFirebaseReference.deleteShopSync(shopSyncUid)).thenReturn(mockVoidTask);

        // Act
        shopSyncsService.deleteShopSync(shopSyncUid);

        // Assert
        verify(userShopSyncMapFirebaseReference).removeShopSync(shopSyncUid);
        verify(shopSyncsFirebaseReference).deleteShopSync(shopSyncUid);
    }

    @Test
    public void testDeleteShopSync_Failure() {
        // Arrange
        String shopSyncUid = "shop1";

        Task<Void> mockVoidTask = mock(Task.class);
        when(mockVoidTask.addOnCompleteListener(any())).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            ((OnCompleteListener<Void>) args[0]).onComplete(mockVoidTask);
            return mockVoidTask;
        });
        when(mockVoidTask.isSuccessful()).thenReturn(false);

        when(shopSyncsFirebaseReference.deleteShopSync(shopSyncUid)).thenReturn(mockVoidTask);

        // Act
        shopSyncsService.deleteShopSync(shopSyncUid);

        // Assert
        verify(userShopSyncMapFirebaseReference, never()).removeShopSync(shopSyncUid);
        verify(shopSyncsFirebaseReference).deleteShopSync(shopSyncUid);
    }

    @Test
    public void testAddShoppingItem() {
        // Arrange
        String shopSyncUid = "shop1";
        String itemName = "Item1";
        boolean inBasket = true;

        ShoppingItemModel mockShoppingItem = mock(ShoppingItemModel.class);
        when(shopSyncsFirebaseReference.addShoppingItem(shopSyncUid, itemName, inBasket)).thenReturn(mockShoppingItem);

        // Act
        ShoppingItemModel result = shopSyncsService.addShoppingItem(shopSyncUid, itemName,
                                                                    inBasket);

        // Assert
        assertNotNull(result);
        assertSame(mockShoppingItem, result);
    }

    @Test
    public void testGetShoppingItemWithUid() {
        // Arrange
        String shopSyncUid = "shop1";
        String itemUid = "item1";

        Task<DataSnapshot> mockDataTask = mock(Task.class);
        when(shopSyncsFirebaseReference.getShoppingItemWithUid(shopSyncUid, itemUid)).thenReturn(mockDataTask);

        // Act
        Task<DataSnapshot> result = shopSyncsService.getShoppingItemWithUid(shopSyncUid, itemUid);

        // Assert
        assertNotNull(result);
        assertSame(mockDataTask, result);
    }

    @Test
    public void testGetShoppingItemsWithShopSyncUid() {
        // Arrange
        String shopSyncUid = "shop1";

        Task<DataSnapshot> mockDataTask = mock(Task.class);
        when(shopSyncsFirebaseReference.getShoppingItemsWithShopSyncUid(shopSyncUid)).thenReturn(mockDataTask);

        // Act
        Task<DataSnapshot> result = shopSyncsService.getShoppingItemsWithShopSyncUid(shopSyncUid);

        // Assert
        assertNotNull(result);
        assertSame(mockDataTask, result);
    }

    @Test
    public void testGetShoppingItemsWithName() {
        // Arrange
        String shopSyncUid = "shop1";
        String itemName = "Item1";

        Task<DataSnapshot> mockDataTask = mock(Task.class);
        when(shopSyncsFirebaseReference.getShoppingItemsWithName(shopSyncUid, itemName)).thenReturn(mockDataTask);

        // Act
        Task<DataSnapshot> result = shopSyncsService.getShoppingItemsWithName(shopSyncUid,
                                                                              itemName);

        // Assert
        assertNotNull(result);
        assertSame(mockDataTask, result);
    }

    @Test
    public void testUpdateShoppingItem() {
        // Arrange
        String shopSyncUid = "shop1";
        ShoppingItemModel updatedShoppingItem = mock(ShoppingItemModel.class);

        Task<Void> mockVoidTask = mock(Task.class);
        when(shopSyncsFirebaseReference.updateShoppingItem(shopSyncUid, updatedShoppingItem)).thenReturn(mockVoidTask);

        // Act
        Task<Void> result = shopSyncsService.updateShoppingItem(shopSyncUid, updatedShoppingItem);

        // Assert
        assertNotNull(result);
        assertSame(mockVoidTask, result);
    }

    @Test
    public void testDeleteShoppingItem() {
        // Arrange
        String shopSyncUid = "shop1";
        String itemId = "item1";

        Task<Void> mockVoidTask = mock(Task.class);
        when(shopSyncsFirebaseReference.deleteShoppingItem(shopSyncUid, itemId)).thenReturn(mockVoidTask);

        // Act
        Task<Void> result = shopSyncsService.deleteShoppingItem(shopSyncUid, itemId);

        // Assert
        assertNotNull(result);
        assertSame(mockVoidTask, result);
    }

    @Test
    public void testCheckIfShoppingBasketExists() {
        // Arrange
        String shopSyncUid = "shop1";
        String userUid = "user1";

        Consumer<Boolean> mockConsumer = mock(Consumer.class);

        // Act
        shopSyncsService.checkIfShoppingBasketExists(shopSyncUid, userUid, mockConsumer);

        // Assert
        verify(shopSyncsFirebaseReference).checkIfShoppingBasketExists(shopSyncUid, userUid,
                                                                       mockConsumer);
    }

    @Test
    public void testAddShoppingBasket() {
        // Arrange
        String shopSyncUid = "shop1";
        String userUid = "user1";

        ShoppingBasketModel mockShoppingBasket = mock(ShoppingBasketModel.class);
        when(shopSyncsFirebaseReference.addShoppingBasket(shopSyncUid, userUid)).thenReturn(mockShoppingBasket);

        // Act
        ShoppingBasketModel result = shopSyncsService.addShoppingBasket(shopSyncUid, userUid);

        // Assert
        assertNotNull(result);
        assertSame(mockShoppingBasket, result);
    }

    @Test
    public void testGetShoppingBasketWithUid() {
        // Arrange
        String shopSyncUid = "shop1";
        String userUid = "user1";

        Task<DataSnapshot> mockDataTask = mock(Task.class);
        when(shopSyncsFirebaseReference.getShoppingBasketWithUid(shopSyncUid, userUid)).thenReturn(mockDataTask);

        // Act
        Task<DataSnapshot> result = shopSyncsService.getShoppingBasketWithUid(shopSyncUid, userUid);

        // Assert
        assertNotNull(result);
        assertSame(mockDataTask, result);
    }

    @Test
    public void testUpdateShoppingBasket() {
        // Arrange
        String shopSyncUid = "shop1";
        ShoppingBasketModel updatedShoppingBasket = mock(ShoppingBasketModel.class);

        Task<Void> mockVoidTask = mock(Task.class);
        when(shopSyncsFirebaseReference.updateShoppingBasket(shopSyncUid, updatedShoppingBasket)).thenReturn(mockVoidTask);

        // Act
        Task<Void> result = shopSyncsService.updateShoppingBasket(shopSyncUid,
                                                                  updatedShoppingBasket);

        // Assert
        assertNotNull(result);
        assertSame(mockVoidTask, result);
    }

    @Test
    public void testDeleteShoppingBasket_Success() {
        // Arrange
        String shopSyncUid = "shop1";
        String userUid = "user1";
        Runnable mockSuccessRunnable = mock(Runnable.class);

        // Act
        shopSyncsService.deleteShoppingBasket(shopSyncUid, userUid, mockSuccessRunnable, null);

        // Assert
        verify(shopSyncsFirebaseReference).deleteShoppingBasket(shopSyncUid, userUid,
                                                                mockSuccessRunnable, null);
    }

    @Test
    public void testDeleteShoppingBasket_Failure() {
        // Arrange
        String shopSyncUid = "shop1";
        String userUid = "user1";
        Consumer<ErrorHandle> mockFailureConsumer = mock(Consumer.class);

        // Act
        shopSyncsService.deleteShoppingBasket(shopSyncUid, userUid, null, mockFailureConsumer);

        // Assert
        verify(shopSyncsFirebaseReference).deleteShoppingBasket(shopSyncUid, userUid, null,
                                                                mockFailureConsumer);
    }

    @Test
    public void testAddBasketItem() {
        // Arrange
        String shopSyncUid = "shop1";
        String shoppingBasketUid = "user1";
        String shoppingItemUid = "item1";
        long quantity = 2L;
        double pricePerUnit = 5.0;

        Consumer<BasketItemModel> mockSuccessConsumer = mock(Consumer.class);
        Consumer<ErrorHandle> mockFailureConsumer = mock(Consumer.class);

        // Act
        shopSyncsService.addBasketItem(shopSyncUid, shoppingBasketUid, shoppingItemUid, quantity,
                                       pricePerUnit, mockSuccessConsumer, mockFailureConsumer);

        // Assert
        verify(shopSyncsFirebaseReference).addBasketItem(shopSyncUid, shoppingBasketUid,
                                                         shoppingItemUid, quantity, pricePerUnit,
                                                         mockSuccessConsumer, mockFailureConsumer);
    }

    @Test
    public void testCheckIfPurchasedItemExistsForBasketItem() {
        // Arrange
        String shopSyncUid = "shop1";
        String basketItemUid = "basketItem1";
        Consumer<Boolean> mockConsumer = mock(Consumer.class);

        // Act
        shopSyncsService.checkIfPurchasedItemExistsForBasketItem(shopSyncUid, basketItemUid,
                                                                 mockConsumer);

        // Assert
        verify(shopSyncsFirebaseReference).checkIfPurchasedItemExistsForBasketItem(shopSyncUid,
                                                                                   basketItemUid,
                                                                                   mockConsumer);
    }

    @Test
    public void testCheckIfPurchasedItemExistsForShoppingItem() {
        // Arrange
        String shopSyncUid = "shop1";
        String shoppingItemUid = "shoppingItem1";
        Consumer<Boolean> mockConsumer = mock(Consumer.class);

        // Act
        shopSyncsService.checkIfPurchasedItemExistsForShoppingItem(shopSyncUid, shoppingItemUid,
                                                                   mockConsumer);

        // Assert
        verify(shopSyncsFirebaseReference).checkIfPurchasedItemExistsForShoppingItem(shopSyncUid,
                                                                                     shoppingItemUid, mockConsumer);
    }

    @Test
    public void testAddPurchasedItem_Success() {
        // Arrange
        String shopSyncUid = "shop1";
        String shoppingBasketUid = "basket1";
        BasketItemModel mockBasketItem = mock(BasketItemModel.class);
        Consumer<PurchasedItemModel> mockSuccessConsumer = mock(Consumer.class);

        Task<DataSnapshot> mockDataTask = mock(Task.class);
        when(mockUsersService.getUserProfileWithUid(shoppingBasketUid)).thenReturn(mockDataTask);
        when(mockDataTask.isSuccessful()).thenReturn(true);
        when(mockDataTask.addOnCompleteListener(any())).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            ((OnCompleteListener<DataSnapshot>) args[0]).onComplete(mockDataTask);
            return mockDataTask;
        });

        DataSnapshot mockDataSnapshot = mock(DataSnapshot.class);
        when(mockDataTask.getResult()).thenReturn(mockDataSnapshot);

        when(mockDataSnapshot.child(anyString())).thenReturn(mockDataSnapshot);
        when(mockDataSnapshot.getValue(String.class)).thenReturn("user@example.com");

        // Act
        shopSyncsService.addPurchasedItem(shopSyncUid, shoppingBasketUid, mockBasketItem,
                                          mockSuccessConsumer, null);

        // Assert
        verify(shopSyncsFirebaseReference).addPurchasedItem(eq(shopSyncUid),
                                                            eq(shoppingBasketUid),
                                                            eq(mockBasketItem), anyString(),
                                                            eq(mockSuccessConsumer), isNull());
    }

    @Test
    public void testAddPurchasedItem_Failure_NullUserProfile() {
        // Arrange
        String shopSyncUid = "shop1";
        String shoppingBasketUid = "basket1";
        BasketItemModel mockBasketItem = mock(BasketItemModel.class);
        Consumer<ErrorHandle> mockFailureConsumer = mock(Consumer.class);

        Task<DataSnapshot> mockDataTask = mock(Task.class);
        when(mockUsersService.getUserProfileWithUid(shoppingBasketUid)).thenReturn(mockDataTask);
        when(mockDataTask.isSuccessful()).thenReturn(true);
        when(mockDataTask.getResult()).thenReturn(null);
        when(mockDataTask.addOnCompleteListener(any())).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            ((OnCompleteListener<DataSnapshot>) args[0]).onComplete(mockDataTask);
            return mockDataTask;
        });

        // Act
        shopSyncsService.addPurchasedItem(shopSyncUid, shoppingBasketUid, mockBasketItem, null,
                                          mockFailureConsumer);

        // Assert
        verify(mockFailureConsumer).accept(any(ErrorHandle.class));
        verify(shopSyncsFirebaseReference, never()).addPurchasedItem(any(), any(), any(), any(),
                                                                     any(), any());
    }

    @Test
    public void testAddPurchasedItem_Failure_NullUserEmail() {
        // Arrange
        String shopSyncUid = "shop1";
        String shoppingBasketUid = "basket1";
        BasketItemModel mockBasketItem = mock(BasketItemModel.class);
        Consumer<ErrorHandle> mockFailureConsumer = mock(Consumer.class);

        Task<DataSnapshot> mockDataTask = mock(Task.class);
        when(mockDataTask.addOnCompleteListener(any())).thenAnswer(invocation -> {
            Object[] args = invocation.getArguments();
            ((OnCompleteListener<DataSnapshot>) args[0]).onComplete(mockDataTask);
            return mockDataTask;
        });
        when(mockUsersService.getUserProfileWithUid(shoppingBasketUid)).thenReturn(mockDataTask);
        when(mockDataTask.isSuccessful()).thenReturn(true);

        DataSnapshot mockDataSnapshot = mock(DataSnapshot.class);
        when(mockDataTask.getResult()).thenReturn(mockDataSnapshot);
        when(mockDataSnapshot.child(anyString())).thenReturn(mockDataSnapshot);
        when(mockDataSnapshot.getValue(String.class)).thenReturn(null);

        // Act
        shopSyncsService.addPurchasedItem(shopSyncUid, shoppingBasketUid, mockBasketItem, null,
                                          mockFailureConsumer);

        // Assert
        verify(mockFailureConsumer).accept(any(ErrorHandle.class));
        verify(shopSyncsFirebaseReference, never()).addPurchasedItem(any(), any(), any(), any(),
                                                                     any(), any());
    }

    @Test
    public void testDeleteBasketItem() {
        // Arrange
        String shopSyncUid = "shop1";
        String shoppingBasketUid = "basket1";
        String shoppingItemUid = "item1";
        Consumer<ErrorHandle> mockFailureConsumer = mock(Consumer.class);
        boolean updateShoppingItemInBasketStatus = true;

        // Act
        shopSyncsService.deleteBasketItem(shopSyncUid, shoppingBasketUid, shoppingItemUid,
                                          mockFailureConsumer, updateShoppingItemInBasketStatus);

        // Assert
        verify(shopSyncsFirebaseReference).deleteBasketItem(shopSyncUid, shoppingBasketUid,
                                                            shoppingItemUid, mockFailureConsumer,
                                                            updateShoppingItemInBasketStatus);
    }

    @Test
    public void testGetPurchasedItemsWithUid() {
        // Arrange
        String shopSyncUid = "shop1";
        String purchasedItemUid = "purchasedItem1";

        Task<DataSnapshot> mockDataTask = mock(Task.class);
        when(shopSyncsFirebaseReference.getPurchasedItemWithUid(shopSyncUid, purchasedItemUid)).thenReturn(mockDataTask);

        // Act
        Task<DataSnapshot> result = shopSyncsService.getPurchasedItemsWithUid(shopSyncUid,
                                                                              purchasedItemUid);

        // Assert
        assertNotNull(result);
        assertSame(mockDataTask, result);
    }

    @Test
    public void testGetPurchasedItemsWithShopSyncUid() {
        // Arrange
        String shopSyncUid = "shop1";

        Task<DataSnapshot> mockDataTask = mock(Task.class);
        when(shopSyncsFirebaseReference.getPurchasedItemsWithShopSyncUid(shopSyncUid)).thenReturn(mockDataTask);

        // Act
        Task<DataSnapshot> result = shopSyncsService.getPurchasedItemsWithShopSyncUid(shopSyncUid);

        // Assert
        assertNotNull(result);
        assertSame(mockDataTask, result);
    }

    @Test
    public void testGetPurchasedItemsWithUserUid() {
        // Arrange
        String shopSyncUid = "shop1";
        String userUid = "user1";

        Task<DataSnapshot> mockDataTask = mock(Task.class);
        when(shopSyncsFirebaseReference.getPurchasedItemsWithUserUid(shopSyncUid, userUid)).thenReturn(mockDataTask);

        // Act
        Task<DataSnapshot> result = shopSyncsService.getPurchasedItemsWithUserUid(shopSyncUid,
                                                                                  userUid);

        // Assert
        assertNotNull(result);
        assertSame(mockDataTask, result);
    }

    @Test
    public void testUpdatePurchasedItem() {
        // Arrange
        String shopSyncUid = "shop1";
        PurchasedItemModel mockUpdatedPurchasedItem = mock(PurchasedItemModel.class);

        Task<Void> mockVoidTask = mock(Task.class);
        when(shopSyncsFirebaseReference.updatePurchasedItem(shopSyncUid,
                                                            mockUpdatedPurchasedItem)).thenReturn(mockVoidTask);

        // Act
        Task<Void> result = shopSyncsService.updatePurchasedItem(shopSyncUid,
                                                                 mockUpdatedPurchasedItem);

        // Assert
        assertNotNull(result);
        assertSame(mockVoidTask, result);
    }

    @Test
    public void testDeletePurchasedItem() {
        // Arrange
        String shopSyncUid = "shop1";
        String purchasedItemId = "item1";

        Task<Void> mockVoidTask = mock(Task.class);
        when(shopSyncsFirebaseReference.deletePurchasedItem(shopSyncUid, purchasedItemId)).thenReturn(mockVoidTask);

        // Act
        Task<Void> result = shopSyncsService.deletePurchasedItem(shopSyncUid, purchasedItemId);

        // Assert
        assertNotNull(result);
        assertSame(mockVoidTask, result);
    }

    @Test
    public void testDeleteAllPurchasedItems() {
        // Arrange
        String shopSyncUid = "shop1";

        Task<Void> mockVoidTask = mock(Task.class);
        when(shopSyncsFirebaseReference.deleteAllPurchasedItems(shopSyncUid)).thenReturn(mockVoidTask);

        // Act
        Task<Void> result = shopSyncsService.deleteAllPurchasedItems(shopSyncUid);

        // Assert
        assertNotNull(result);
        assertSame(mockVoidTask, result);
    }

}

