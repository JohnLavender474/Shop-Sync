package edu.uga.cs.shopsync.frontend.fragments;

import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.List;

import edu.uga.cs.shopsync.R;
import edu.uga.cs.shopsync.backend.exceptions.IllegalNullValueException;
import edu.uga.cs.shopsync.backend.models.BasketItemModel;
import edu.uga.cs.shopsync.backend.models.PurchasedItemModel;
import edu.uga.cs.shopsync.backend.models.ShoppingItemModel;
import edu.uga.cs.shopsync.frontend.Constants;
import edu.uga.cs.shopsync.frontend.utils.ChildEventListenerFragment;
import edu.uga.cs.shopsync.utils.ArraySetList;
import edu.uga.cs.shopsync.utils.CallbackReceiver;
import edu.uga.cs.shopsync.utils.Props;

/**
 * This class contains the logic for the purchased items fragment.
 */
public class PurchasedItemsFragment extends ChildEventListenerFragment {

    private static final String TAG = "PurchasedItemsFragment";

    public static final String ACTION_INITIALIZE_PURCHASED_ITEMS =
            "ACTION_INITIALIZE_PURCHASED_ITEMS";
    public static final String ACTION_UNDO_PURCHASE = "ACTION_UNDO_PURCHASE";
    public static final String ACTION_DELETE_PURCHASE = "ACTION_DELETE_PURCHASE";
    public static final String PROP_PURCHASED_ITEMS = "PROP_PURCHASED_ITEMS";

    private final List<PurchasedItemModel> purchasedItems;
    private final PurchasedItemsAdapter adapter;

    private CallbackReceiver callbackReceiver;

    /**
     * Default constructor for PurchasedItemsFragment.
     */
    public PurchasedItemsFragment() {
        super();

        Log.d(TAG, "PurchasedItemsFragment: called");

        callbackReceiver = null;
        purchasedItems = new ArraySetList<>();
        adapter = new PurchasedItemsAdapter(purchasedItems);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: called");

        // if the activity does not implement CallbackReceiver, then throw an exception
        // otherwise, set the callbackReceiver to the activity
        if (callbackReceiver == null) {
            if (!(getActivity() instanceof CallbackReceiver)) {
                Log.e(TAG, "onCreateView: Activity must implement CallbackReceiver");
                throw new ClassCastException("Activity must implement CallbackReceiver");
            }

            Log.d(TAG, "onCreateView: Activity implements CallbackReceiver");
            callbackReceiver = (CallbackReceiver) getActivity();
        }

        View view = inflater.inflate(R.layout.fragment_purchased_items, container, false);

        // set up the RecyclerView and its adapter
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewPurchasedItems);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

        callbackReceiver.onCallback(ACTION_INITIALIZE_PURCHASED_ITEMS, Props.of(
                Pair.create(PROP_PURCHASED_ITEMS, purchasedItems),
                Pair.create(Constants.ADAPTER, adapter)));

        return view;
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart: called");
        super.onStart();

        if (!(getActivity() instanceof CallbackReceiver)) {
            Log.e(TAG, "onStart: Activity must implement CallbackReceiver");
            throw new ClassCastException("Activity must implement CallbackReceiver");
        }

        Log.d(TAG, "onStart: Activity implements CallbackReceiver");
        callbackReceiver = (CallbackReceiver) getActivity();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy: called");
        super.onDestroy();

        Log.d(TAG, "onDestroy: setting callbackReceiver to null");
        callbackReceiver = null;
    }

    @Override
    public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
        Log.d(TAG, "onChildAdded: called with snapshot = " + snapshot + " and previous " +
                "child name = " + previousChildName);

        PurchasedItemModel purchasedItem = snapshot.getValue(PurchasedItemModel.class);
        if (purchasedItem == null || purchasedItem.getPurchasedItemUid() == null ||
                purchasedItem.getPurchasedItemUid().isBlank()) {
            Log.d(TAG, "onChildAdded: purchasedItem is null or has null or blank uid. " +
                    "Snapshot = " + snapshot);
            return;
        }

        Log.d(TAG, "onChildAdded: purchasedItem = " + purchasedItem);

        // TODO: very inefficient to add to beginning of array list, a better way to do this
        // possibly implement a customer array or list adapter than allows for modifying
        // the iteration order
        purchasedItems.add(0, purchasedItem);

        adapter.notifyItemInserted(0);
    }

    @Override
    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
        Log.d(TAG, "onChildChanged: called with snapshot = " + snapshot + " and previous " +
                "child name = " + previousChildName);

        PurchasedItemModel purchasedItem = snapshot.getValue(PurchasedItemModel.class);
        if (purchasedItem == null || purchasedItem.getPurchasedItemUid() == null ||
                purchasedItem.getPurchasedItemUid().isBlank()) {
            Log.d(TAG, "onChildAdded: purchasedItem is null or has null or blank uid. " +
                    "Snapshot = " + snapshot);
            return;
        }

        Log.d(TAG, "onChildChanged: purchasedItem = " + purchasedItem);

        int index = purchasedItems.indexOf(purchasedItem);
        if (index == -1) {
            Log.d(TAG, "onChildChanged: purchasedItem not found in purchasedItems");
            return;
        }

        purchasedItems.set(index, purchasedItem);
        adapter.notifyItemChanged(index);
    }

    @Override
    public void onChildRemoved(@NonNull DataSnapshot snapshot) {
        Log.d(TAG, "onChildRemoved: called with snapshot = " + snapshot);

        PurchasedItemModel purchasedItem = snapshot.getValue(PurchasedItemModel.class);
        if (purchasedItem == null || purchasedItem.getPurchasedItemUid() == null ||
                purchasedItem.getPurchasedItemUid().isBlank()) {
            Log.d(TAG, "onChildAdded: purchasedItem is null or has null or blank uid. " +
                    "Snapshot = " + snapshot);
            return;
        }

        Log.d(TAG, "onChildRemoved: purchasedItem = " + purchasedItem);

        int index = purchasedItems.indexOf(purchasedItem);
        if (index == -1) {
            Log.d(TAG, "onChildRemoved: purchasedItem not found in purchasedItems");
            return;
        }

        purchasedItems.remove(index);
        adapter.notifyItemRemoved(index);
    }

    @Override
    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
        Log.d(TAG, "onChildMoved: called with snapshot = " + snapshot + " and previous " +
                "child name = " + previousChildName);
    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {
        Log.e(TAG, "onCancelled: called with error = " + error);
    }

    public class PurchasedItemsAdapter
            extends RecyclerView.Adapter<PurchasedItemsAdapter.ViewHolder> {

        private final List<PurchasedItemModel> purchasedItems;

        PurchasedItemsAdapter(List<PurchasedItemModel> purchasedItems) {
            Log.d(TAG, "PurchasedItemsAdapter: called");
            this.purchasedItems = purchasedItems;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Log.d(TAG, "onCreateViewHolder: called");
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_purchased_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Log.d(TAG, "onBindViewHolder: called with position = " + position +
                    " and item = " + purchasedItems.get(position));
            holder.bind(purchasedItems.get(position));
        }

        @Override
        public int getItemCount() {
            return purchasedItems.size();
        }

        private class ViewHolder extends RecyclerView.ViewHolder {

            private final TextView textViewItemName;
            private final TextView textViewQuantity;
            private final TextView textViewPricePerUnit;
            private final Button buttonUndoPurchase;
            private final Button buttonDeletePurchase;

            ViewHolder(@NonNull View itemView) {
                super(itemView);

                Log.d(TAG, "ViewHolder: called");

                textViewItemName = itemView.findViewById(R.id.itemNameTextView);
                textViewQuantity = itemView.findViewById(R.id.quantityTextView);
                textViewPricePerUnit = itemView.findViewById(R.id.pricePerUnitTextView);
                buttonUndoPurchase = itemView.findViewById(R.id.undoPurchaseButton);
                buttonDeletePurchase = itemView.findViewById(R.id.deletePurchaseButton);
            }

            void bind(@NonNull PurchasedItemModel purchasedItem) {
                Log.d(TAG, "bind: called with purchasedItem = " + purchasedItem);

                // fetch the shopping item
                if (purchasedItem.getShoppingItem() == null) {
                    throw new IllegalNullValueException("Purchased item cannot have null shopping" +
                                                                " item field");
                }
                ShoppingItemModel shoppingItem = purchasedItem.getShoppingItem();

                // fetch the basket item
                if (purchasedItem.getBasketItem() == null) {
                    throw new IllegalNullValueException("Purchased item cannot have null basket" +
                                                                " item field");
                }
                BasketItemModel basketItem = purchasedItem.getBasketItem();

                // set the item name
                String itemName = shoppingItem.getName();
                if (itemName == null) {
                    throw new IllegalNullValueException("Shopping item cannot have null name" +
                                                                " field");
                }
                textViewItemName.setText(itemName);

                // set the quantity
                String quantity = String.valueOf(basketItem.getQuantity());
                String quantityText = "Quantity: " + quantity;
                textViewQuantity.setText(quantityText);

                // set the price per unit
                String pricePerUnit = String.valueOf(basketItem.getPricePerUnit());
                String pricePerUnitText = "Price per unit: $" + pricePerUnit;
                textViewPricePerUnit.setText(pricePerUnitText);

                // set up the undo purchase button click listener
                buttonUndoPurchase.setOnClickListener(v -> {
                    // Perform undo purchase logic
                    undoPurchase(purchasedItem);
                });

                // set up the delete purchase button click listener
                buttonDeletePurchase.setOnClickListener(v -> {
                    // Perform delete purchase logic
                    deletePurchase(purchasedItem);
                });
            }

            private void undoPurchase(@NonNull PurchasedItemModel purchasedItem) {
                Log.d(TAG, "undoPurchase: called with purchasedItem = " + purchasedItem);
                callbackReceiver.onCallback(ACTION_UNDO_PURCHASE, Props.of(
                        Pair.create(Constants.PURCHASED_ITEM, purchasedItem)));
            }

            private void deletePurchase(@NonNull PurchasedItemModel purchasedItem) {
                Log.d(TAG, "deletePurchase: called with purchasedItem = " + purchasedItem);
                callbackReceiver.onCallback(ACTION_DELETE_PURCHASE, Props.of(
                        Pair.create(Constants.PURCHASED_ITEM, purchasedItem)));
            }

        }

    }

}

