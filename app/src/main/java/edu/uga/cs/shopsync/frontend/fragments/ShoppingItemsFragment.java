package edu.uga.cs.shopsync.frontend.fragments;

import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import edu.uga.cs.shopsync.R;
import edu.uga.cs.shopsync.backend.exceptions.IllegalNullValueException;
import edu.uga.cs.shopsync.backend.models.ShoppingItemModel;
import edu.uga.cs.shopsync.frontend.Constants;
import edu.uga.cs.shopsync.frontend.activities.contracts.FragmentCallbackReceiver;
import edu.uga.cs.shopsync.utils.Props;

/**
 * Fragment for displaying shopping items.
 */
public class ShoppingItemsFragment extends Fragment {

    private static final String TAG = "ShoppingItemsFragment";

    public static final String ACTION_INITIALIZE_SHOPPING_ITEMS =
            "ACTION_INITIALIZE_SHOPPING_ITEMS";
    public static final String ACTION_MOVE_TO_BASKET = "ACTION_ADD_TO_BASKET";
    public static final String PROP_SHOPPING_ITEMS = "PROP_SHOPPING_ITEMS";
    public static final String PROP_SHOPPING_ITEMS_ADAPTER = "PROP_SHOPPING_ITEMS_ADAPTER";

    private FragmentCallbackReceiver callbackReceiver = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        if (callbackReceiver == null) {
            throw new IllegalNullValueException("FragmentCallbackReceiver not initialized");
        }

        View view = inflater.inflate(R.layout.fragment_shopping_items, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.recyclerViewShoppingItems);

        // initialize shopping items (replace this with your actual data retrieval)
        List<ShoppingItemModel> shoppingItems = new ArrayList<>();

        // set up the RecyclerView and its adapter
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        ShoppingItemsAdapter adapter = new ShoppingItemsAdapter(shoppingItems);
        recyclerView.setAdapter(adapter);

        // send the shopping items to the parent activity to be initialized
        callbackReceiver.onFragmentCallback(ACTION_INITIALIZE_SHOPPING_ITEMS, Props.of(
                Pair.create(PROP_SHOPPING_ITEMS, shoppingItems),
                Pair.create(Constants.RECYCLER_VIEW_ADAPTER, adapter)));

        return view;
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart: called");
        super.onStart();

        if (!(getActivity() instanceof FragmentCallbackReceiver)) {
            Log.e(TAG, "onStart: Activity must implement FragmentCallbackReceiver");
            throw new ClassCastException("Activity must implement FragmentCallbackReceiver");
        }

        Log.d(TAG, "onStart: Activity implements FragmentCallbackReceiver");
        callbackReceiver = (FragmentCallbackReceiver) getActivity();
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

    /**
     * Adapter for shopping items.
     */
    public static class ShoppingItemsAdapter
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
            holder.bind(items.get(position));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        private class ViewHolder extends RecyclerView.ViewHolder {

            private final EditText editTextItemName;
            private final EditText editTextInBasket;
            private final Button buttonSetInMyShoppingBasket;

            ViewHolder(@NonNull View itemView) {
                super(itemView);
                editTextItemName = itemView.findViewById(R.id.editTextItemName);
                editTextInBasket = itemView.findViewById(R.id.editTextInBasket);
                buttonSetInMyShoppingBasket =
                        itemView.findViewById(R.id.buttonSetInMyShoppingBasket);
            }

            void bind(ShoppingItemModel item) {
                // Bind data to views
                editTextItemName.setText(item.getName());
                String inBasketText = "In a basket: " + (item.isInBasket() ? "Yes" : "No");
                editTextInBasket.setText(inBasketText);

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

                // Set up the purchase button click listener
                buttonSetInMyShoppingBasket.setOnClickListener(v -> {
                    // Perform purchase logic (remove from shopping items, add to purchased items)
                    // You'll need to implement this part based on your requirements
                    setItemInMyBasket(item);
                });
            }

            private void setItemInMyBasket(ShoppingItemModel item) {
                // TODO: do not remove item?
                // shoppingItems.remove(item);

                // Add item to shopping basket, mark this item as in basket, and update
                // the database and UI

                // Notify the adapter that the data set has changed
                notifyDataSetChanged();
            }
        }
    }
}

