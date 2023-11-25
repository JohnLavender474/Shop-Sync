package edu.uga.cs.shopsync.frontend.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;

import java.util.List;

import edu.uga.cs.shopsync.R;
import edu.uga.cs.shopsync.backend.exceptions.IllegalNullValueException;
import edu.uga.cs.shopsync.backend.models.ShoppingItemModel;
import edu.uga.cs.shopsync.frontend.Constants;
import edu.uga.cs.shopsync.utils.ArraySetList;
import edu.uga.cs.shopsync.utils.CallbackReceiver;
import edu.uga.cs.shopsync.utils.Props;

/**
 * Fragment for displaying shopping items.
 */
public class ShoppingItemsFragment extends Fragment implements ChildEventListener {

    private static final String TAG = "ShoppingItemsFragment";

    public static final String ACTION_INITIALIZE_SHOPPING_ITEMS =
            "ACTION_INITIALIZE_SHOPPING_ITEMS";
    public static final String ACTION_MOVE_SHOPPING_ITEM_TO_BASKET = "ACTION_ADD_TO_BASKET";
    public static final String ACTION_DELETE_SHOPPING_ITEM = "ACTION_DELETE_SHOPPING_ITEM";
    public static final String PROP_SHOPPING_ITEMS = "PROP_SHOPPING_ITEMS";

    private final List<ShoppingItemModel> shoppingItems;
    private final ShoppingItemsAdapter adapter;

    private CallbackReceiver callbackReceiver;

    public ShoppingItemsFragment() {
        super();

        Log.d(TAG, "ShoppingItemsFragment: called");

        callbackReceiver = null;
        shoppingItems = new ArraySetList<>();
        adapter = new ShoppingItemsAdapter(shoppingItems);
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

        View view = inflater.inflate(R.layout.fragment_shopping_items, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewShoppingItems);

        // set up the RecyclerView and its adapter
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(adapter);

        callbackReceiver.onCallback(ACTION_INITIALIZE_SHOPPING_ITEMS, Props.of(
                Pair.create(PROP_SHOPPING_ITEMS, shoppingItems),
                Pair.create(Constants.RECYCLER_VIEW_ADAPTER, adapter)));

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
    public void onStop() {
        Log.d(TAG, "onStop: called");
        super.onStop();

        Log.d(TAG, "onStop: setting callbackReceiver to null");
        callbackReceiver = null;
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
        ShoppingItemModel shoppingItem = snapshot.getValue(ShoppingItemModel.class);
        Log.d(TAG, "onChildAdded: shoppingItem = " + shoppingItem);

        if (shoppingItem == null) {
            Log.e(TAG, "onChildAdded: shoppingItem is null");
            return;
        }

        shoppingItems.add(shoppingItem);
        adapter.notifyItemInserted(shoppingItems.size() - 1);
    }

    @Override
    public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
        ShoppingItemModel shoppingItem = snapshot.getValue(ShoppingItemModel.class);
        Log.d(TAG, "onChildChanged: shoppingItem = " + shoppingItem);

        if (shoppingItem == null) {
            Log.e(TAG, "onChildChanged: shoppingItem is null");
            return;
        }

        String shoppingItemUid = shoppingItem.getUid();
        for (int i = 0; i < shoppingItems.size(); i++) {
            if (shoppingItems.get(i).getUid().equals(shoppingItemUid)) {
                shoppingItems.set(i, shoppingItem);
                adapter.notifyItemChanged(i);
                break;
            }
        }
    }

    @Override
    public void onChildRemoved(@NonNull DataSnapshot snapshot) {
        ShoppingItemModel shoppingItem = snapshot.getValue(ShoppingItemModel.class);
        Log.d(TAG, "onChildRemoved: shoppingItem = " + shoppingItem);

        if (shoppingItem == null) {
            Log.e(TAG, "onChildRemoved: shoppingItem is null");
            return;
        }

        String shoppingItemUid = shoppingItem.getUid();
        for (int i = 0; i < shoppingItems.size(); i++) {
            if (shoppingItems.get(i).getUid().equals(shoppingItemUid)) {
                shoppingItems.remove(i);
                adapter.notifyItemRemoved(i);
                break;
            }
        }
    }

    @Override
    public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
        Log.d(TAG, "onChildMoved: called");
    }

    @Override
    public void onCancelled(@NonNull DatabaseError error) {
        Log.e(TAG, "onCancelled: error = " + error);
    }

    /**
     * Adapter for shopping items.
     */
    public class ShoppingItemsAdapter
            extends RecyclerView.Adapter<ShoppingItemsAdapter.ViewHolder> {

        private final List<ShoppingItemModel> items;

        /**
         * Constructor for the adapter.
         *
         * @param items The list of shopping items to display.
         */
        ShoppingItemsAdapter(List<ShoppingItemModel> items) {
            Log.d(TAG, "ShoppingItemsAdapter: called with items = " + items);
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            Log.d(TAG, "onCreateViewHolder: called");
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_shopping_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Log.d(TAG, "onBindViewHolder: called with position = " + position
                    + " and item = " + items.get(position));
            holder.bind(items.get(position));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        private class ViewHolder extends RecyclerView.ViewHolder {

            private final EditText editTextItemName;
            private final TextView textViewInBasket;
            private final Button buttonSetInBasket;
            private final Button buttonDeleteItem;

            ViewHolder(@NonNull View itemView) {
                super(itemView);

                Log.d(TAG, "ViewHolder: called");

                editTextItemName = itemView.findViewById(R.id.editTextItemName);
                textViewInBasket = itemView.findViewById(R.id.textViewInBasket);
                buttonSetInBasket = itemView.findViewById(R.id.buttonSetInBasket);
                buttonDeleteItem = itemView.findViewById(R.id.buttonDeleteItem);
            }

            void bind(ShoppingItemModel item) {
                Log.d(TAG, "bind: called with item = " + item);

                editTextItemName.setText(item.getName());
                String inBasketText = "In a user's basket: " + (item.isInBasket() ? "Yes" : "No");
                textViewInBasket.setText(inBasketText);

                // set the button to set the item in the user's basket
                buttonSetInBasket.setOnClickListener(v -> setItemInMyBasket(item));
                // if the item is already in a user's basket, then disable the button
                if (item.isInBasket()) {
                    Log.d(TAG, "bind: item is already in a user's basket");
                    buttonSetInBasket.setTextColor(Color.GRAY);
                    buttonSetInBasket.setEnabled(false);
                } else {
                    Log.d(TAG, "bind: item is not in a user's basket");
                    buttonSetInBasket.setTextColor(Color.GREEN);
                    buttonSetInBasket.setEnabled(true);
                }

                // set the button to delete the item
                buttonDeleteItem.setOnClickListener(v -> deleteItem(item));
            }

            private void setItemInMyBasket(ShoppingItemModel shoppingItem) {
                Log.d(TAG, "setItemInMyBasket: called");

                if (callbackReceiver == null) {
                    Log.e(TAG, "setItemInMyBasket: callbackReceiver is null");
                    throw new IllegalNullValueException("callbackReceiver is null");
                }

                Log.d(TAG, "setItemInMyBasket: calling callbackReceiver.onCallback");
                callbackReceiver.onCallback(ACTION_MOVE_SHOPPING_ITEM_TO_BASKET, Props.of(
                        Pair.create(Constants.SHOPPING_ITEM, shoppingItem)));
            }

            private void deleteItem(ShoppingItemModel shoppingItem) {
                Log.d(TAG, "deleteItem: called");

                if (callbackReceiver == null) {
                    Log.e(TAG, "deleteItem: callbackReceiver is null");
                    throw new IllegalNullValueException("callbackReceiver is null");
                }

                Log.d(TAG, "deleteItem: calling callbackReceiver.onCallback");
                callbackReceiver.onCallback(ACTION_DELETE_SHOPPING_ITEM, Props.of(
                        Pair.create(Constants.SHOPPING_ITEM, shoppingItem)));
            }
        }
    }
}

