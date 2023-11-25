package edu.uga.cs.shopsync.frontend.fragments;

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

import java.util.ArrayList;
import java.util.List;

import edu.uga.cs.shopsync.R;
import edu.uga.cs.shopsync.backend.models.ShoppingItemModel;
import edu.uga.cs.shopsync.frontend.Constants;
import edu.uga.cs.shopsync.utils.CallbackReceiver;
import edu.uga.cs.shopsync.utils.Props;

/**
 * Fragment for displaying shopping items.
 */
public class ShoppingItemsFragment extends Fragment implements ChildEventListener {

    private static final String TAG = "ShoppingItemsFragment";

    public static final String ACTION_INITIALIZE_SHOPPING_ITEMS =
            "ACTION_INITIALIZE_SHOPPING_ITEMS";
    public static final String ACTION_MOVE_TO_BASKET = "ACTION_ADD_TO_BASKET";
    public static final String PROP_SHOPPING_ITEMS = "PROP_SHOPPING_ITEMS";
    public static final String PROP_SHOPPING_ITEMS_ADAPTER = "PROP_SHOPPING_ITEMS_ADAPTER";

    private final List<ShoppingItemModel> shoppingItems;
    private final ShoppingItemsAdapter adapter;

    private CallbackReceiver callbackReceiver;

    public ShoppingItemsFragment() {
        super();
        callbackReceiver = null;
        shoppingItems = new ArrayList<>();
        adapter = new ShoppingItemsAdapter(shoppingItems);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
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

        // send the shopping items to the parent activity to be initialized
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
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_shopping_item, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.bind(position, items.get(position));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        private class ViewHolder extends RecyclerView.ViewHolder {

            private final EditText editTextItemName;
            private final TextView textViewInBasket;
            private final Button buttonSetInMyShoppingBasket;
            private final Button buttonDeleteItem;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                editTextItemName = itemView.findViewById(R.id.editTextItemName);
                textViewInBasket = itemView.findViewById(R.id.textViewInBasket);
                buttonSetInMyShoppingBasket =
                        itemView.findViewById(R.id.buttonSetInMyShoppingBasket);
                buttonDeleteItem =
                        itemView.findViewById(R.id.buttonDeleteItem);
            }

            void bind(int position, ShoppingItemModel item) {
                // Bind data to views
                editTextItemName.setText(item.getName());
                String inBasketText = "In a basket: " + (item.isInBasket() ? "Yes" : "No");
                textViewInBasket.setText(inBasketText);

                // TODO: listen to updates to shopping item model and update the UI accordingly
                /*
                item.addOnPropertyChangedCallback(new Observable.OnPropertyChangedCallback() {
                    @Override
                    public void onPropertyChanged(Observable sender, int propertyId) {
                        // TODO: disable in basket text view if not in basket
                        // TODO: show basket text view if in basket

                        // TODO: disable add to basket button if in basket
                        // TODO: enable add to basket button if not in basket
                    }
                 */

                buttonSetInMyShoppingBasket.setOnClickListener(v -> setItemInMyBasket(position,
                                                                                      item));
                buttonDeleteItem.setOnClickListener(v -> deleteItem(position, item));
            }

            private void setItemInMyBasket(int position, ShoppingItemModel item) {
                // TODO: do not remove item?
                // shoppingItems.remove(item);

                // Add item to shopping basket, mark this item as in basket, and update
                // the database and UI

                // Notify the adapter that the data set has changed
                notifyDataSetChanged();
            }

            private void deleteItem(int position, ShoppingItemModel item) {
            }
        }
    }
}

